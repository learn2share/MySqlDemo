package com.yuan.mysqldemo;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DBOpenHelper {

    private static String driver = "org.postgresql.Driver";//postgresql 驱动
    private static String url = "jdbc:postgresql://120.220.27.189:54321/postgres";//postgresql数据库连接Url
    private static String user = "postgres";//用户名
    private static String password = "cmcc2017";//密码

    /**
     * 连接数据库
     */

    public static Connection getConn() {
        Connection conn = null;
        try {
            Class.forName(driver);//获取postgresql驱动
            conn = (Connection) DriverManager.getConnection(url, user, password);//获取连接
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭数据库
     */
    public static void closeAll(Connection conn, PreparedStatement ps) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭数据库
     */
    public static void closeAll(Connection conn, PreparedStatement ps, ResultSet rs) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean execSQL(Connection conn, String sql) {
        boolean execResult = false;
        if (conn == null) {
            return execResult;
        }

        Statement statement = null;

        try {
            statement = conn.createStatement();
            if (statement != null) {
                statement.execute(sql);
                execResult = true;
            }
        } catch (SQLException e) {
            execResult = false;
        }

        return execResult;
    }

    public static HashMap<String, Object> getInfoByDeviceId(Connection connection, String deviceId) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            String sql = "select * from device_info where device_id = ?";
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null) {
                    // 设置上面的sql语句中的？的值为deviceId
                    ps.setString(1, deviceId);
                    // 执行sql查询语句并返回结果集
                    ResultSet rs = ps.executeQuery();
                    if (rs != null) {
                        int count = rs.getMetaData().getColumnCount();
                        Log.e("DBOpenHelper", "列总数：" + count);
                        while (rs.next()) {
                            // 注意：下标是从1开始的
                            for (int i = 1; i <= count; i++) {
                                String field = rs.getMetaData().getColumnName(i);
                                map.put(field, rs.getString(field));
                            }
                        }
//                        connection.close();
                        ps.close();
                        return map;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBOpenHelper", "异常：" + e.getMessage());
            return null;
        }

    }
}
