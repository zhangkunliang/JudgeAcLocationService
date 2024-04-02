package com.fh.crawler.belongingplaceservice;

import com.alibaba.fastjson.JSON;
import com.fh.belonging.bean.AreaInfo;
import com.fh.belonging.bean.SolrFields;
import com.fh.belonging.util.BelongPlaceUtil;
import com.fh.belonging.util.MsisdnUtil;
import com.fh.crawler.belongingplaceservice.constant.Numconstant;
import com.fh.crawler.belongingplaceservice.service.BelongingPlaceJudge;
import com.fh.crawler.belongingplaceservice.util.SolrDataTransferUtil;
import com.fh.crawler.belongingplaceservice.util.SolrToRedisUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.CollectionUtils;

import java.io.IOException;

@SpringBootTest
public class BelongingPlaceServiceApplicationTests {
    @Autowired
    private SolrDataTransferUtil solrDataTransferUtil;

    @Autowired
    private SolrToRedisUtil solrToRedisUtil;
    @Value("${area.code}")
    private String areaCodeBaseUrl;

    @Autowired
    private SolrTemplate oversSolrTemplate;

    @Autowired
    private BelongingPlaceJudge belongingPlaceJudge;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void contextLoads() {
        SolrFields solrFields = new SolrFields();
        solrFields.setIntroduction("每晚7点左右直播，不开就发说说。 周文何人，鱼知水恩，乃幸福之源也，知四海皆兄弟，何处相逢非故人。 很高兴认识你。");
        solrFields.setResidence("广东省深圳市");
        AreaInfo areaInfo = BelongPlaceUtil.dealBelongPlace(solrFields, "http://127.0.0.1:8955/AreaService/areaGet?text=");
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        if (!areaInfo.getAreaCodes().isEmpty() || !areaInfo.getFieldInfos().isEmpty()) {
            solrInputDocument.addField("belongplace_ss", (areaInfo.areaCodes));  //打标地区编码
            solrInputDocument.addField("belongfield_ss", (areaInfo.fieldInfos)); //打标使用字段
            System.out.println(JSON.toJSONString(solrFields));
            System.out.println(JSON.toJSONString(solrInputDocument));
        }
        System.out.println(JSON.toJSONString(areaInfo));
    }

    @Test
    void transferData() {
//        solrDataTransferUtil.transferData("100000006",10);
        solrDataTransferUtil.transferData("100000152", 10);
//        solrDataTransferUtil.transferData("100000021",1000);
//        solrDataTransferUtil.transferData("100000268",1000);
//        solrDataTransferUtil.transferData("100000074",1000);
//        solrDataTransferUtil.transferData("100000007",1000);
//        solrDataTransferUtil.transferData("100000010",1000);
    }

    @Test
    void testSolr() throws IOException, SolrServerException {
        MsisdnUtil.init();
        SolrQuery solrQuery = new SolrQuery("id:05b0d71698fa38116f9c3c8832d41058");
        //游标必须指定id排序，同一天内更新时间不强制有序
        solrQuery.setSort("id", SolrQuery.ORDER.asc);
        solrQuery.setRows(Numconstant.N_1000);
        solrQuery.setFields("id,APPTYPE,MOBILEPHONE,NATIVE_PLACE,INTRODUCTION,RESIDENCE,SIGNATURE,telephone_l,briefintro,address,UPDATE_TIME,lastupdatetime,nextupdatetime,telephone,describe,ipArea,location");
        // 游标分页（cursorMark）特性进行数据遍历
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse queryResponse = oversSolrTemplate.getSolrClient().query(solrQuery);
            if (queryResponse == null) {
                done = true;
            } else {
                SolrDocumentList solrDocumentList = queryResponse.getResults();
                belongingPlaceJudge.handleSolrData(solrDocumentList, oversSolrTemplate, "http://127.0.0.1:8983/solr/SPX_DATA_PERSONINFO2");
                //没有数据，退出
                System.out.println(solrDocumentList);
            }
        }
    }

    @Test
    void testTime() {
        // 获取当前毫秒数时间
        long currentTimeInMillis = System.currentTimeMillis();

        // 将毫秒数转换为秒数
        long currentTimeInSeconds = currentTimeInMillis / 1000;

        System.out.println("当前秒数时间：" + currentTimeInSeconds);
    }

    @Test
    void testRedisUtil() {
        solrToRedisUtil.syncIdsToRedis(oversSolrTemplate, redisTemplate);
//        redisTemplate.opsForValue().set("00216e99d1e10238d096be915b188cf0","");
    }

}
