import bean.Datas;
import bean.Region;
import bean.WeatherIndex;
import com.alibaba.fastjson.JSONArray;
import util.CommonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static util.CommonUtil.sleep;
import static util.JDBCUtil.*;

/**
 * @Author: Avalon
 * @Date: 20/5/13 14:26
 * @Description:
 */
public class dataHandling {
    public static void main(String[] args) {

        List<Region> regionList = sheshiyuanyi.region();
        List<WeatherIndex> weatherIndices = sheshiyuanyi.weatherIndex();

        for (int i = 0; i < regionList.size(); i = i + 19) {

            List<String> idList = new ArrayList<>();
            List<String> nameList = new ArrayList<>();
            for (int j = i; j < i + 19; j++) {
                idList.add(String.valueOf(regionList.get(j).getStationId()));
                nameList.add(String.valueOf(regionList.get(j).getStationName()));
            }

            System.out.println(String.join("-", idList));
            List<Datas> datasList = getData(String.join("-", idList));

            for (Datas datas : datasList) {
                System.out.println(datas);
                JSONArray jsonArray = JSONArray.parseArray(new String(CommonUtil.unGZip(datas.getJson())));

                for (int j = 0; j < 19; j++) {
//                    System.out.println(idList.get(j));
                    JSONArray jsonArray1 = new JSONArray();
                    for (int k = 0; k < jsonArray.size(); k++) {
                        if (Objects.equals(jsonArray.getJSONObject(k).getString("station_id"), idList.get(j))) {
                            jsonArray1.add(jsonArray.getJSONObject(k));
                        }
                    }

                    String valueName = "";
                    String subValueName = "";
                    for (WeatherIndex weatherIndex:weatherIndices){

                        if (Objects.equals(datas.getValue(), "accumulated_temperature")){
                            valueName = "积温";
                        }
                        if (Objects.equals(weatherIndex.getValue(), datas.getValue()) && Objects.equals(weatherIndex.getSubValue(), datas.getSupValue())){
                            valueName = weatherIndex.getValueName();
                            subValueName = weatherIndex.getSubValueName();
                        }
                    }

                    String sql = "insert into weather_data(station_id, year, value, sub_value, json, station_name, value_name, sub_value_name) value (?,?,?,?,?,?,?,?)";
                    Connection conn = getConn();
                    PreparedStatement pst = null;
                    try {
                        conn.setAutoCommit(false);
                        pst = conn.prepareStatement(sql);

                        pst.setInt(1, Integer.parseInt(idList.get(j)));
                        pst.setInt(2, Integer.parseInt(datas.getYear()));
                        pst.setString(3, datas.getValue());
                        pst.setString(4, datas.getSupValue());
                        pst.setString(5, jsonArray1.toString());
                        pst.setString(6, nameList.get(j));
                        pst.setString(7, valueName);
                        pst.setString(8, subValueName);

                        pst.execute();
                        conn.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        closeConn();
                    }

                    sleep(10);
                }
            }
        }

    }

    public static List<Datas> getData(String ids) {
        List<Datas> datasList = new ArrayList<>();
        String sql = "SELECT * FROM data where ids = \"" + ids + "\"";
        ResultSet rs = execQuery(sql);
        try {
            while (rs.next()) {
                Datas datas = new Datas();
                datas.setIds(rs.getString("ids"));
                datas.setUrl(rs.getString("url"));
                datas.setYear(rs.getString("year"));
                datas.setValue(rs.getString("value"));
                datas.setSupValue(rs.getString("sup_value"));
                datas.setJson(rs.getBytes("json"));
                datasList.add(datas);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn();
        }
        return datasList;
    }
}
