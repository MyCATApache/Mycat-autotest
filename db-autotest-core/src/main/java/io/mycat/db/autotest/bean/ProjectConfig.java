package io.mycat.db.autotest.bean;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import io.mycat.db.autotest.bean.annotation.FieldType;
import io.mycat.db.autotest.bean.testgroup.Config;
import io.mycat.db.autotest.dataSource.UseDataSource;
import io.mycat.db.autotest.utils.DateUtil;
import io.mycat.db.autotest.utils.LogFrameFile;
import org.apache.commons.io.FileUtils;

import javax.sql.DataSource;

public class ProjectConfig extends AutoTestBaseBean implements AutoTestDataSource,UseCaseLocalPath{
	
	private String path="./";
	
	private Integer checkConcurrency = 100;
	
	//private Integer performanceConcurrency = 20;
	
	/**
	 * 1不单独启动进程进行测试，2启动新的进程进行测试
	 */
	private int performanceType = 1;
	
	private String quartz;

	private String outPath;

	private String todayTime;

	private String performanceExec = "io.mycat.db.autotest.autoTestCheckPerformance.performance.exec.DefaultPerformanceExec";
	
	@FieldType(childName="connection",childType=Connection.class)
	private List<Connection> connections;

	private transient Map<String,DataSource> dataSources = new java.util.concurrent.ConcurrentHashMap<>();

	public ProjectConfig() {
		super(Arrays.asList("path","checkConcurrency","performanceConcurrency","quartz","outPath","connections"),  "projectConfig", Arrays.asList(Connection.class));
		todayTime = DateUtil.getStrDatebyTobayTime();
	}

	public String getTodayTime() {
		return todayTime;
	}

	public void setTodayTime(String todayTime) {
		this.todayTime = todayTime;
	}

	public String getOutPath() {
		return outPath+"/"+todayTime;
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

	/*public Integer getPerformanceConcurrency() {
		return performanceConcurrency;
	}

	public void setPerformanceConcurrency(Integer performanceConcurrency) {
		this.performanceConcurrency = performanceConcurrency;
	}*/

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

	public String getPerformanceExec() {
		return performanceExec;
	}

	public void setPerformanceExec(String performanceExec) {
		this.performanceExec = performanceExec;
	}

	@Override
	public void initDataSource() {
		if(dataSources == null){
			dataSources = new java.util.concurrent.ConcurrentHashMap<>();
		}
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
				LogFrameFile.getInstance().error("",e);
				throw e;
			}
		}
	}
}

