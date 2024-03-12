package com.fh.crawler.belongingplaceservice.service;


import com.alibaba.fastjson.JSON;
import com.fh.belonging.bean.AreaInfo;
import com.fh.belonging.bean.SolrFields;
import com.fh.belonging.util.BelongPlaceUtil;
import com.fh.belonging.util.MsisdnUtil;
import com.fh.crawler.belongingplaceservice.constant.CommonConstant;
import com.fh.crawler.belongingplaceservice.constant.Numconstant;
import com.fh.crawler.belongingplaceservice.mybatis.entity.OcaExtractRecord;
import com.fh.crawler.belongingplaceservice.mybatis.mapper.OcaExtractRecordMapper;
import com.fh.crawler.belongingplaceservice.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author zkl
 * @date 2024/3/5
 */

@Service
public class BelongingPlaceJudge {

    @Value("${area.code}")
    private String areaCodeBaseUrl;
    @Autowired
    private OcaExtractRecordMapper ocaExtractRecordMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(BelongingPlaceJudge.class);

    // 定义1个成员变量，用于记录执行过程中一旦出现错误的情况
    private boolean extractStateFlag = true;

    private void lockExtractStateFlag(boolean extractStateFlag) {
        this.extractStateFlag = extractStateFlag;
    }

    private boolean getExtractStateFlag() {
        return extractStateFlag;
    }

    /**
     * 归属地调用服务
     *
     * @param solrTemplate
     * @param fields
     * @return
     */
    public boolean callBelongingPlaceService(SolrTemplate solrTemplate, String fields, String url) {
        String appType = "all";
        //获取已处理最大的提取ID，作为本次开始ID
        Long extractStartId = getStartIdBasedOnUrl(url, appType);
        // PG库中没有数据的话则从solr中调用获取
        if (extractStartId == null) {
            LOGGER.info("未找到相关提取记录，从头开始处理。");
            //UPDATE_TIME最小值
            extractStartId = queryUpdateTime(appType, SolrQuery.ORDER.asc, solrTemplate, fields);
        }
        //UPDATE_TIME最大值
        Long extractEndId = queryUpdateTime(appType, SolrQuery.ORDER.desc, solrTemplate, fields);

        if (extractStartId == null || extractEndId == null || Objects.equals(extractStartId, extractEndId)) {
            LOGGER.info("没有增量数据待处理，退出。");
            return true;
        }
        Long startTime = extractStartId;
        //起始时间+1天大于extractEndId时间则为extractEndId，否则为起始时间+1天
        Long endTime = calculateEndTimeByStartTime(startTime, extractEndId);
        //首次处理数据
        loopHandleByUpdateTimeRange(startTime, endTime, appType, solrTemplate, fields, url);
        //判断是否是最后1天数据，不是则继续循环处理
        while (!DateUtil.parseLongToDate(startTime).equals(DateUtil.parseLongToDate(endTime))) {
            startTime = endTime;
            endTime = calculateEndTimeByStartTime(startTime, extractEndId);
            //多次处理数据
            loopHandleByUpdateTimeRange(startTime, endTime, appType, solrTemplate, fields, url);
        }
        return true;
    }

    /**
     * 根据url获取起始Id值
     *
     * @param url
     * @param appType
     * @return
     */
    private Long getStartIdBasedOnUrl(String url, String appType) {
        // 境外
        if (url.contains("SPX_DATA_PERSONINFO")) {
            return ocaExtractRecordMapper.getLastExtractEndId(CommonConstant.OCA_EXTRACT_RECORD_NAME_PERSONINFO, appType);
        } else if (url.contains("WEIBO")) {
            //微博
            return ocaExtractRecordMapper.getLastExtractWeiboEndId(CommonConstant.OCA_EXTRACT_RECORD_NAME_WEIBO, appType);
        } else if (url.contains("wemedia")) {
            // 多媒体
            return ocaExtractRecordMapper.getLastExtractEndIdWemedia(CommonConstant.OCA_EXTRACT_RECORD_NAME_WEMEDIA, appType);
        }
        return null;
    }

