package com.ibm.issw.jdbc.profiler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.commerce.cache.SingleJdbcConnection;
import com.ibm.commerce.cache.SingleJdbcDataSource;
import com.ibm.issw.jdbc.wrappers.WrappedConnection;
import com.ibm.logger.PerformanceLogger;
import com.ibm.logger.jmx.TimeIntervalLogEntryMXBean;

public class JdbcProfilerTest {

	private static final String CREATE_UNIQUECACHEHITS_TABLE_SQL = "CREATE TABLE UNIQUECACHEHITS ("
			+ "AVERAGEDURATION   BIGINT NOT NULL,"
			+ "OPERATIONNAME     VARCHAR(100) NOT NULL,"
			+ "CONSTRAINT PK_UNIQUECACHEHITS PRIMARY KEY (OPERATIONNAME)" + ")";

	/**
	 * Clear the UNIQUECACHEHITS table SQL.
	 */
	private static final String CLEAR_UNIQUECACHEHITS_TABLE_SQL = "delete from UNIQUECACHEHITS";

	/**
	 * Drop the UNIQUECACHEHITS table SQL.
	 */
	private static final String DROP_UNIQUECACHEHITS_TABLE_SQL = "drop table UNIQUECACHEHITS";

	private static final String SELECT_SQL = "select * from UNIQUECACHEHITS";

	private static final String INSERT_SQL = "INSERT INTO UNIQUECACHEHITS "
			+ "(OPERATIONNAME, AVERAGEDURATION)" + " VALUES (?,?) ";

	private static WrappedConnection wrappedConnection;

	private static SingleJdbcDataSource dataSource;

	private static SingleJdbcConnection jdbcConnection;

	private static String jdbcDriver = "org.apache.derby.jdbc.EmbeddedDriver";

	private static String jdbcUrl = "jdbc:derby:./target/test-database;create=true";

	private int counter = 0;

	@BeforeClass
	public static void initializeDatabase() throws Exception {
		File dbFolder = new File("./target");
		dbFolder.mkdirs();

		connectDB();

		exectuteSqlSilently(dataSource, DROP_UNIQUECACHEHITS_TABLE_SQL);
		exectuteSqlSilently(dataSource, CREATE_UNIQUECACHEHITS_TABLE_SQL);

		PerformanceLogger.setEnabled(true);
	}

	@Before
	public void clearData() {
		PerformanceLogger.clear();
		exectuteSqlSilently(dataSource, CLEAR_UNIQUECACHEHITS_TABLE_SQL);
		JdbcProfiler.getInstance().clearPendingEvents();
	}

	public void insertRows(int count) throws Exception {
		for (int i = 0; i < count; ++i) {
			insertOneRow();
		}
	}

	private void insertOneRow() throws SQLException {
		PreparedStatement prepareStatement = dataSource.getConnection()
				.prepareStatement(INSERT_SQL);
		prepareStatement.setString(1, Integer.toString(counter++));
		prepareStatement.setInt(2, 2);
		prepareStatement.execute();
	}

