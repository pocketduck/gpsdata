package com.chexiao.gpsdata.util;

import org.junit.Test;

/**
 * Created by fulei on 2016/12/27.
 */
public class HttpClientTest {

    @Test
    public void getBaidulantiAndLongti() {
        //106.090483,25.1722
        //104.054283,30.5894
       // 30.493734247746247,104.08021888698887
        String result = BaiduUtil.testGetBaiduLnti(Double.valueOf("30.493734247746247"),Double.valueOf("104.08021888698887"));
        System.out.println(result);
    }
}
