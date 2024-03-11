package com.fh.crawler.belongingplaceservice.service;

import com.fh.crawler.belongingplaceservice.BelongingPlaceServiceApplication;
import com.fh.crawler.belongingplaceservice.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.solr.core.SolrTemplate;

public class SolrTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BelongingPlaceServiceApplication.class);
    private final SolrTemplate solrTemplate;
    private final String fields;
    private final String solrHost;

    public SolrTask(SolrTemplate solrTemplate, String fields, String solrHost) {
        this.solrTemplate = solrTemplate;
        this.fields = fields;
        this.solrHost = solrHost;
    }

    @Override
    public void run() {
        BelongingPlaceJudge belongingPlaceJudge = new BelongingPlaceJudge();
        while (belongingPlaceJudge.callBelongingPlaceService(solrTemplate, fields, solrHost)) {
            LOGGER.info("处理完成，开始休眠1小时。");
            CommonUtil.threadSleep(60 * 60 * 1000);
            LOGGER.info("结束休眠1小时。");
        }

    }


}
