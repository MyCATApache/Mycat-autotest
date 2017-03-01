package io.mycat.db.autotest.bean;

import io.mycat.db.autotest.bean.testgroup.Config;

import java.sql.SQLException;

/**
 * Created by qiank on 2017/1/25.
 */
public interface AutoTestDataSource {

    void initDataSource();

    Config getConfig();

    java.sql.Connection getConnection(String name) throws SQLException;

    public void close() throws Exception;
}
