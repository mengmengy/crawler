package org.gwu.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataBaseManager {
	protected static final Log log = LogFactory.getLog(DataBaseManager.class);
	private static DataBaseManager instance = null;
	private static DataSource ds;

	private DataBaseManager() {
	}

	public static DataBaseManager getInstance() {
		if (instance == null) {
			synchronized (DataBaseManager.class) {
				if (instance == null) {
					instance = new DataBaseManager();
				}
			}
		}
		// if (instance == null) {
		// try {
		// Thread.sleep((long)Math.random()*10000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// instance = new DataBaseManager();
		// }
		return instance;
	}

	static {
		try {
			Properties prop = new Properties();
			InputStream is = DataBaseManager.class.getClassLoader()
					.getResourceAsStream("dbcp.properties");
			prop.load(is);
			ds = BasicDataSourceFactory.createDataSource(prop);
			log.info("databasepool init success! idle="
					+ ((BasicDataSource) ds).getNumIdle());
		} catch (Exception e) {
			log.error("databasepool init error:" + e);
			e.printStackTrace();
			throw new ExceptionInInitializerError("databasepool init error!");
		}
	}

	/**
	 * get a connection from connection pool
	 * 
	 * @return connection
	 */
	public static Connection getConnection() {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			log.error("getConnection fail:", e);
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * release connection
	 * 
	 * @param conn
	 */
	public static void releaseConnection(Connection conn) {
		try {
			if (conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			log.error("releaseConnection fail:", e);
			e.printStackTrace();
		}
	}

	/**
	 * close statement
	 * 
	 * @param statement
	 * @param resultSet
	 */
	public static void closeStatement(Statement statement, ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				log.error("closeResultSet error:" + e);
				e.printStackTrace();
			}
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error("closeStatement error:" + e);
				e.printStackTrace();
			}
		}
	}

	// public static void main(String[] args){
	// for(int i=0;i<100;i++){
	// new Thread(new Runnable(){
	// @Override
	// public void run() {
	// System.out.println("the instance is"+DataBaseManager.getInstance());
	// }
	// }).start();
	// }
	// }
	
	/**
	 * general query approach
	 * @param sql
	 * @return
	 */
	public ResultSet executeQuery(String sql) {
		ResultSet rset = null;
		PreparedStatement stmt = null;
		Connection conn = getConnection();
		try {
			stmt=conn.prepareStatement(sql);
			rset=stmt.executeQuery();
		} catch (SQLException e) {
			log.error("executeQuery error:",e);
			e.printStackTrace();
		}finally{
			closeStatement(stmt,rset);
			releaseConnection(conn);
		}
		return rset;
	}
	
	/**
	 * general updateDB 
	 * @param sql
	 */
	public void updateDB(String sql){
		Connection conn=getConnection();
		PreparedStatement stmt = null;
		try {
			stmt=conn.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("updateDB error:",e);
			e.printStackTrace();
		}finally{
			closeStatement(stmt,null);
			releaseConnection(conn);
		}
	}
	
	public static void main(String[] args){
		Connection conn=getConnection();
		releaseConnection(conn);
		try {
			Thread.sleep(60*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
