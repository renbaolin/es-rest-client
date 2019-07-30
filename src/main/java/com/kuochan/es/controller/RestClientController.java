package com.kuochan.es.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.kuochan.es.constant.RestClientConstant;
import com.kuochan.es.vo.PostVO;
import com.kuochan.es.vo.SearchResultVO;

/**
 * RestClient demo
 *
 * @author 贝壳
 * @date 2019/7/17 3:27 PM
 */
@RestController
@RequestMapping("/rest_client")
public class RestClientController {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${weibo.fid}")
    private Long weiBoFid;

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @RequestMapping("/query")
    public Object query(@RequestParam(value = "id", defaultValue = "1") String id) {
        GetRequest getRequest = new GetRequest(RestClientConstant.POST_INDEX, RestClientConstant.INDEX_TYPE, id);
        GetResponse response = null;
        try {
            response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 根据分页查询
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/query_by_page")
    public SearchResultVO queryByPage(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "15") int size) {

        //分页信息
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(page);
        searchSourceBuilder.size(size);

        //匹配关键字
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.matchQuery(RestClientConstant.SUBJECT, keyword).boost(5F))
                .should(QueryBuilders.matchQuery(RestClientConstant.MESSAGE, keyword));
        searchSourceBuilder.query(boolQueryBuilder);

        //类型为帖子, 状态为正常;
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        filter.must(QueryBuilders.termQuery(RestClientConstant.TYPE, RestClientConstant.POST_TYPE))
                .must(QueryBuilders.rangeQuery(RestClientConstant.STATUS).gte(0));
        filter.mustNot(QueryBuilders.termQuery(RestClientConstant.FID, weiBoFid));
        searchSourceBuilder.postFilter(filter);

        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest(RestClientConstant.POST_INDEX);
        searchRequest.types(RestClientConstant.INDEX_TYPE);
        searchRequest.source(searchSourceBuilder);
        SearchResultVO searchResultVO = new SearchResultVO();
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = response.getHits();
            long total = searchHits.getTotalHits();
            searchResultVO.setTotal(total);
            List<PostVO> postVOList = Lists.newArrayListWithCapacity((int) total);
            SearchHit[] searchHitList = searchHits.getHits();
            for (SearchHit searchHit : searchHitList){
                String sourceString = searchHit.getSourceAsString();
                if(StringUtils.isNotEmpty(sourceString) && JSON.isValid(sourceString)){
                    PostVO postVO = JSON.parseObject(searchHit.getSourceAsString(),PostVO.class);
                    postVOList.add(postVO);
                }
            }
            searchResultVO.setData(postVOList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResultVO;
    }

    /**
     * 添加数据
     * @param subject
     * @param message
     * @param fid
     * @return
     */
    @RequestMapping("/add")
    public Object add(@RequestParam(value = "id", defaultValue = "") String id,
                      @RequestParam(value = "subject", defaultValue = "") String subject,
                      @RequestParam(value = "message", defaultValue = "") String message,
                      @RequestParam(value = "fid", defaultValue = "") String fid) {
        IndexResponse response = null;
        try {
            IndexRequest indexRequest = new IndexRequest(RestClientConstant.POST_INDEX, RestClientConstant.INDEX_TYPE, id);
            String jsonString = "{" +
                    "\"id\":\"" + id + "\"," +
                    "\"subject\":\"" + subject + "\"," +
                    "\"fid\":\"" + fid + "\"," +
                    "\"message\":\"" + message + "\"," +
                    "\"type\":\"1\"," +
                    "\"status\":\"0\"" +
                    "}";
            indexRequest.source(jsonString, XContentType.JSON);
            response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping("/delete")
    public Object delete(@RequestParam(value = "id", defaultValue = "1") String id) {
        DeleteResponse response = null;
        try {
            DeleteRequest deleteRequest = new DeleteRequest(RestClientConstant.POST_INDEX, RestClientConstant.INDEX_TYPE, id);
            response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