    /**
     * 在指定的更新时间范围内循环处理Solr中的数据
     *
     * @param startTime
     * @param endTime
     * @param appType
     * @param solrTemplate
     * @param fields
     * @param url
     */
    private void loopHandleByUpdateTimeRange(Long startTime, Long endTime, String appType, SolrTemplate solrTemplate, String fields, String url) {
        LOGGER.info("开始处理数据，数据开始时间：{}，数据结束时间：{}。", startTime, endTime);
        //每个区间重置状态标识为true
        lockExtractStateFlag(true);
        long executeStartTime = System.currentTimeMillis();
        // 提取总数
        long extractTotal = 0L;
        try {
            String sql = formatSolrQuery(appType, fields); // 构建基于appType的Solr查询语句
            // 不同Solr库的更新字段名不同,根据手机号做区分
            String updateTime = judgeUpdateTimeSolrType(fields);
            SolrQuery solrQuery = new SolrQuery(String.format("%s AND %s:[%s TO %s}", sql, updateTime, startTime, endTime));
            //游标必须指定id排序，同一天内更新时间不强制有序
            solrQuery.setSort("id", SolrQuery.ORDER.asc);
            solrQuery.setRows(Numconstant.N_1000);
            solrQuery.setFields("id,APPTYPE,MOBILEPHONE,NATIVE_PLACE,INTRODUCTION,RESIDENCE,SIGNATURE,telephone_l,briefintro,address,UPDATE_TIME,updatetime,nextupdatetime,telephone,describe,ipArea,location");
            // 游标分页（cursorMark）特性进行数据遍历
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
                        handleSolrData(solrDocumentList, solrTemplate, url);
                        String nextCursorMark = queryResponse.getNextCursorMark();
                        //游标为空或者和第一页相同
                        if (nextCursorMark == null || cursorMark.equals(nextCursorMark)) {
                            done = true;
                        }
                        cursorMark = nextCursorMark;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("", e);
            lockExtractStateFlag(false);
        }
        //保存本次提取记录
        OcaExtractRecord ocaExtractRecord = new OcaExtractRecord();
        // 境外
        if (url.contains("SPX_DATA_PERSONINFO")) {
            ocaExtractRecord.setExtractName(CommonConstant.OCA_EXTRACT_RECORD_NAME_PERSONINFO);
        } else if (url.contains("WEIBO")) {
            //微博
            ocaExtractRecord.setExtractName(CommonConstant.OCA_EXTRACT_RECORD_NAME_WEIBO);
        } else if (url.contains("wemedia")) {
            // 多媒体
            ocaExtractRecord.setExtractName(CommonConstant.OCA_EXTRACT_RECORD_NAME_WEMEDIA);
        }
        ocaExtractRecord.setExtractAppType(appType);
        ocaExtractRecord.setExtractStartId(startTime);
        ocaExtractRecord.setExtractDate(Long.parseLong(DateUtil.parseLongToDate(startTime)));
        ocaExtractRecord.setExtractEndId(endTime);
        // 更新提取总数
        ocaExtractRecord.setExtractTotal(extractTotal);
        ocaExtractRecord.setExtractState(getExtractStateFlag() ? 0 : -1);
        Long extractCostTime = (System.currentTimeMillis() - executeStartTime) / Numconstant.N_1000;
        ocaExtractRecord.setExtractCostTime(extractCostTime);
        // 境外
        int count = 0;
        if (url.contains("SPX_DATA_PERSONINFO")) {
            count = ocaExtractRecordMapper.saveOcaExtractRecord(ocaExtractRecord);
        } else if (url.contains("WEIBO")) {
            //微博
            count = ocaExtractRecordMapper.saveOcaExtractWeiboRecord(ocaExtractRecord);
        } else if (url.contains("wemedia")) {
            // 多媒体
            count = ocaExtractRecordMapper.saveOcaExtractRecordWemedia(ocaExtractRecord);
        }
        if (count <= 0) {
            LOGGER.error("保存本次提取记录异常,{}", ocaExtractRecord);
        }
        LOGGER.info("结束处理数据，数据提取日期：{}，数据开始时间：{}，数据结束时间：{}，数据提取总数：{}，数据提取状态：{}，数据提取耗时：{}秒。", DateUtil.parseLongToDate(startTime), startTime, endTime, extractTotal, ocaExtractRecord.getExtractState(), extractCostTime);
    }

