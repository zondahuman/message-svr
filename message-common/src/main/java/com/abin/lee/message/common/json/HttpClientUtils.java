package com.abin.lee.message.common.json;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created with IntelliJ IDEA.
 * User: abin
 * Date: 16-4-18
 * Time: 上午10:24
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientUtils {
    private static CloseableHttpClient httpsClient = null;
    private static CloseableHttpClient httpClient = null;

    static {
        httpClient = HttpClients.createDefault();
        httpsClient = getHttpsClient();

    }

    public static CloseableHttpClient getHttpClient() {
        try {
            httpClient = HttpClients.createDefault();
            //首先设置全局的标准cookie策略
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
            httpClient = HttpClients.custom().setConnectionManager(PoolManager.getHttpPoolingManager()).setDefaultRequestConfig(requestConfig).build();
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
            //首先设置全局的标准cookie策略
            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
//            ConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(ctx);
            ConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(ctx, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", connectionSocketFactory).build();
            // 设置连接池
            httpsClient = HttpClients.custom().setConnectionManager(PoolManager.getHttpsPoolingManager(socketFactoryRegistry)).setDefaultRequestConfig(requestConfig).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpsClient;
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

        private int maxTotal = 50;

        private int defaultMaxPerRoute = 25;

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

        private static PoolManager poolManager = null;

        public synchronized static PoolManager getInstance() {
            if (poolManager == null) {
                clientConnectionManager = new PoolingHttpClientConnectionManager();
                poolManager = new PoolManager();
            }
            return poolManager;
        }
        public synchronized static PoolManager getInstance(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
            if (poolManager == null) {
                clientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                poolManager = new PoolManager();
            }
            return poolManager;
        }

        public synchronized static PoolManager getInstance(int maxTotal, int defaultMaxPerRoute) {
            if (poolManager == null) {
                poolManager = new PoolManager(maxTotal, defaultMaxPerRoute);
            }

            return poolManager;
        }

        public static HttpClientConnectionManager getHttpsPoolingManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
            if (clientConnectionManager == null) {
                clientConnectionManager = new PoolingHttpClientConnectionManager();
                getInstance(socketFactoryRegistry);
            }
            return clientConnectionManager;
        }
        public static HttpClientConnectionManager getHttpPoolingManager() {
            if (clientConnectionManager == null) {
                clientConnectionManager = new PoolingHttpClientConnectionManager();
                getInstance();
            }

            return clientConnectionManager;
        }
    }


}
