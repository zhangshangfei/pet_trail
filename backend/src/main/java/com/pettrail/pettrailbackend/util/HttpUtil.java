package com.pettrail.pettrailbackend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @program: pet-trail
 * @description:
 * @author: zsf
 * @create: 2026-03-27 13:38
 **/
public class HttpUtil {

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
