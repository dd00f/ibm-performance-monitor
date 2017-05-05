package com.ibm.issw.jdbc.wrappers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleResultSetCache;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeRegistration;

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.ibm.service.detailed.JdbcLogger;

public class WrappedOracleStatement extends WrappedStatement implements
		OracleStatement {

	private final OracleStatement oraclePs;

	public WrappedOracleStatement(Statement statement, String ref,
			String transaction, Connection connection) {
		super(statement, ref, transaction, connection);
		oraclePs = (OracleStatement) statement;
	}

	@Override
	public void clearDefines() throws SQLException {
		oraclePs.clearDefines();
	}

	@Override
	public void defineColumnType(int paramInt1, int paramInt2)
			throws SQLException {
		oraclePs.defineColumnType(paramInt1, paramInt2);
	}

	@Override
	public void defineColumnType(int paramInt1, int paramInt2, int paramInt3)
			throws SQLException {
		oraclePs.defineColumnType(paramInt1, paramInt2, paramInt3);

	}

	@Override
	public void defineColumnType(int paramInt1, int paramInt2, int paramInt3,
			short paramShort) throws SQLException {
		oraclePs.defineColumnType(paramInt1, paramInt2, paramInt3, paramShort);

	}

	@Override
	public void defineColumnTypeBytes(int paramInt1, int paramInt2,
			int paramInt3) throws SQLException {
		oraclePs.defineColumnTypeBytes(paramInt1, paramInt2, paramInt3);

	}

	@Override
	public void defineColumnTypeChars(int paramInt1, int paramInt2,
			int paramInt3) throws SQLException {
		oraclePs.defineColumnTypeChars(paramInt1, paramInt2, paramInt3);

	}

	@Override
	public void defineColumnType(int paramInt1, int paramInt2,
			String paramString) throws SQLException {
		oraclePs.defineColumnType(paramInt1, paramInt2, paramString);

	}

	@Override
	public int getRowPrefetch() {
		return oraclePs.getRowPrefetch();
	}

	@Override
	public void setResultSetCache(OracleResultSetCache paramOracleResultSetCache)
			throws SQLException {
		oraclePs.setResultSetCache(paramOracleResultSetCache);

	}

	@Override
	public void setRowPrefetch(int paramInt) throws SQLException {
		oraclePs.setRowPrefetch(paramInt);

	}

	@Override
	public void closeWithKey(String paramString) throws SQLException {
		oraclePs.closeWithKey(paramString);

	}

	@SuppressWarnings("deprecation")
	@Override
	public int creationState() {
		return oraclePs.creationState();
	}

	@Override
	public boolean isNCHAR(int paramInt) throws SQLException {
		return oraclePs.isNCHAR(paramInt);
	}

	@Override
	public void setDatabaseChangeRegistration(
			DatabaseChangeRegistration paramDatabaseChangeRegistration)
			throws SQLException {
		oraclePs.setDatabaseChangeRegistration(paramDatabaseChangeRegistration);
	}

	@Override
	public String[] getRegisteredTableNames() throws SQLException {
		return oraclePs.getRegisteredTableNames();
	}

	@Override
	public long getRegisteredQueryId() throws SQLException {
		return oraclePs.getRegisteredQueryId();
	}

	@Override
	protected ResultSet wrapResultSet(JdbcEvent jdbcEvent,
			ResultSet executeQuery, String ref) {
		if (executeQuery instanceof OracleResultSet) {
			ResultSet wrapOracleResultSet = WrappedOracleStatement.wrapOracleResultSet(
					(OracleResultSet) executeQuery, ref, jdbcEvent, this);
			return wrapOracleResultSet;
		}

		return super.wrapResultSet(jdbcEvent, executeQuery, ref);
	}

	/**
	 * 
	 * wrapResultSet
	 * 
	 * @param resultSet the result set
	 * @param currentRef the reference string
	 * @param jdbcEvent the event
	 * @param statement the statement
	 * @return the wrapped result set
	 */
	public static ResultSet wrapOracleResultSet(OracleResultSet resultSet,
			String currentRef, JdbcEvent jdbcEvent, WrappedStatement statement) {
		ResultSet rslt;
		if (JdbcLogger.isResultSetSizeMeasured()) {
			rslt = new WrappedOracleCalculatedResultSet(resultSet, currentRef,
					jdbcEvent);
		} else {
			rslt = new WrappedOracleResultSet(resultSet, currentRef, jdbcEvent);
		}
		
		statement.addPendingResultSet(rslt);
		return rslt;
	}

}
