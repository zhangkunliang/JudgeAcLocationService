package com.fh.crawler.belongingplaceservice.service;


import com.fh.crawler.belongingplaceservice.BelongingPlaceServiceApplication;
import com.fh.crawler.belongingplaceservice.config.SolrConfig;
import com.fh.crawler.belongingplaceservice.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class BelongPlaceJudgeService {

    @Autowired
    private SolrConfig solrConfig;

    @Autowired
    private BelongingPlaceJudge belongingPlaceJudge;

    @Autowired
    private SolrTemplate oversSolrTemplate;

    @Autowired
    private SolrTemplate weiboSolrTemplate;

    @Autowired
    private SolrTemplate mediaSolrTemplate;

    public boolean runService() {
        return belongingPlaceJudge.callBelongingPlaceService(oversSolrTemplate, solrConfig.getOversFields(), solrConfig.getOversSolrHost());
    }

    public void executeParallelQueries() {
        // 创建一个固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(3);
        // 创建并执行任务
        executor.submit(new SolrTask(oversSolrTemplate, solrConfig.getOversFields(), solrConfig.getOversSolrHost()));
        executor.submit(new SolrTask(weiboSolrTemplate, solrConfig.getWeiboFields(), solrConfig.getWeiboSolrHost()));
        executor.submit(new SolrTask(mediaSolrTemplate, solrConfig.getMediaFields(), solrConfig.getMediaSolrHost()));
        // 关闭线程池
        executor.shutdown();
    }
}
