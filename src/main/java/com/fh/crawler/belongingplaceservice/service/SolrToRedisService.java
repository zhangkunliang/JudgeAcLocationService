package com.fh.crawler.belongingplaceservice.service;

import com.fh.crawler.belongingplaceservice.util.SolrToRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;

@Service
public class SolrToRedisService {
    @Autowired
    private SolrToRedisUtil solrToRedisUtil;

    @Autowired
    private SolrTemplate oversSolrTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    public void idToRedis() {
        solrToRedisUtil.syncIdsToRedis(oversSolrTemplate, redisTemplate);
    }
}
