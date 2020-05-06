import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import util.HttpUtil;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.CommonUtil.sleep;
import static util.JDBCUtil.closeConn;
import static util.JDBCUtil.getConn;

/**
 * @Author: Avalon
 * @Date: 20/4/27 15:37
 * @Description: 全国温室数据系统
 */
public class sheshiyuanyi {

    static String URL = "http://data.sheshiyuanyi.com/WeatherData/php/stationsInf.php?area_name=%E5%85%A8%E5%9B%BD";
    static String getWeatherData = "http://data.sheshiyuanyi.com/WeatherData/php/getWeatherData.php?action=";

    public static void main(String[] args) {

        JSONObject jsonObject = JSONObject.parseObject(HttpUtil.httpGet(URL, "UTF-8"));
        JSONArray stations = jsonObject.getJSONArray("stations");

        for (int i = 0; i < stations.size(); i++) {
            JSONObject stationsInf = stations.getJSONObject(i);

            String sql = "insert into region(station_id, province_name, station_name, latitude, longitude, elevation) VALUES (?,?,?,?,?,?)";
            Connection conn = getConn();
            PreparedStatement pst = null;
            try {
                conn.setAutoCommit(false);
                pst = conn.prepareStatement(sql);

                pst.setString(1, stationsInf.getString("station_id"));
                pst.setString(2, stationsInf.getString("province_name"));
                pst.setString(3, stationsInf.getString("station_name"));
                pst.setString(4, stationsInf.getString("latitude"));
                pst.setString(5, stationsInf.getString("longitude"));
                pst.setString(6, stationsInf.getString("elevation"));

                pst.execute();
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeConn();
            }
        }

    }

    /**
     * 获取 weatherIndex
     *
     * @return
     */
    public static JSONObject weatherIndex() {
        File file = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\weatherIndex.json");

        String json = null;
        try {
            json = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSON.parseObject(json);
    }
}
