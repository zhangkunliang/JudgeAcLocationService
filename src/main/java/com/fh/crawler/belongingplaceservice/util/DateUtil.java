package com.fh.crawler.belongingplaceservice.util;

import com.fh.crawler.belongingplaceservice.constant.Numconstant;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class DateUtil {
    private DateUtil(){
        // 构造方法
    }
    public static String parseLongToDate(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp * Numconstant.N_1000), ZoneOffset.of("+8"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return dateTimeFormatter.format(localDateTime);
    }

    public static Long parseDateToLong(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() / Numconstant.N_1000;

    }
}
