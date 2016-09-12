package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.ibm.issw.jdbc.profiler.JdbcProfiler;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * WrappedPreparedStatement
 */
public class WrappedPreparedStatement extends WrappedStatement implements
		PreparedStatement {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	private static final Logger LOG = Logger
			.getLogger(WrappedPreparedStatement.class.getName());
	private String sqlStatement;
	private final PreparedStatement pstmt;

	/**
	 * ctor
	 * @param preparedStatement
	 * @param sql
	 * @param ref
	 * @param transaction
	 */
	public WrappedPreparedStatement(PreparedStatement preparedStatement,
			String sql, String ref, String transaction, Connection connection) {
		super(preparedStatement, ref, transaction, connection);
		this.pstmt = preparedStatement;
		this.sqlStatement = sql;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	@Override
    public final ResultSet executeQuery() throws SQLException {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Executing query: " + this.sqlStatement);
		}

		ResultSet rslt = null;

		if ((this instanceof CallableStatement)){
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.CALLABLE, this.ref);}
		else {
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, this.ref);
		}
		profileSqlStatement(this.sqlStatement);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
		JdbcEvent jdbcEvent = JdbcProfiler.getInstance().getJdbcEvent(ref);

		ResultSet resultSet = this.pstmt.executeQuery();
		String currentRef = this.ref;
		rslt = wrapResultSet(jdbcEvent, resultSet, currentRef);
		JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
		JdbcProfiler.getInstance().addStack(this.ref);

		allocateNewRef();

		return rslt;
	}

	/**
	 * 
	 * allocateNewRef
	 * @throws SQLException
	 */
	public void allocateNewRef() throws SQLException {
		this.ref = WrappedConnection.getNextRefCount();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	@Override
    public final int executeUpdate() throws SQLException {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Executing update: " + this.sqlStatement);
		}

		int rows = 0;
		if ((this instanceof CallableStatement)) {
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.CALLABLE, this.ref);
		} else {
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, this.ref);
		}
		profileSqlStatement(this.sqlStatement);
		JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
		
        boolean success = false;
        try
        {
            rows = this.pstmt.executeUpdate();
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_UPDATE, this.ref);
            JdbcProfiler.getInstance().addStack(this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(rows, this.ref, success);
        }

		allocateNewRef();

		return rows;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	@Override
    public final void setNull(int parameterIndex, int sqlType)
			throws SQLException {
		setData(parameterIndex, "null");
		this.pstmt.setNull(parameterIndex, sqlType);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	@Override
    public final void setBoolean(int parameterIndex, boolean x)
			throws SQLException {
		setData(parameterIndex, new Boolean(x));
		this.pstmt.setBoolean(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	@Override
    public final void setByte(int parameterIndex, byte x) throws SQLException {
		setData(parameterIndex, new Byte(x));
		this.pstmt.setByte(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	@Override
    public final void setShort(int parameterIndex, short x) throws SQLException {
		setData(parameterIndex, new Short(x));
		this.pstmt.setShort(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	@Override
    public final void setInt(int parameterIndex, int x) throws SQLException {
		setData(parameterIndex, new Integer(x));
		this.pstmt.setInt(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	@Override
    public final void setLong(int parameterIndex, long x) throws SQLException {
		setData(parameterIndex, new Long(x));
		this.pstmt.setLong(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	@Override
    public final void setFloat(int parameterIndex, float x) throws SQLException {
		setData(parameterIndex, new Float(x));
		this.pstmt.setFloat(parameterIndex, x);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	@Override
    public final void setDouble(int parameterIndex, double x)
			throws SQLException {
		setData(parameterIndex, new Double(x));
		this.pstmt.setDouble(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
    public final void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setBigDecimal(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	@Override
    public final void setString(int parameterIndex, String x)
			throws SQLException {
		setData(parameterIndex, "'" + x + "'");
		this.pstmt.setString(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	@Override
    public final void setBytes(int parameterIndex, byte[] x)
			throws SQLException {
		this.pstmt.setBytes(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	@Override
    public final void setDate(int parameterIndex, Date x) throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setDate(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	@Override
    public final void setTime(int parameterIndex, Time x) throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setTime(parameterIndex, x);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	@Override
    public final void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setTimestamp(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
    public final void setAsciiStream(int parameterIndex, InputStream x,
			int length) throws SQLException {
		this.pstmt.setAsciiStream(parameterIndex, x, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
	 */
	@Override
    @Deprecated
	public final void setUnicodeStream(int parameterIndex, InputStream x,
			int length) throws SQLException {
		//$ANALYSIS-IGNORE
		this.pstmt.setUnicodeStream(parameterIndex, x, length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
    public final void setBinaryStream(int parameterIndex, InputStream x,
			int length) throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setBinaryStream(parameterIndex, x, length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	@Override
    public final void clearParameters() throws SQLException {
		this.pstmt.clearParameters();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
	 */
	@Override
    public final void setObject(int parameterIndex, Object x,
			int targetSqlType, int scale) throws SQLException {
		if ((x instanceof String)){
			setData(parameterIndex, "'" + (String) x + "'");}
		else {
			setData(parameterIndex, x);
		}
		this.pstmt.setObject(parameterIndex, x, targetSqlType, scale);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	@Override
    public final void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		if ((x instanceof String)){
			setData(parameterIndex, "'" + (String) x + "'");}
		else {
			setData(parameterIndex, x);
		}
		this.pstmt.setObject(parameterIndex, x, targetSqlType);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	@Override
    public final void setObject(int parameterIndex, Object x)
			throws SQLException {
		if ((x instanceof String)){
			setData(parameterIndex, "'" + (String) x + "'");}
		else {
			setData(parameterIndex, x);
		}
		this.pstmt.setObject(parameterIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#execute()
	 */
	@Override
    public final boolean execute() throws SQLException {
		if ((this instanceof CallableStatement)){
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.CALLABLE, this.ref);}
		else {
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, this.ref);
		}

        profileSqlStatement(this.sqlStatement);
        JdbcProfiler.getInstance().start(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
        boolean bool = false;
        boolean success = false;
        try
        {
            bool = this.pstmt.execute();
            success = true;
        }
        finally
        {
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_EXECUTE_QUERY, this.ref);
            JdbcProfiler.getInstance().addRowsUpdated(1, this.ref, success);
        }

		allocateNewRef();

		return bool;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	@Override
    public final void addBatch() throws SQLException {
		getBatchList().add(sqlStatement);
		this.pstmt.addBatch();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
	 */
	@Override
    public final void setCharacterStream(int parameterIndex, Reader reader,
			int length) throws SQLException {
		this.pstmt.setCharacterStream(parameterIndex, reader, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	@Override
    public final void setRef(int i, Ref x) throws SQLException {
		this.pstmt.setRef(i, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	@Override
    public final void setBlob(int i, Blob x) throws SQLException {
		setData(i, x);
		this.pstmt.setBlob(i, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	@Override
    public final void setClob(int i, Clob x) throws SQLException {
		setData(i, x);
		this.pstmt.setClob(i, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	@Override
    public final void setArray(int i, Array x) throws SQLException {
		setData(i, x);
		this.pstmt.setArray(i, x);
	}

	
	private void setData(int i, Object x) throws SQLException {
		JdbcProfiler.getInstance().addSetData(i, x, this.ref);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	@Override
    public final ResultSetMetaData getMetaData() throws SQLException {
		return this.pstmt.getMetaData();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
	 */
	@Override
    public final void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setDate(parameterIndex, x, cal);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
	 */
	@Override
    public final void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setTime(parameterIndex, x, cal);
	}
	
	/*
	 * 
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
	 */
	@Override
    public final void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setTimestamp(parameterIndex, x, cal);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	@Override
    public final void setNull(int paramIndex, int sqlType, String typeName)
			throws SQLException {
		setData(paramIndex, "null");
		this.pstmt.setNull(paramIndex, sqlType, typeName);
	}
	/*
	 * 
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	@Override
    public final void setURL(int parameterIndex, URL x) throws SQLException {
		setData(parameterIndex, x);
		this.pstmt.setURL(parameterIndex, x);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	@Override
    public final ParameterMetaData getParameterMetaData() throws SQLException {
		return this.pstmt.getParameterMetaData();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setAsciiStream(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setAsciiStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setAsciiStream(arg0, arg1, arg2);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setBinaryStream(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setBinaryStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setBinaryStream(arg0, arg1, arg2);

	}

	
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	@Override
	public void setBlob(int arg0, InputStream arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setBlob(arg0, arg1);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void setBlob(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setBlob(arg0, arg1, arg2);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setCharacterStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setCharacterStream(arg0, arg1, arg2);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	@Override
	public void setClob(int arg0, Reader arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setClob(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	@Override
	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setClob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setNCharacterStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setNCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setNCharacterStream(arg0, arg1, arg2);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	@Override
	public void setNClob(int arg0, NClob arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setNClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	@Override
	public void setNClob(int arg0, Reader arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setNClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	@Override
	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setNClob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	@Override
	public void setNString(int arg0, String arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setNString(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	@Override
	public void setRowId(int arg0, RowId arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setRowId(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
		setData(arg0, arg1);
		this.pstmt.setSQLXML(arg0, arg1);
	}
}
