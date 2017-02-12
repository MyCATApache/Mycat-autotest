package io.mycat.db.autotest.dataSource;

import io.mycat.db.autotest.bean.Connection;
import io.mycat.db.autotest.dataSource.factory.DruidDataSourceFactory;

import javax.sql.DataSource;

/**
 * Created by qiank on 2017/1/25.
 */
public class UseDataSource {

    public static DataSource getDataSource(Connection conn){
        switch (conn.getDataSourceClass()){
            case "druid":
                return DruidDataSourceFactory.getDataSource(conn);
            default:
                return null;
        }
    }

}
