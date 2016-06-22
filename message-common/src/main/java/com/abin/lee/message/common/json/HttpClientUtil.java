package com.abin.lee.message.common.json;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created with IntelliJ IDEA.
 * User: abin
 * Date: 16-4-18
 * Time: 上午10:24
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientUtil {
    private static CloseableHttpClient httpsClient = null;
    private static CloseableHttpClient httpClient = null;

    static {
        httpClient = getHttpClient();
        httpsClient = getHttpsClient();
    }

    public static CloseableHttpClient getHttpClient() {
        try {
            httpClient = HttpClients.createDefault();
            httpClient = HttpClients.custom().setConnectionManager(PoolManager.getHttpPoolingManager())
                    .setConnectionManagerShared(true)
                    .setDefaultRequestConfig(requestConfig())
                    .setRetryHandler(requestHandler())
                    .build();
            System.out.println("getAvailable="+ PoolManager.clientConnectionManager.getTotalStats().getAvailable());
            System.out.println("getPending="+ PoolManager.clientConnectionManager.getTotalStats().getPending());
            System.out.println("getMax="+ PoolManager.clientConnectionManager.getTotalStats().getMax());
            System.out.println("getLeased="+ PoolManager.clientConnectionManager.getTotalStats().getLeased());
            System.out.println("getValidateAfterInactivity="+ PoolManager.clientConnectionManager.getValidateAfterInactivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpClient;
    }


    public static CloseableHttpClient getHttpsClient() {
        try {
            //Secure Protocol implementation.
            SSLContext ctx = SSLContext.getInstance("SSL");
            //Implementation of a trust manager for X509 certificates
            TrustManager x509TrustManager = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs,
                                               String string) throws CertificateException {
                }
                public void checkServerTrusted(X509Certificate[] xcs,
                                               String string) throws CertificateException {
                }
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{x509TrustManager}, null);
            ConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(ctx, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", connectionSocketFactory).build();
            // 设置连接池
            httpsClient = HttpClients.custom().setConnectionManager(PoolManager.getHttpsPoolingManager(socketFactoryRegistry))
                    .setConnectionManagerShared(true)
                    .setDefaultRequestConfig(requestConfig())
                    .setRetryHandler(requestHandler())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpsClient;
    }


    /**
     * 请求重试处理,httpoClient默认三次
     * @return
     */
    public static HttpRequestRetryHandler requestHandler(){
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,int executionCount, HttpContext context) {
                if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// ssl握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
        return  httpRequestRetryHandler;
    }

    /**
     * 设置全局的标准cookie策略,超时处理
     * @return
     */
    public static RequestConfig requestConfig(){
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
                .setConnectionRequestTimeout(20000)
                .setConnectTimeout(20000)
                .setSocketTimeout(20000)
                .build();
        return requestConfig;
    }



    //创建HostnameVerifier
    //用于解决javax.net.ssl.SSLException: hostname in certificate didn't match: <123.125.97.66> != <123.125.97.241>
    static final HostnameVerifier hostnameVerifier = new HostnameVerifier(){
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return false;
        }
    };


    public static class PoolManager {
        public static PoolingHttpClientConnectionManager clientConnectionManager = null;

        private static int maxTotal = 200;

        private static int defaultMaxPerRoute = 50;

        private PoolManager(int maxTotal, int defaultMaxPerRoute) {
            this.maxTotal = maxTotal;
            this.defaultMaxPerRoute = defaultMaxPerRoute;
            clientConnectionManager.setMaxTotal(maxTotal);
            clientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        }

        private PoolManager() {
            clientConnectionManager.setMaxTotal(maxTotal);
            clientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        }

        private static class PoolManagerHolder{
            private static PoolManager instance = new PoolManager();
        }

        public static PoolManager getInstance(){
             return PoolManagerHolder.instance;
        }

        public static HttpClientConnectionManager getHttpsPoolingManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
            if (clientConnectionManager == null) {
                clientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                PoolManager.getInstance();
            }
            return clientConnectionManager;
        }

        public static HttpClientConnectionManager getHttpPoolingManager() {
            if (clientConnectionManager == null) {
                clientConnectionManager = new PoolingHttpClientConnectionManager();
                PoolManager.getInstance();
                System.out.println("getAvailable=" + clientConnectionManager.getTotalStats().getAvailable());
                System.out.println("getLeased=" + clientConnectionManager.getTotalStats().getLeased());
                System.out.println("getMax=" + clientConnectionManager.getTotalStats().getMax());
                System.out.println("getPending="+clientConnectionManager.getTotalStats().getPending());
            }
            return clientConnectionManager;
        }

    }


}
