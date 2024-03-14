package com.fh.crawler.belongingplaceservice.util;

import com.alibaba.fastjson.JSON;
import com.fh.crawler.belongingplaceservice.constant.Numconstant;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SolrDataTransferUtil {

    @Autowired
    private SolrTemplate sourceSolrTemplate;

    @Autowired
    private SolrTemplate destinationSolrTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrDataTransferUtil.class);

    // 生产环境->测试环境  境内数据
    public void transferData(String appType, int num) {

        SolrQuery query = new SolrQuery("APPTYPE:" + appType+" AND RESIDENCE:*");
//        SolrQuery query = new SolrQuery("APPTYPE:" + appType);
        query.setRows(num);
        QueryResponse response = getQueryResponseBySolrQuery(query, sourceSolrTemplate);
        assert response != null;
        SolrDocumentList solrDocumentList = response.getResults();
        if (num > solrDocumentList.size()) {
            num = solrDocumentList.size();
        }
        Random random = new Random();
        List<SolrInputDocument> solrInputDocumentList = new ArrayList<>();
        int index = 0;
        while (index < num) {
            int randomIndex = random.nextInt(solrDocumentList.size());
            SolrInputDocument inputDocument = new SolrInputDocument();
            SolrDocument document = solrDocumentList.get(randomIndex);
            document.setField("UPDATE_TIME", System.currentTimeMillis() / 1000);
//            document.setField("RESIDENCE", "天津东丽");
//            document.setField("INTRODUCTION", "天津市巴拉拉小魔仙玩偶售卖处");
            document.forEach(inputDocument::addField);
            // 不需要该字段，否则报错
            inputDocument.removeField("_version_");
            solrInputDocumentList.add(inputDocument);
            LOGGER.info("{}", JSON.toJSONString(inputDocument));
            LOGGER.info("{}", inputDocument.getFieldValue("id"));
            UpdateBySolrInputDocument(solrInputDocumentList, destinationSolrTemplate);
            LOGGER.info("{}", inputDocument.getFieldValue("id"));
            index += 1;
        }
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

    private UpdateResponse UpdateBySolrInputDocument(List<SolrInputDocument> solrInputDocumentList, SolrTemplate solrTemplate) {
        for (int count = 0; count < Numconstant.N_3; count++) {
            try {
                LOGGER.info("{}", "更新成功");
                return solrTemplate.getSolrClient().add(solrInputDocumentList);
            } catch (Exception e) {
                LOGGER.error("solr更新请求失败", e);
            }
        }
        return null;
    }
}
