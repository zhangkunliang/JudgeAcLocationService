package com.fh.crawler.belongingplaceservice.util;

import com.fh.crawler.belongingplaceservice.constant.Numconstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
    private CommonUtil() {
        // 构造方法
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    /**
     * 线程睡眠
     *
     * @param time
     */
    public static void threadSleep(long time) {
        time = time > 0 ? time : Numconstant.N_1000;
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
    }

}
