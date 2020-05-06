package util;

import java.sql.*;

/**
 * @Author: Avalon
 * @Date: 20/4/23 11:40
 * @Description: JDBC
 */
public class JDBCUtil {

    //三大核心接口
    private static Connection conn = null;
    private static PreparedStatement pst = null;
    private static ResultSet rs = null;

    /**
     * 创建连接
     *
     * @return conn
     */
    public static Connection getConn() {
        String clazz = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/crawler_weatherData?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "root";
        try {
            Class.forName(clazz);
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭连接
     */
    public static void closeConn() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 专门用于发送查询语句
     *
     * @param sql
     * @return rs
     */
    public static ResultSet execQuery(String sql) {
        try {
            conn = getConn();
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}