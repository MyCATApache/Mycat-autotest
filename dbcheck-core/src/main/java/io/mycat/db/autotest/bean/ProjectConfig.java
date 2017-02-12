package io.mycat.db.autotest.bean;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import io.mycat.db.autotest.bean.annotation.FieldType;
import io.mycat.db.autotest.bean.testgroup.Config;
import io.mycat.db.autotest.dataSource.UseDataSource;
import io.mycat.db.autotest.utils.DateUtil;
import org.apache.commons.io.FileUtils;

import javax.sql.DataSource;

public class ProjectConfig extends AutoTestBaseBean implements AutoTestDataSource{
	
	private String path="./";
	
	private Integer checkConcurrency = 100;
	
	private Integer performanceConcurrency = 20;
	
	/**
	 * 1不单独启动进程进行测试，2启动新的进程进行测试
	 */
	private int performanceType = 1;
	
	private String quartz;

	private String outPath;

	private String today;
	
	@FieldType(childName="connection",childType=Connection.class)
	private List<Connection> connections;

	private transient Map<String,DataSource> dataSources = new java.util.concurrent.ConcurrentHashMap<>();

	public ProjectConfig() {
		super(Arrays.asList("path","checkConcurrency","performanceConcurrency","quartz","outPath","connections"),  "projectConfig", Arrays.asList(Connection.class));
		today = DateUtil.getStrDatebyTobay();
	}

	public String getToday() {
		return today;
	}

	public void setToday(String today) {
		this.today = today;
	}

	public String getOutPath() {
		return outPath+"/"+today;
	}

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}

	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(Map<String, DataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getCheckConcurrency() {
		return checkConcurrency;
	}

	public void setCheckConcurrency(Integer checkConcurrency) {
		this.checkConcurrency = checkConcurrency;
	}

	public Integer getPerformanceConcurrency() {
		return performanceConcurrency;
	}

	public void setPerformanceConcurrency(Integer performanceConcurrency) {
		this.performanceConcurrency = performanceConcurrency;
	}

	public String getQuartz() {
		return quartz;
	}

	public void setQuartz(String quartz) {
		this.quartz = quartz;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	public int getPerformanceType() {
		return performanceType;
	}

	public void setPerformanceType(int performanceType) {
		this.performanceType = performanceType;
	}


	@Override
	public void initDataSource() {
		if(connections != null){
			for (Connection connection : connections) {
				dataSources.put(connection.getId(),UseDataSource.getDataSource(connection));
			}
		}
	}

	@Override
	public Config getConfig() {
		return null;
	}

	@Override
	public java.sql.Connection getConnection(String name) throws SQLException {
		DataSource dataSource = dataSources.get(name);
		if(dataSource == null){
			return null;
		}
		return dataSource.getConnection();
	}

	@Override
	public void close() throws Exception {
		Collection<DataSource> ls = dataSources.values();
		for (DataSource dataSource : ls) {
			try {
				((Closeable)dataSource).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

