package io.mycat.db.autotest.bean;


import java.util.Arrays;
import java.util.List;


/**
* @ClassName: Connection 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author zjliu qiankunshe@126.com
* @date 2017年1月16日 下午1:38:09 
 */
public class Connection extends AutoTestBaseBean {
	
	
	private String host;
	
	private Integer post;
	
	private String username;
	
	private String password;
	
	private String driver = "com.mysql.jdbc.Driver";// oracle "oracle.jdbc.driver.OracleDriver" sqlserver "com.microsoft.sqlserver.jdbc.SQLServerDriver" 

	private String url = "jdbc:mysql://${ip}:${post}/${pid}?useUnicode=true&characterEncoding=UTF-8";// oracle "jdbc:oracle:thin:@${ip}:${post}:${pid}"  sqlserver "jdbc:sqlserver://@${ip}:${post}; DatabaseName=${pid}"
	
	private String dataSourceClass = "druid";

	private String database;

	public Connection(List<String> fields, String tagName, List<Class<? extends AutoTestBaseBean>> loadChildFields, String host, Integer post, String username, String password, String driver, String url, String dataSourceClass, String database) {
		super(fields, tagName, loadChildFields);
		this.host = host;
		this.post = post;
		this.username = username;
		this.password = password;
		this.driver = driver;
		this.url = url;
		this.dataSourceClass = dataSourceClass;
		this.database = database;
	}

	public Connection(List<String> fields, String tagName, List<Class<? extends AutoTestBaseBean>> loadChildFields) {
		super(fields, tagName, loadChildFields);
	}

	public Connection() {
		super(Arrays.asList("host","post","username","password","driver","url"),  "connection", null);
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPost() {
		return post;
	}

	public void setPost(Integer post) {
		this.post = post;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDataSourceClass() {
		return dataSourceClass;
	}

	public void setDataSourceClass(String dataSourceClass) {
		this.dataSourceClass = dataSourceClass;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
