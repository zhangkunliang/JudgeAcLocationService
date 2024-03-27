package com.fh.crawler.belongingplaceservice.mybatis.mapper;

import com.fh.crawler.belongingplaceservice.mybatis.entity.OcaExtractRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface OcaExtractRecordMapper {
    int saveOcaExtractRecord(OcaExtractRecord ocaExtractRecord);

    Long getLastExtractEndId(@Param("extractName") String extractName, @Param("extractAppType") String extractAppType);

    int saveOcaExtractWeiboRecord(OcaExtractRecord ocaExtractRecord);

    Long getLastExtractWeiboEndId(@Param("extractName") String extractName, @Param("extractAppType") String extractAppType);

    int saveOcaExtractRecordWemedia(OcaExtractRecord ocaExtractRecord);

    Long getLastExtractEndIdWemedia(@Param("extractName") String extractName, @Param("extractAppType") String extractAppType);

}
