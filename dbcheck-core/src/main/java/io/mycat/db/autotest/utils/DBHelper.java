package io.mycat.db.autotest.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 * 数据库操作工具类，提供�?用的操作数据库方法，每次调用完毕后必�?��用close()关闭链接
 * 
 * @author Liqiang4j
 * @version 1.0
 */
public class DBHelper {

	private Connection conn = null;

	private Statement stmt = null;

	public DBHelper(Connection conn) {
		this.conn = conn;
}

	/**
	 * 获取数据库连�?
	 * 
	 * @return Connection
	 */
	public Connection getConnection() {
		return this.conn;
	}

	/**
	 * 获取 Statement 对象
	 * 
	 * @return Statement
	 * @throws SQLException
	 */
	public Statement getStatement() throws SQLException {
		if (conn == null) {
			conn = getConnection();
		}
		stmt = conn.createStatement();
		return stmt;
	}

	/**
	 * 返回预定义的执行语句
	 * 
	 * @param sql
	 *            SQL语句
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		if (conn == null) {
			conn = getConnection();
		}
		stmt = conn.prepareStatement(sql);
		return (PreparedStatement) stmt;
	}

	/**
	 * 返回执行存储过程的执行语�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return CallableStatement
	 * @throws SQLException
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		if (conn == null) {
			conn = getConnection();
		}
		stmt = conn.prepareCall(sql);
		return (CallableStatement) stmt;
	}

	/**
	 * 执行查询
	 * 
	 * @param sql
	 *            SQL语句
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		if (conn == null) {
			conn = getConnection();
		}
		if (stmt == null) {
			stmt = getStatement();
		}
		return stmt.executeQuery(sql);
	}

	/**
	 * 执行更新
	 * 
	 * @param sql
	 * @return int
	 * @throws SQLException
	 */
	public int executeUpdate(String sql) throws SQLException {
		int rowCount = 0;
		if (conn == null) {
			conn = getConnection();
		}
		if (stmt == null) {
			stmt = getStatement();
		}
		rowCount = stmt.executeUpdate(sql);
		return rowCount;
	}

	/**
	 * 关闭数据库连�?
	 * 
	 * @throws SQLException
	 */
	public void close() {
		if (conn != null) {
			try {
				if (!conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �?��事务
	 * 
	 * @throws Exception
	 */
	public void startTransaction() {
		if (conn != null) {
			try {
				if (conn.getAutoCommit())
					conn.setAutoCommit(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 事务回滚
	 */
	public void rollbackTransaction() {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 提交事务但不关闭连接
	 */
	public void commitTransaction() {
		if (conn != null) {
			try {
				conn.commit();
				if (!conn.getAutoCommit())
					conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 提交事务并关闭连�?
	 */
	public void commitAndCloseTransaction() {
		if (conn != null) {
			try {
				conn.commit();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 执行查询语句，返回list查询结果,无参数，list中的数据是Map类型�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return List
	 */
	public List queryForListMap(String sql) throws SQLException {
		return queryForListMap(sql, new Object[] {});
	}

	/**
	 * 执行查询语句，返回list查询结果，有参数，list中的数据是Map类型，在获取数据时Map的key要全部大写�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组
	 * @return List
	 * @throws SQLException
	 */
	public List queryForListMap(String sql, Object[] params)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		ResultSetHandler rsh = new MapListHandler();
		return  (List) qr.query(this.conn, sql, params, rsh);
	}

	/**
	 * 执行查询语句，返回list查询结果，有参数，list中的数据是Object[]类型�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param parmas
	 *            参数数组
	 * @return List
	 * @throws SQLException
	 */
	public List<?> queryForList(String sql, Object[] parmas)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		ResultSetHandler rsh = new ArrayListHandler();
		return (ArrayList<?>) qr.query(this.conn, sql, parmas, rsh);
	}
	
	/**
	 * 执行查询语句，返回list查询结果，没有参数，list中的数据是Object[]类型�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return List
	 * @throws SQLException
	 */
	public List<?> queryForList(String sql) throws SQLException {
		return queryForList(sql, new Object[] {});
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据，数据类型是HashMap�?
	 * 在查询语句没有参数的情况下调用此方法获取单条数据，在获取数据时Map的key要全部大写�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return HashMap
	 * @throws SQLException
	 */
	public Map<?, ?> queryForMap(String sql) throws SQLException {
		return queryForMap(sql, new Object[] {});
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据，数据类型是HashMap�?
	 * 在查询语句有参数的情况下调用此方法获取单条数据，在获取数据时Map的key要全部大写�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组
	 * @return HashMap
	 * @throws SQLException
	 */
	public Map<?, ?> queryForMap(String sql, Object[] params)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		ResultSetHandler rsh = new MapHandler();
		return (HashMap<?, ?>) qr.query(conn, sql, params, rsh);
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是Object�?在查询语句有参数的情况下调用此方法获取单条数据�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组
	 * @return Object
	 * @throws SQLException
	 */
	public Object queryForObject(String sql, Object[] params)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		ResultSetHandler rsh = new ArrayHandler();
		Object obj = null;
		Object[] objs = (Object[]) qr.query(conn, sql, params, rsh);
		if (objs != null && objs.length > 0)
			obj = objs[0];
		return obj;
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是Object�?在查询语句没有参数的情况下调用此方法获取单条数据�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return Object
	 * @throws SQLException
	 */
	public Object queryForObject(String sql) throws SQLException {
		return queryForObject(sql, new Object[] {});
	}

	/**
	 * 执行insert,update,delete语句，�?用于没有参数的更新�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return int 数据改变的行�?
	 * @throws SQLException
	 */
	public int update(String sql) throws SQLException {
		return update(sql, new Object[] {});
	}

	/**
	 * 执行insert,update,delete语句，�?用于有参数的更新�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组
	 * @return int 数据改变的行�?
	 * @throws SQLException
	 */
	public int update(String sql, Object[] params) throws SQLException {
		QueryRunner qr = new QueryRunner();
		return qr.update(conn, sql, params);
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是int�?在查询语句有参数的情况下调用此方法获取单条数据�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组
	 * @return Integer
	 */
	public Integer queryForInt(String sql, Object[] params) {
		String s = queryForString(sql, params);
		return s != null ? Integer.parseInt(s) : null;
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是int�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return Integer
	 */
	public Integer queryForInt(String sql) {
		return queryForInt(sql, new Object[] {});
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是long，在查询语句有参数的情况下调用此方法获取单条数据�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组
	 * @return Long
	 */
	public Long queryForLong(String sql, Object[] params) {
		String s = queryForString(sql, params);
		return s != null ? Long.parseLong(s) : null;
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是long�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return Long
	 */
	public Long queryForLong(String sql) {
		return queryForLong(sql, new Object[] {});
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是String，在查询语句有参数的情况下调用此方法获取单条数据�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return String
	 */
	public String queryForString(String sql) {
		return queryForString(sql, new Object[] {});
	}

	/**
	 * 执行查询语句，返回查询结果的第一条数据的第一行第�?��，数据类型是String�?
	 * 
	 * @param sql
	 *            SQL语句
	 * @return String
	 * @throws SQLException
	 */
	public String queryForString(String sql, Object[] params) {
		QueryRunner qr = new QueryRunner();
		ResultSetHandler rsh = new ScalarHandler(1);// 第一�?
		Object obj = null;
		try {
			obj = qr.query(conn, sql, params, rsh);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
		return obj != null ? obj.toString() : null;
	}

}