	public static void exectuteSqlSilently(DataSource source, String sql) {
		try {
			CacheUtilities.execute(source, sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void testSelect() throws Exception {
		int insertCount = 3;
		insertRows(insertCount);

		Statement statement = wrappedConnection.createStatement();
		ResultSet rs = statement.executeQuery(SELECT_SQL);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		Assert.assertEquals(insertCount, count);
		rs.close();
		statement.close();
		wrappedConnection.commit();
		wrappedConnection.close();

		assertProfilerIsClean();

		String adjustJdbcOperationName = JdbcProfiler.getInstance()
				.adjustJdbcOperationName(SELECT_SQL);
		
		Assert.assertEquals("JDBC : select * from UNIQUECACHEHITS", adjustJdbcOperationName);
		
		TimeIntervalLogEntryMXBean performanceLog = PerformanceLogger
				.getPerformanceLog(adjustJdbcOperationName);
		Assert.assertEquals(1, performanceLog.getCallCount());

		performanceLog = PerformanceLogger
				.getPerformanceLog(JdbcProfiler.JDBC_ALL_OPERATIONS);
		Assert.assertEquals(1, performanceLog.getCallCount());
	}
	
	@Test
	public void testSelectDoubleClose() throws Exception {
		int insertCount = 3;
		insertRows(insertCount);

		Statement statement = wrappedConnection.createStatement();
		ResultSet rs = statement.executeQuery(SELECT_SQL);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		Assert.assertEquals(insertCount, count);
		rs.close();
		rs.close();
		statement.close();
		statement.close();
		wrappedConnection.commit();
		wrappedConnection.commit();
		wrappedConnection.close();
		wrappedConnection.close();

		assertProfilerIsClean();

		String adjustJdbcOperationName = JdbcProfiler.getInstance()
				.adjustJdbcOperationName(SELECT_SQL);
		
		Assert.assertEquals("JDBC : select * from UNIQUECACHEHITS", adjustJdbcOperationName);
		
		TimeIntervalLogEntryMXBean performanceLog = PerformanceLogger
				.getPerformanceLog(adjustJdbcOperationName);
		Assert.assertEquals(1, performanceLog.getCallCount());

		performanceLog = PerformanceLogger
				.getPerformanceLog(JdbcProfiler.JDBC_ALL_OPERATIONS);
		Assert.assertEquals(1, performanceLog.getCallCount());
	}
	
	@Test
	public void testSelectNoResultSetClose() throws Exception {
		int insertCount = 3;
		insertRows(insertCount);

		Statement statement = wrappedConnection.createStatement();
		ResultSet rs = statement.executeQuery(SELECT_SQL);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		Assert.assertEquals(insertCount, count);
		// rs.close();
		statement.close();
		wrappedConnection.commit();
		wrappedConnection.close();

		assertProfilerIsClean();

		String adjustJdbcOperationName = JdbcProfiler.getInstance()
				.adjustJdbcOperationName(SELECT_SQL);
		
		TimeIntervalLogEntryMXBean performanceLog = PerformanceLogger
				.getPerformanceLog(adjustJdbcOperationName);
		Assert.assertEquals(1, performanceLog.getCallCount());

		performanceLog = PerformanceLogger
				.getPerformanceLog(JdbcProfiler.JDBC_ALL_OPERATIONS);
		Assert.assertEquals(1, performanceLog.getCallCount());
	}
	
	@Test
	public void testSelectNoResultSetAndStatementClose() throws Exception {
		int insertCount = 3;
		insertRows(insertCount);

		Statement statement = wrappedConnection.createStatement();
		ResultSet rs = statement.executeQuery(SELECT_SQL);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		Assert.assertEquals(insertCount, count);
		// rs.close();
		// statement.close();
		wrappedConnection.commit();
		wrappedConnection.close();

		assertProfilerIsClean();

		String adjustJdbcOperationName = JdbcProfiler.getInstance()
				.adjustJdbcOperationName(SELECT_SQL);
		
		TimeIntervalLogEntryMXBean performanceLog = PerformanceLogger
				.getPerformanceLog(adjustJdbcOperationName);
		Assert.assertEquals(1, performanceLog.getCallCount());

		performanceLog = PerformanceLogger
				.getPerformanceLog(JdbcProfiler.JDBC_ALL_OPERATIONS);
		Assert.assertEquals(1, performanceLog.getCallCount());
	}
	
	@Test
	public void testSelectNoResultSetAndStatementCloseAndConnectionCommit() throws Exception {
		int insertCount = 3;
		insertRows(insertCount);

		Statement statement = wrappedConnection.createStatement();
		ResultSet rs = statement.executeQuery(SELECT_SQL);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		Assert.assertEquals(insertCount, count);
		// rs.close();
		// statement.close();
		// wrappedConnection.commit();
		wrappedConnection.close();

		assertProfilerIsClean();

		String adjustJdbcOperationName = JdbcProfiler.getInstance()
				.adjustJdbcOperationName(SELECT_SQL);
		
		TimeIntervalLogEntryMXBean performanceLog = PerformanceLogger
				.getPerformanceLog(adjustJdbcOperationName);
		Assert.assertEquals(1, performanceLog.getCallCount());

		performanceLog = PerformanceLogger
				.getPerformanceLog(JdbcProfiler.JDBC_ALL_OPERATIONS);
		Assert.assertEquals(1, performanceLog.getCallCount());
	}
	
	
	private void assertProfilerIsClean() {
		Assert.assertEquals(0, JdbcProfiler.getPendingEvents().size());
		Assert.assertEquals(0, JdbcProfiler.getPendingJdbcEvents().size());
		Assert.assertEquals(0, JdbcProfiler.getPendingOperations().size());
	}

	public static void connectDB() throws Exception {
		Class.forName(jdbcDriver);

		if (dataSource != null) {
			return;
		}

		Connection conn = DriverManager.getConnection(jdbcUrl);

		// wrap a test connection that never closes
		jdbcConnection = new SingleJdbcConnection();
		jdbcConnection.setConnection(conn);

		dataSource = new SingleJdbcDataSource();
		dataSource.setConnection(jdbcConnection);

		wrappedConnection = new WrappedConnection(jdbcConnection);

	}

}
