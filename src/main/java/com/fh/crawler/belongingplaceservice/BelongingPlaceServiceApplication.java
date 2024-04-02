package com.fh.crawler.belongingplaceservice;

import com.fh.belonging.util.MsisdnUtil;
import com.fh.crawler.belongingplaceservice.service.BelongPlaceJudgeService;
import com.fh.crawler.belongingplaceservice.service.SolrToRedisService;
import com.fh.crawler.belongingplaceservice.util.CommonUtil;
import com.fh.crawler.belongingplaceservice.util.SolrToRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;

@SpringBootApplication
public class BelongingPlaceServiceApplication {


    private static final Logger LOGGER = LoggerFactory.getLogger(BelongingPlaceServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BelongingPlaceServiceApplication.class, args);
        BelongPlaceJudgeService belongPlaceJudgeService = context.getBean(BelongPlaceJudgeService.class);
        // 初始化加载
        MsisdnUtil.init();
        //单线程同步处理
        while (belongPlaceJudgeService.runService()) {
            LOGGER.info("处理完成，开始休眠1小时。");
            CommonUtil.threadSleep(60 * 60 * 1000);
            LOGGER.info("结束休眠1小时。");
        }
//        SolrToRedisService solrToRedisService = context.getBean(SolrToRedisService.class);
//        solrToRedisService.idToRedis();
    }
}
