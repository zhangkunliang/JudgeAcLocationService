package com.fh.crawler.belongingplaceservice.mybatis.mapper;

import com.fh.crawler.belongingplaceservice.mybatis.entity.OcaExtractRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface OcaExtractRecordWemediaMapper {
    int saveOcaExtractRecordWemedia(OcaExtractRecord ocaExtractRecord);

    Long getLastExtractEndIdWemedia(@Param("extractName") String extractName, @Param("extractAppType") String extractAppType);
}
