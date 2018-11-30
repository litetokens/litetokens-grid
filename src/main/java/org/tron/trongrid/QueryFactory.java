package org.tron.trongrid;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import java.lang.String;

public class QueryFactory {

    private Query query;

    public static final String findByContractAndEventSinceTimestamp = "{ 'contract_address' : ?0, " +
            "'event_name': ?1,  " +
            "'$or' : [ {'block_timestamp' : ?2}, {'block_timestamp' : {$gt : ?2}} ], " +
            "'resource_Node' : {$exists : true} }";

    public static final String findByContractSinceTimeStamp = "{ 'contract_address' : ?0, " +
            "'$or' : [ {'block_timestamp' : ?1}, {'block_timestamp' : {$gt : ?1}} ], " +
            "'resource_Node' : {$exists : true}}";

    public static Pageable make_pagination(int page_num, int page_size, String sort_property){

        if (sort_property.charAt(0) == '-')
            return PageRequest.of(page_num, page_size, Sort.Direction.DESC, sort_property.substring(1));

        return PageRequest.of(page_num, page_size, Sort.Direction.ASC, sort_property);
    }

    private static long getParameterorDefault(HttpServletRequest request, String param, long def){
        return request.getParameter(param) != null && request.getParameter(param).length() > 0 ? Long.parseLong(request.getParameter(param)) : def;
    }

    private static int getParameterorDefault(HttpServletRequest request, String param, int def){
        return request.getParameter(param) != null && request.getParameter(param).length() > 0 ? Integer.parseInt(request.getParameter(param)) : def;
    }

    private static String getParameterorDefault(HttpServletRequest request, String param, String def){
        String by = def;
        by = request.getParameter(param) != null && request.getParameter(param).length() > 0 ? request.getParameter(param) : def;
        return by;
    }

    public static boolean isBool(String s) {
        return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
    }

    public static QueryFactory intialize (HttpServletRequest request){
        Map params = request.getParameterMap();
        Iterator i = params.keySet().iterator();
        long since = 0, block = 0, until = 0, to = 0;
        int page = 0, size = 20;
        String sort = "-block_timestamp";

        while ( i.hasNext() ) {
            String key = (String) i.next();
            if ("since".equalsIgnoreCase(key)) {
                since = getParameterorDefault(request,key,since);
                continue;
            }
            if ("block".equalsIgnoreCase(key)){
                block = getParameterorDefault(request,key, block);
                continue;
            }
            if ("until".equalsIgnoreCase(key)){
                until = getParameterorDefault(request, key,until);
                continue;
            }
            if ("to".equalsIgnoreCase(key)){
                to = getParameterorDefault(request, key, to);
                continue;
            }
            if ("page".equalsIgnoreCase(key)){
                page = Math.max(0,getParameterorDefault(request,key,page)-1);
                continue;
            }
            if ("sort".equalsIgnoreCase(key)){
                sort = getParameterorDefault(request,key,sort);
                continue;
            }
            if ("size".equalsIgnoreCase(key)){
                size = Math.min(200,getParameterorDefault(request,key,size));
                continue;
            }

        }

        QueryFactory query = new QueryFactory();
        query.setBetweenTime(since, until);
//        query.setBetweenBlock(block, to);
        query.setPageniate(make_pagination(page, size, sort));
        return query;
    }

    public QueryFactory(){
        this.query = new Query();
        this.query.addCriteria(Criteria.where("resource_Node").exists(true));
    }

    public QueryFactory(long timestamp, long blocknum){
        this.query = new Query();
        this.query.addCriteria(Criteria.where("resource_Node").exists(true));
        this.setTimestampGreaterEqual(timestamp);
        if (blocknum >= 0)
            this.setBocknumberGreaterEqual(blocknum);
    }

    public void setBetweenTime(long from, long to){
        if (from == 0 && to == 0) return;
        if (from == 0){
            this.setTimestampLessEqual(to);
            return;
        }
        if(to == 0){
            this.setTimestampGreaterEqual(from);
            return;
        }
        this.query.addCriteria(Criteria.where("block_timestamp").gte(Math.min(from,to)).lte(Math.max(from,to)));

    }

    public void setBetweenBlock(long from, long to){
        if (from == 0 && to == 0) return;
        if (from == 0){
            this.setBlocknumberLessEqual(to);
            return;
        }
        if(to == 0){
            this.setBocknumberGreaterEqual(from);
            return;
        }
        this.query.addCriteria(Criteria.where("block_number").gte(Math.min(from,to)).lte(Math.max(from,to)));

    }

    public void setTimestampGreaterEqual (long timestamp) {
        this.query.addCriteria(Criteria.where("block_timestamp").gte(timestamp));
    }

    public void setTimestampLessEqual(long timestamp) {
        if (timestamp < 0) return;
        this.query.addCriteria(Criteria.where("block_timestamp").lte(timestamp));
    }

    public void setBocknumberGreaterEqual (long blockNum) {
        this.query.addCriteria(Criteria.where("block_number").gte(blockNum));
    }

    public void setBlocknumberLessEqual(long blockNum) {
        if (blockNum < 0) return;
        this.query.addCriteria((Criteria.where("block_number").lte(blockNum)));
    }

    public void setContractAddress (String addr) {
        this.query.addCriteria(Criteria.where("contract_address").is(addr));
    }

    public void setPageniate(Pageable page){
        this.query.with(page);
    }

    public void setEventName (String event) {
        this.query.addCriteria(Criteria.where("event_name").is(event));
    }

    public void setTxid (String txid) {
        this.query.addCriteria(Criteria.where("transaction_id").is(txid));
    }

    public void setBockNum(long block){
        this.query.addCriteria(Criteria.where("block_number").is(block));
    }

    public String toString (){
        return this.query.toString();
    }

    public Query getQuery() { return this.query; }

}
