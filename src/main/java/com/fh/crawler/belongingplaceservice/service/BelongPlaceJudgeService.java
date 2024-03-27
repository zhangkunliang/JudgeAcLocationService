package com.fh.crawler.belongingplaceservice.service;


import com.fh.crawler.belongingplaceservice.config.SolrConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;


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


}
