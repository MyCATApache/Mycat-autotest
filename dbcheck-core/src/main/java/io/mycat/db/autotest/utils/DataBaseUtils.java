package io.mycat.db.autotest.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by qiank on 2017/2/1.
 */
public class DataBaseUtils {

    public static boolean execSqlOpenTransaction(Connection conn,String sql,boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
        if(!autoCommit){
            conn.getTransactionIsolation();
        }
        return execSql(conn,sql);
    }

    public static boolean execSql(Connection conn,String sql) throws SQLException {
        System.out.println(sql);
        PreparedStatement stat = conn.prepareStatement(sql);
        return stat.execute();
    }

    public static boolean rollback(Connection conn){
        try {
            conn.rollback();
        } catch (SQLException e) {
            LogFrameFile.getInstance().error("",e);
            return false;
        }
       return true;
    }

    public static boolean commit(Connection conn){
        try {
            conn.commit();
        } catch (SQLException e) {
            LogFrameFile.getInstance().error("",e);
            return false;
        }
        return true;
    }
}