    /**
     * 数据处理
     *
     * @param solrDocumentList
     * @param solrTemplate
     * @param url
     */
    private void handleSolrData(SolrDocumentList solrDocumentList, SolrTemplate solrTemplate, String url) {
        int solrDocumentList_size = solrDocumentList.size();
        if (solrDocumentList_size <= 0) return;
        // solr境外库
        List<SolrInputDocument> solrInputDocumentList = new ArrayList<>();
        // 取出每个solrDocument对象的字段值并更新

        solrDocumentList.forEach(solrDocument -> {
            // 创建SolrInputDocument类修改文档
            SolrInputDocument solrInputDocument = new SolrInputDocument();
            // 根据手机号获取地区编码
            String areaCodeByPhone = "";
            // 读取配置，筛选字段中的手机号
            solrInputDocument.addField("id", solrDocument.getFieldValue("id"));

            // 根据solrFields和地区打标服务url获取地区信息
            SolrFields solrFields = new SolrFields();
            // 封装solrFields类
            if (url.contains("SPX_DATA_PERSONINFO")) {
                if (StringUtils.isNotBlank(solrDocument.getFieldValue("MOBILEPHONE").toString())) {
                    areaCodeByPhone = MsisdnUtil.getAreacodeByPhone(solrDocument.getFieldValue("MOBILEPHONE").toString());
                    solrInputDocument.addField("telephoneareacode_s", buildSolrFieldMap(areaCodeByPhone));
                }
                solrFields.setMobilePhone(solrDocument.getFieldValue("MOBILEPHONE").toString());
                solrFields.setIntroduction(solrDocument.getFieldValue("INTRODUCTION").toString());
                solrFields.setHomeTown(solrDocument.getFieldValue("NATIVE_PLACE").toString());
                solrFields.setResidence(solrDocument.getFieldValue("RESIDENCE").toString());
                solrFields.setSignature(solrDocument.getFieldValue("SIGNATURE").toString());
            } else if (url.contains("WEIBO")) {
                // 存在无telephone_l字段的记录
                if (solrDocument.getFieldValue("telephone_l") != null) {
                    areaCodeByPhone = MsisdnUtil.getAreacodeByPhone(solrDocument.getFieldValue("telephone_l").toString());
                    solrInputDocument.addField("telephoneareacode_s", buildSolrFieldMap(areaCodeByPhone));
                    solrFields.setMobilePhone(solrDocument.getFieldValue("telephone_l").toString());
                }
                if (solrDocument.getFieldValue("briefintro") != null) {
                    solrFields.setIntroduction(solrDocument.getFieldValue("briefintro").toString());
                }
                if (solrDocument.getFieldValue("address") != null) {
                    solrFields.setHomeTown(solrDocument.getFieldValue("address").toString());
                }
            } else if (url.contains("wemedia")) {
                if (solrDocument.getFieldValue("telephone") != null) {
                    areaCodeByPhone = MsisdnUtil.getAreacodeByPhone(solrDocument.getFieldValue("telephone").toString());
                    solrInputDocument.addField("telephoneareacode_s", buildSolrFieldMap(areaCodeByPhone));
                    solrFields.setMobilePhone(solrDocument.getFieldValue("telephone").toString());
                }
                if (solrDocument.getFieldValue("describe") != null) {
                    solrFields.setIntroduction(solrDocument.getFieldValue("describe").toString());
                }

                if (solrDocument.getFieldValue("ipArea") != null) {
                    solrFields.setHomeTown(solrDocument.getFieldValue("ipArea").toString());
                }
                if (solrDocument.getFieldValue("location") != null) {
                    solrFields.setResidence(solrDocument.getFieldValue("location").toString());
                }

            }

            //根据居住地或者家乡获取地区编码
            AreaInfo areaInfo = BelongPlaceUtil.dealBelongPlace(solrFields, areaCodeBaseUrl);
            if (areaInfo.getAreaCodes().size() > 0 || areaInfo.getFieldInfos().size() > 0) {
                solrInputDocument.addField("belongplace_ss", buildSolrFieldMap(areaInfo.areaCodes));  //打标地区编码
                solrInputDocument.addField("belongfield_ss", buildSolrFieldMap(areaInfo.fieldInfos)); //打标使用字段
                LOGGER.info("solrFields={}", JSON.toJSONString(solrFields));
                LOGGER.info("solrInputDocument={}", solrInputDocument);
            }
            if (solrInputDocument.getFieldValue("telephoneareacode_s") != null
                    || areaInfo.getAreaCodes().size() > 0 || areaInfo.getFieldInfos().size() > 0) {
                solrInputDocumentList.add(solrInputDocument);
            }
        });
        if (solrInputDocumentList.size() > 0) {
            UpdateBySolrInputDocument(solrInputDocumentList, solrTemplate);
        }
        System.out.println("solrInputDocumentList大小" + solrInputDocumentList.size());
    }

