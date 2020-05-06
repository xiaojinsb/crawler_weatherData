import bean.Region;
import bean.WeatherIndex;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static util.JDBCUtil.closeConn;
import static util.JDBCUtil.execQuery;

/**
 * @Author: Avalon
 * @Date: 20/4/27 15:37
 * @Description: 全国温室数据系统
 */
public class sheshiyuanyi {

    static String URL = "http://data.sheshiyuanyi.com/WeatherData/php/stationsInf.php?area_name=%E5%85%A8%E5%9B%BD";
    static String getWeatherData = "http://data.sheshiyuanyi.com/WeatherData/php/getWeatherData.php?action=";

    public static void main(String[] args) {
        List<Region> regionList = region();
        List<WeatherIndex> weatherIndices = weatherIndex();
        for (int i = 0; i < regionList.size(); i++) {
            int id = regionList.get(i).getStationId();
            System.out.println("id: " + id);

            for (WeatherIndex weatherIndex : weatherIndices) {

                for (int j = 1984; j <= 2018; j++) {
                    System.out.println(j);
                    System.out.println(getWeatherData+id);
                }
            }
        }


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
            WeatherIndex weatherIndex = new WeatherIndex();
            weatherIndex.setValue(jsonObject.getJSONObject(s).getString("value"));
            JSONArray subIndex = jsonObject.getJSONObject(s).getJSONArray("subIndex");
            if (subIndex.size() > 0) {
                for (int i = 0; i < subIndex.size(); i++) {
                    weatherIndex.setSubValue(subIndex.getJSONObject(i).getString("value"));
                    weatherIndices.add(weatherIndex);
                }
            } else {
                weatherIndices.add(weatherIndex);
            }
        }
        return weatherIndices;
    }
}
