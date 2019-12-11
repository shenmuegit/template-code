package dataSource;

import bean.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtil {

    private static Connection conn = null;

    private static void initConn(String driver, String url, String username, String password){
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Table> getTables(String driver,String url,String username,String password,String table){
        initConn(driver,url,username,password);
        try {
            return selectTable(table);
        } finally {
            if(null != conn){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Table> selectTable(String table) {
        String sql = "select column_name,column_comment,data_type " +
                "from information_schema.columns " +
                "where table_name = ? and table_schema='calabash'";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Table> tableList = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,table);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Table tab = new Table();
                tab.setColumnName(rs.getString(1));
                tab.setColumnComment(rs.getString(2));
                tab.setDataType(rs.getString(3));
                tableList.add(tab);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != rs){
                    rs.close();
                }
                if(null != pstmt){
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tableList;
    }
}