    /**
     * 更新Solr
     *
     * @param solrInputDocumentList
     * @param solrTemplate
     * @return
     */
    private UpdateResponse UpdateBySolrInputDocument(List<SolrInputDocument> solrInputDocumentList, SolrTemplate solrTemplate) {
        for (int count = 0; count < Numconstant.N_3; count++) {
            try {
                System.out.println("更新成功");
                return solrTemplate.getSolrClient().add(solrInputDocumentList);
            } catch (Exception e) {
                LOGGER.error("solr更新请求失败", e);
            }
        }
        lockExtractStateFlag(false);
        return null;
    }

    /**
     * Solr类型构建
     *
     * @param value
     * @return
     */
    private Map<String, Object> buildSolrFieldMap(Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put("set", value);
        return map;
    }

    /**
     * 查询更新时间
     *
     * @param appType
     * @param order
     * @param solrTemplate
     * @param fields
     * @return
     */
    private Long queryUpdateTime(String appType, SolrQuery.ORDER order, SolrTemplate solrTemplate, String fields) {
        SolrQuery solrQuery = new SolrQuery(formatSolrQuery(appType, fields));
        System.out.println(solrQuery);
        solrQuery.setSort(judgeUpdateTimeSolrType(fields), order);
        solrQuery.setRows(Numconstant.N_1);
        for (int count = 0; count < Numconstant.N_3; count++) {
            try {
                QueryResponse rsp = solrTemplate.getSolrClient().query(solrQuery);
                SolrDocumentList solrDocumentList = rsp.getResults();
                if (CollectionUtils.isEmpty(solrDocumentList)) {
                    return null;
                }
                // 微博库中有的记录没有updatetime字段
                for (int i = 0; i < solrDocumentList.size(); i++) {
                    if (solrDocumentList.get(i).containsKey(judgeUpdateTimeSolrType(fields))) {
                        return Long.parseLong(solrDocumentList.get(i).getFieldValue(judgeUpdateTimeSolrType(fields)).toString());
                    }
                }
                return null;

            } catch (Exception e) {
                LOGGER.error("solr查询请求失败", e);
            }
        }
        return null;
    }

    /**
     * 根据fields首个字段不同作区分
     *
     * @param fields
     * @return
     */
    private String judgeUpdateTimeSolrType(String fields) {
        String upt = "";
        String solrFlag = fields.split(",")[0];
        switch (solrFlag) {
            case "MOBILEPHONE":
                upt = "UPDATE_TIME";
                break;
            case "telephone_l":
                upt = "nextupdatetime";
                break;
            case "telephone":
                upt = "updatetime";
                break;
        }
        return upt;
    }

    /**
     * 计算结束时间
     *
     * @param startTime
     * @param extractEndId
     * @return
     */
    private Long calculateEndTimeByStartTime(Long startTime, Long extractEndId) {
        //开始时间+1天，作为当天数据结束时间
        Long dateLong = DateUtil.parseDateToLong(DateUtil.parseLongToDate(startTime + 24 * 60 * 60) + " 00:00:00");
        //判断是否是最后1天数据
        return dateLong > extractEndId ? extractEndId : dateLong;
    }

    /**
     * Solr查询结果
     *
     * @param solrQuery
     * @param solrTemplate
     * @return
     */
    private QueryResponse getQueryResponseBySolrQuery(SolrQuery solrQuery, SolrTemplate solrTemplate) {
        for (int count = 0; count < Numconstant.N_3; count++) {
            try {
                return solrTemplate.getSolrClient().query(solrQuery);
            } catch (Exception e) {
                LOGGER.error("solr查询请求失败", e);
            }
        }
        lockExtractStateFlag(false);
        return null;
    }

    /**
     * Solr查询语句拼接
     *
     * @param appType
     * @param fields
     * @return
     */
    private String formatSolrQuery(String appType, String fields) {
        StringBuilder queryBuilder = new StringBuilder();
        // 微博类型手机号为long类型
        if (fields.contains("telephone_l")) {
            queryBuilder.append("(telephone_l:[0 TO *] OR briefintro:* " +
                    "OR address:*)");
        } else if (fields.contains("MOBILEPHONE")) {
            queryBuilder.append("(MOBILEPHONE:[\"\" TO *] OR NATIVE_PLACE:[\"\" TO *] " +
                    "OR INTRODUCTION:* OR RESIDENCE:* OR SIGNATURE:*)");
        } else if (fields.contains("telephone")) {
            queryBuilder.append("(telephone:[\"\" TO *] OR describe:* " +
                    "OR ipArea:* OR location:*)");
        }
        String sql = queryBuilder.toString();
        if (StringUtils.isEmpty(appType) || appType.equalsIgnoreCase("all")) {
            return sql;
        } else {
            return "APPTYPE:" + appType + " AND " + sql;
        }

    }

}
