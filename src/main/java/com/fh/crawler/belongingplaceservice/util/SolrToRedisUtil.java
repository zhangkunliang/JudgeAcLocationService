package com.fh.crawler.belongingplaceservice.util;

import com.fh.crawler.belongingplaceservice.constant.Numconstant;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class SolrToRedisUtil {
    public SolrToRedisUtil() {
        // 构造方法
    }
    long extractTotal = 0;
    long extractId = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrToRedisUtil.class);

    public void syncIdsToRedis(SolrTemplate solrTemplate, RedisTemplate<String, String> redisTemplate) {
        SolrQuery solrQuery = new SolrQuery("DomainType:\"0\"");
        solrQuery.setSort("id", SolrQuery.ORDER.asc);
        solrQuery.setRows(Numconstant.N_1000);
        solrQuery.setFields("id");
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse queryResponse = getQueryResponseBySolrQuery(solrQuery, solrTemplate);
            if (queryResponse == null) {
                done = true;
            } else {
                SolrDocumentList solrDocumentList = queryResponse.getResults();
                //没有数据，退出
                if (CollectionUtils.isEmpty(solrDocumentList)) {
                    done = true;
                } else {
                    extractTotal += solrDocumentList.size();
                    //处理数据
                    idsToRedis(solrDocumentList,redisTemplate);
                    String nextCursorMark = queryResponse.getNextCursorMark();
                    //游标为空或者和第一页相同
                    if (nextCursorMark == null || cursorMark.equals(nextCursorMark)) {
                        done = true;
                    }
                    cursorMark = nextCursorMark;
                }
            }

        }
    }
    private void idsToRedis(SolrDocumentList solrDocumentList,RedisTemplate<String, String> redisTemplate){
        solrDocumentList.forEach(solrDocument->{
            extractId++;
            LOGGER.info("第{}条，id={}",extractId,solrDocument.getFieldValue("id").toString());
            redisTemplate.opsForValue().set(solrDocument.getFieldValue("id").toString(),"");
        });
    }

    private QueryResponse getQueryResponseBySolrQuery(SolrQuery solrQuery, SolrTemplate solrTemplate) {
        for (int count = 0; count < Numconstant.N_3; count++) {
            try {
                return solrTemplate.getSolrClient().query(solrQuery);
            } catch (Exception e) {
                LOGGER.error("solr查询请求失败", e);
            }
        }
        return null;
    }


}
