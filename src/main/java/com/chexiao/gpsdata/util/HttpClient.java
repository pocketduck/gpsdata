package com.chexiao.gpsdata.util;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by fulei on 2016/12/27.
 */
public class HttpClient {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HttpClient.class);

    public static String httpClientGet(String url) {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(500).setSocketTimeout(2000).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();
        HttpGet get = new HttpGet(url);
        try {
            CloseableHttpResponse response = client.execute(get);

            try {
                if (response.getStatusLine().getStatusCode() == 200) {// 如果状态码为200,就是正常返回
                    String ret = EntityUtils.toString(response.getEntity());
                    if (ret == null) {
                        return "";
                    }
                    return ret;
                }
            } catch (IOException e) {
                logger.error("http response error",e);
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                response.close();
            }
            return "";
        } catch (Exception e) {
            logger.error("http 请求超时，url:"+url,e);
            return null;
        } finally {

            try {
                client.close();
            } catch (IOException e) {
                logger.error("http close error",e);
            }
        }

    }
}
