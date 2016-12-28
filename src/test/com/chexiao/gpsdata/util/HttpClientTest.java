package com.chexiao.gpsdata.util;

import org.junit.Test;

/**
 * Created by fulei on 2016/12/27.
 */
public class HttpClientTest {

    @Test
    public void getBaidulantiAndLongti() {
        String result = BaiduUtil.testGetBaiduLnti(Double.valueOf("30.4967"),Double.valueOf("104.0768"));
        System.out.println(result);
    }
}
