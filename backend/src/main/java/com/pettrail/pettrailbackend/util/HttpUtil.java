package com.pettrail.pettrailbackend.util;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * @program: pet-trail
 * @description: HTTP 工具类
 * @author: zsf
 * @create: 2026-03-27 13:38
 **/
public class HttpUtil {

    // 静态初始化块，配置信任所有 SSL 证书
    static {
        trustAllCertificates();
    }

    /**
     * 配置信任所有 SSL 证书
     */
    private static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            System.err.println("警告: SSL 证书信任配置失败: " + e.getMessage());
        }
    }

    /**
     * 发送 GET 请求
     *
     * @param url 请求地址
     * @return 响应结果的字符串
     * @throws IOException 网络异常时抛出
     */
    public static String doGet(String url) throws IOException {
        URL realUrl = new URL(url);
        // 打开和 URL 之间的连接
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();

        // 设置通用的请求属性
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 连接超时时间 5 秒
        connection.setReadTimeout(5000);    // 读取超时时间 5 秒
        connection.setDoInput(true); // 允许输入
        connection.setDoOutput(false); // GET 请求不允许输出

        // 建立实际的连接
        connection.connect();

        // 定义 BufferedReader 输入流来读取 URL 的响应
        StringBuilder result = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        }

        // 断开连接
        connection.disconnect();

        return result.toString();
    }


}
