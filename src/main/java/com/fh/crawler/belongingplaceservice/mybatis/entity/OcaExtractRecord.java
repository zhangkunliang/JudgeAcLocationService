package com.fh.crawler.belongingplaceservice.mybatis.entity;

public class OcaExtractRecord {
    private Long id;
    private String extractName;
    private String extractAppType;
    private Long extractDate;
    private Long extractStartId;
    private Long extractEndId;
    private Long extractTotal;
    private Long extractCostTime;
    private Integer extractState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExtractName() {
        return extractName;
    }

    public void setExtractName(String extractName) {
        this.extractName = extractName;
    }

    public String getExtractAppType() {
        return extractAppType;
    }

    public void setExtractAppType(String extractAppType) {
        this.extractAppType = extractAppType;
    }

    public Long getExtractDate() {
        return extractDate;
    }

    public void setExtractDate(Long extractDate) {
        this.extractDate = extractDate;
    }

    public Long getExtractStartId() {
        return extractStartId;
    }

    public void setExtractStartId(Long extractStartId) {
        this.extractStartId = extractStartId;
    }

    public Long getExtractEndId() {
        return extractEndId;
    }

    public void setExtractEndId(Long extractEndId) {
        this.extractEndId = extractEndId;
    }

    public Long getExtractTotal() {
        return extractTotal;
    }

    public void setExtractTotal(Long extractTotal) {
        this.extractTotal = extractTotal;
    }

    public Long getExtractCostTime() {
        return extractCostTime;
    }

    public void setExtractCostTime(Long extractCostTime) {
        this.extractCostTime = extractCostTime;
    }

    public Integer getExtractState() {
        return extractState;
    }

    public void setExtractState(Integer extractState) {
        this.extractState = extractState;
    }
}
