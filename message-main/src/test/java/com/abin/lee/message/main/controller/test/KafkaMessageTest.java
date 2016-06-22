package com.abin.lee.message.main.controller.test;

import com.abin.lee.message.common.json.HttpClientUtil;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class KafkaMessageTest {
    private static final String httpURL = "http://localhost:9000/kafka/producer";

    @Test
    public void testKafkaMessage() throws IOException {
        CloseableHttpClient httpclient = HttpClientUtil.getHttpClient();
        try {
            HttpPost httpPost = new HttpPost(httpURL);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("messageId", "100000000000"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

            // httpPost.addHeader("Content-type","application/json; charset=utf-8");
            // httpPost.setHeader("Accept", "application/json");
            // httpPost.setEntity(new StringEntity(parameters,
            // Charset.forName("UTF-8")));

            System.out.println("Executing request: " + httpPost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpPost);
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpclient.close();
        }
    }

}
