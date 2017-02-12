package io.mycat.db.autotest.dataSource.factory;

import com.alibaba.druid.pool.DruidDataSource;
import io.mycat.db.autotest.bean.Connection;

/**
 * Created by qiank on 2017/1/25.
 */
public class DruidDataSourceFactory {

    public static DruidDataSource getDataSource(Connection conn){
        String url = conn.getUrl();
        url = url.replace("${ip}",conn.getHost());
        url = url.replace("${post}",conn.getPost()+"");
        url = url.replace("${pid}",conn.getDatabase());

        DruidDataSource ds = new DruidDataSource();

        ds.setUrl(url);
        ds.setUsername(conn.getUsername());
        ds.setPassword(conn.getPassword());

        return ds;
        /*if (driverClass != null)
            ds.setDriverClassName(driverClass);
        ds.setInitialSize(initialSize);
        ds.setMinIdle(minIdle);
        ds.setMaxActive(maxActive);
        ds.setMaxWait(maxWait);
        ds.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
        ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

        ds.setValidationQuery(validationQuery);
        ds.setTestWhileIdle(testWhileIdle);
        ds.setTestOnBorrow(testOnBorrow);
        ds.setTestOnReturn(testOnReturn);

        ds.setRemoveAbandoned(removeAbandoned);
        ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
        ds.setLogAbandoned(logAbandoned);

        //只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，参照druid的源码
        ds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);*/
    }
}
