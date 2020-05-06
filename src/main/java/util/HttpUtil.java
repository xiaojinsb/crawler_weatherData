package util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;

/**
 * @Author: Avalon
 * @Date: 20/4/13 15:49
 * @Description: http工具类
 */
public class HttpUtil {
    /**
     * get封装
     *
     * @param url
     * @param charset
     * @return html
     */
    public static String httpGet(String url, String charset) {
        String content = null;

        // 获得Http客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建Get请求
        HttpGet httpGet = new HttpGet(url);
        // 添加头信息
        addHeader(httpGet, url);
        // 响应模型
        CloseableHttpResponse response = null;

        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 判断请求数据是否存在
            if (response != null && response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 200) {
                // 从响应模型中获取响应实体
                HttpEntity entity = response.getEntity();
                content = EntityUtils.toString(entity, charset);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return content;
    }

    /**
     * 下载文件
     *
     * @param url
     * @param dirPath
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String download(String url, String dirPath, String filePath) throws Exception {
        String result = null;
        // 获得Http客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建Get请求
        HttpGet httpGet = new HttpGet(url);
        // 添加头信息
        addHeader(httpGet, url);
        // 响应模型
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(httpGet);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                InputStream in = entity.getContent();
                savePicToDisk(in, dirPath, filePath);
                in.close();
                System.out.println("保存文件 " + filePath + " 成功....");
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            response.close();
            httpGet.releaseConnection();
        }
        return result;
    }

    /**
     * 将图片写到 硬盘指定目录下
     *
     * @param in
     * @param dirPath
     * @param filePath
     */
    private static void savePicToDisk(InputStream in, String dirPath, String filePath) {
        BufferedOutputStream bos = null;
        try {
            File dir = new File(dirPath);
            if (dir == null || !dir.exists()) {
                dir.mkdirs();
            }

            // 文件真实路径
            String realPath = dirPath.concat(filePath);
            File file = new File(realPath);
            if (file == null || !file.exists()) {
                file.createNewFile();
            }
            bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[10240];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Header头部信息
     *
     * @param http
     * @param referer
     */
    public static void addHeader(HttpMessage http, String referer) {
        http.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        http.addHeader("Accept-Encoding", "gzip, deflate");
        http.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
        http.addHeader("Cache-Control", "no-cache");
        http.addHeader("Referer", "http://www.tobst.cn/");
        http.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
    }

}
