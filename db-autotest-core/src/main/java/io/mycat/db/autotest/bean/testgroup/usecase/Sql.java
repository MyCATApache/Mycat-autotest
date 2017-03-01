package io.mycat.db.autotest.bean.testgroup.usecase;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import io.mycat.db.autotest.autoTestCheckPerformance.performance.PerfromanceUtils;
import io.mycat.db.autotest.autoTestCheckPerformance.tagServer.TagServerType;
import io.mycat.db.autotest.bean.AutoTestBaseBean;
import io.mycat.db.autotest.bean.AutoTestDataSource;
import io.mycat.db.autotest.bean.testgroup.TestGroupBaseBean;
import io.mycat.db.autotest.bean.testgroup.TestGroupTransaction;
import io.mycat.db.autotest.exception.AutoTestException;
import io.mycat.db.autotest.server.ioc.BeanFactory;
import io.mycat.db.autotest.utils.DataBaseUtils;
import io.mycat.db.autotest.utils.FileSqlUtils;
import io.mycat.db.autotest.utils.LogFrameFile;
import io.mycat.db.autotest.utils.PathUtils;
import org.apache.commons.io.IOUtils;

public class Sql extends AutoTestBaseBean implements TagServerType {
	
	private String url ;
	
	private String connection;

	private int count = 10;

	private String content;
	
	public Sql() {
		super(Arrays.asList("url","connection","count","content"),  "sql", null);
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getConnection() {
		return connection;
	}

	public Connection getConn(AutoTestDataSource autoTestDataSource) {
		String conn = getConnection();
		if(conn == null){
			if(autoTestDataSource.getConfig() != null){
				conn = autoTestDataSource.getConfig().getDefualutConnection();
			}
		}
		try {
			return autoTestDataSource.getConnection(conn);
		} catch (SQLException e) {
			LogFrameFile.getInstance().error("",e);
		}
		return null;
	}

	/*public Connection getConn(UseCase useCase) {
		String conn = getConnection();
		//UseCase useCase = getUseCase();
		if(conn == null){
			conn = useCase.getDefaultDataSource();
		}
		try {
			return useCase.getConnection(conn);
		} catch (SQLException e) {
			LogFrameFile.getInstance().error("",e);
		}
		return null;
	}*/

	public AutoTestBaseBean getUseCaseOrTestGroupBaseBean(AutoTestBaseBean atbbT){
		if(atbbT == null){
			atbbT = this;
		}
		AutoTestBaseBean atbb = BeanFactory.getBeanById(atbbT.getParentId());
		if(atbb instanceof UseCase || atbb instanceof TestGroupBaseBean){
			return (AutoTestBaseBean)atbb;
		}else{
			return getUseCaseOrTestGroupBaseBean(atbb);
		}
	}

	private AutoTestBaseBean getPerformanceOrTransactionOrInit(AutoTestBaseBean autoTestBaseBean){

		if(autoTestBaseBean instanceof Performance || autoTestBaseBean instanceof Transaction || autoTestBaseBean instanceof Init){
			return autoTestBaseBean;
		}else{
			return getPerformanceOrTransactionOrInit(BeanFactory.getBeanById(autoTestBaseBean.getParentId()));
		}
	}




	@Override
	public boolean exec() throws Exception {
		Sql sql = this;
		AutoTestBaseBean autoTestBaseBean = sql.getUseCaseOrTestGroupBaseBean(null);

		AutoTestBaseBean atbb = getPerformanceOrTransactionOrInit(this);
		if(autoTestBaseBean instanceof TestGroupBaseBean){
			TestGroupBaseBean testGroupBaseBean = (TestGroupBaseBean)autoTestBaseBean;


			List<String> sqls = null;
			if(content != null){
				sqls = FileSqlUtils.getSqls(content);
			}else{
				sqls = FileSqlUtils.getSqls(new File(PathUtils.getPath(testGroupBaseBean.getPath(),sql.getUrl())));
			}
			Connection conn = null;
			try {
				conn = sql.getConn((AutoTestDataSource) autoTestBaseBean);
				for (String s : sqls) {
					if (atbb instanceof Transaction) {
						DataBaseUtils.execSqlOpenTransaction(conn, s, true);
					}
				}
			}finally {
					if(conn != null){
						conn.close();
					}
			}
			return true;
		}
		UseCase useCase = (UseCase)autoTestBaseBean;
		Connection conn = null;
		if(atbb instanceof Init){
			try {
				conn = sql.getConn(useCase);
				List<String> sqls = null;
				if(content != null){
					sqls = FileSqlUtils.getSqls(content);
				}else{
					sqls = FileSqlUtils.getSqls(new File(PathUtils.getPath(useCase.doPath(),sql.getUrl())));
				}

				for (String s : sqls) {
					DataBaseUtils.execSqlOpenTransaction(conn,s,true);
				}
				return true;
			}finally {
				if(conn != null){
					conn.close();
				}
			}
		}else{
			//AutoTestBaseBean atbb = getPerformanceOrTransaction(this);
			try {
				conn = sql.getConn(useCase);
				List<String> sqls = null;
				if(content != null){
					sqls = FileSqlUtils.getSqls(content);
				}else{
					sqls = FileSqlUtils.getSqls(new File(PathUtils.getPath(useCase.doPath(),sql.getUrl())));
				}

				long time = 0;
				for (String s : sqls) {
					if(atbb instanceof Performance){
						//性能测试下 一般情况， sql只有一条
						PerfromanceUtils.check(getCount(),(Performance)atbb,useCase,conn,s);

					}else if(atbb instanceof Transaction){
						long start = System.currentTimeMillis();
						DataBaseUtils.execSqlOpenTransaction(conn,s,((Transaction)atbb).isAutoCommit());
						long end = System.currentTimeMillis();
						time = time + end - start;
					}
				}
				return true;
			}finally {
				if(atbb instanceof Transaction){
					if(!((Transaction)atbb).isAutoCommit()){
						if(conn != null){
							((Transaction)atbb).setConnectionBase(conn);
						}
					}else{
						if(conn != null){
							conn.close();
						}
					}
				} else {
					if(conn != null){
						conn.close();
					}
				}
			}
		}
		//return false;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
