import bean.Region;
import bean.WeatherIndex;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import util.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static util.CommonUtil.gZip;
import static util.CommonUtil.sleep;
import static util.JDBCUtil.*;

/**
 * @Author: Avalon
 * @Date: 20/4/27 15:37
 * @Description: 全国温室数据系统
 */
public class sheshiyuanyi {

    static String URL = "http://data.sheshiyuanyi.com/WeatherData/php/stationsInf.php?area_name=%E5%85%A8%E5%9B%BD";
    static String getWeatherData = "http://data.sheshiyuanyi.com/WeatherData/php/getWeatherData.php?action=";

    public static void main(String[] args) {

        List<Map<String, String>> urlList = url();
        List<String> dataUrl = dataUrl();
        System.out.println(urlList.size());
        System.out.println(dataUrl.size());

        for (Map<String, String> m : urlList) {

            if (dataUrl.contains(m.get("url"))) {
                System.out.println("已采集");
            } else {
                try {

                    JSONObject jsonObject = JSONObject.parseObject(HttpUtil.httpGet(m.get("url"), "UTF-8"));
                    JSONArray jsonArray = jsonObject.getJSONArray(m.get("value"));

                    String sql = "insert into data(year, url, ids, value, sup_value, json) VALUES (?,?,?,?,?,?)";
                    Connection conn = getConn();
                    PreparedStatement pst = null;
                    try {
                        conn.setAutoCommit(false);
                        pst = conn.prepareStatement(sql);

                        pst.setString(1, m.get("year"));
                        pst.setString(2, m.get("url"));
                        pst.setString(3, m.get("ids"));
                        pst.setString(4, m.get("value"));
                        pst.setString(5, m.get("supValue"));
                        pst.setBytes(6, gZip(jsonArray.toString().getBytes()));

                        pst.execute();
                        conn.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        closeConn();
                    }
                    System.out.println(m.get("url"));
                    System.out.println(m.get("year") + " 年 站点：" + m.get("ids"));
                    System.out.println();
                    sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 生成 url
     *
     * @return
     */
    public static List<Map<String, String>> url() {
        List<Region> regionList = region();
        List<WeatherIndex> weatherIndices = weatherIndex();
        List<Map<String, String>> url = new ArrayList<>();
        for (int i = 0; i < regionList.size(); i = i + 19) {

            List<String> idList = new ArrayList<>();
            for (int j = i; j < i + 19; j++) {
                idList.add(String.valueOf(regionList.get(j).getStationId()));
            }

            for (WeatherIndex weatherIndex : weatherIndices) {
                String action = "more";
                if (Objects.equals(weatherIndex.getValue(), "accumulated_temperature")) {
                    action = "moreAccTem";
                }
                for (int j = 1984; j <= 2018; j++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("ids", String.join("-", idList));
                    map.put("year", String.valueOf(j));
                    map.put("value", String.join("-", weatherIndex.getValue()));
                    map.put("supValue", String.join("-", weatherIndex.getSubValue()));
                    map.put("url", getWeatherData + action + "&staNum=" + String.join("-", idList) + "&index=" + weatherIndex.getValue() + "&subIndex=" + weatherIndex.getSubValue() + "&year=" + j + "&month=0");
                    url.add(map);
                }
            }
        }
        return url;
    }

    /**
     * 获取 地区数据
     *
     * @return
     */
    public static List<Region> region() {
        List<Region> regionList = new ArrayList<>();
        ResultSet rs = execQuery("SELECT * FROM region");
        try {
            while (rs.next()) {
                Region region = new Region();
                region.setStationId(rs.getInt("station_id"));
                region.setProvinceName(rs.getString("province_name"));
                region.setStationName(rs.getString("station_name"));
                region.setLatitude(rs.getFloat("latitude"));
                region.setLongitude(rs.getFloat("longitude"));
                region.setElevation(rs.getInt("elevation"));
                regionList.add(region);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn();
        }
        return regionList;
    }

    /**
     * 获取 weatherIndex
     *
     * @return
     */
    public static List<WeatherIndex> weatherIndex() {
        File file = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\weatherIndex.json");

        String json = null;
        try {
            json = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = JSON.parseObject(json);
        String[] key = {"tem", "acc", "pre", "win", "ssd", "gst", "rhu", "prs", "evp", "sr"};
        List<WeatherIndex> weatherIndices = new ArrayList<>();
        for (String s : key) {
            JSONArray subIndex = jsonObject.getJSONObject(s).getJSONArray("subIndex");
            if (subIndex.size() > 0) {
                for (int i = 0; i < subIndex.size(); i++) {
                    WeatherIndex weatherIndex = new WeatherIndex();
                    weatherIndex.setValue(jsonObject.getJSONObject(s).getString("value"));
                    weatherIndex.setValueName(jsonObject.getJSONObject(s).getString("name"));
                    weatherIndex.setSubValue(subIndex.getJSONObject(i).getString("value"));
                    weatherIndex.setSubValueName(subIndex.getJSONObject(i).getString("name"));
                    weatherIndices.add(weatherIndex);
                }
            } else {
                WeatherIndex weatherIndex = new WeatherIndex();
                weatherIndex.setValue(jsonObject.getJSONObject(s).getString("value"));
                weatherIndex.setValueName(jsonObject.getJSONObject(s).getString("name"));
                weatherIndices.add(weatherIndex);
            }
        }
        return weatherIndices;
    }

    /**
     * 获取 data表里的 url
     *
     * @return
     */
    public static List<String> dataUrl() {
        List<String> list = new ArrayList<>();
        ResultSet rs = execQuery("SELECT url FROM data");
        try {
            while (rs.next()) {
                list.add(rs.getString("url"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn();
        }
        return list;
    }

}
