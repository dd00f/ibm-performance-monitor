package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.ibm.issw.jdbc.profiler.JdbcEvent;
import com.ibm.issw.jdbc.profiler.JdbcProfiler;
import com.ibm.service.detailed.JdbcLogger;

/**
 * 
 * WrappedResultSet
 */
public class WrappedResultSet implements ResultSet {

//	private static final int DATA_CELL_OVERHEAD = 2;
//
//	private static final Logger LOG = Logger.getLogger(WrappedResultSet.class
//			.getName());
//
//	private static final int ROW_READ_OVERHEAD = 10;

	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private ResultSet rslt;
	private int rowsRead = 0;
	private String ref;
//	private JdbcEvent event;

	/**
	 * 
	 * wrapResultSet
	 * @param resultSet
	 * @param currentRef
	 * @param jdbcEvent
	 * @return the wrapped result set
	 */
	public static ResultSet wrapResultSet(ResultSet resultSet,
			String currentRef, JdbcEvent jdbcEvent, WrappedStatement statement) {
		ResultSet rslt;
		if (JdbcLogger.isResultSetSizeMeasured()) {
			rslt = new WrappedCalculatedResultSet(resultSet, currentRef,
					jdbcEvent);
		} else {
			rslt = new WrappedResultSet(resultSet, currentRef, jdbcEvent);
		}

		statement.addPendingResultSet( rslt );
		return rslt;
	}

	/**
	 * ctor
	 * @param resultSet
	 * @param reference
	 * @param event
	 */
	public WrappedResultSet(ResultSet resultSet, String reference,
			JdbcEvent event) {
		this.rslt = resultSet;
		this.ref = reference;
//		this.event = event;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#next()
	 */
	@Override
    public boolean next() throws SQLException {
		boolean havemore = this.rslt.next();
		if (havemore) {
			this.rowsRead += 1;
		}
		return havemore;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#close()
	 */
	@Override
    public void close() throws SQLException {
		JdbcProfiler.getInstance().addRowsRead(this.rowsRead, this.ref, true);
		this.rslt.close();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#wasNull()
	 */
	@Override
    public boolean wasNull() throws SQLException {
		return this.rslt.wasNull();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getString(int)
	 */
	@Override
    public String getString(int columnIndex) throws SQLException {
		String retVal = this.rslt.getString(columnIndex);
		return retVal;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	@Override
    public boolean getBoolean(int columnIndex) throws SQLException {
		return this.rslt.getBoolean(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getByte(int)
	 */
	@Override
    public byte getByte(int columnIndex) throws SQLException {
		return this.rslt.getByte(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(int)
	 */
	@Override
    public short getShort(int columnIndex) throws SQLException {
		return this.rslt.getShort(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getInt(int)
	 */
	@Override
    public int getInt(int columnIndex) throws SQLException {
		return this.rslt.getInt(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getLong(int)
	 */
	@Override
    public long getLong(int columnIndex) throws SQLException {
		return this.rslt.getLong(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	@Override
    public float getFloat(int columnIndex) throws SQLException {
		return this.rslt.getFloat(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	@Override
    public double getDouble(int columnIndex) throws SQLException {
		return this.rslt.getDouble(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	@Override
    @Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		//$ANALYSIS-IGNORE
		BigDecimal bigDecimal = this.rslt.getBigDecimal(columnIndex, scale);
		return bigDecimal;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	@Override
    public byte[] getBytes(int columnIndex) throws SQLException {
		byte[] bytes = this.rslt.getBytes(columnIndex);
		return bytes;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(int)
	 */
	@Override
    public Date getDate(int columnIndex) throws SQLException {
		return this.rslt.getDate(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int)
	 */
	@Override
    public Time getTime(int columnIndex) throws SQLException {
		return this.rslt.getTime(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	@Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return this.rslt.getTimestamp(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	@Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
		InputStream asciiStream = this.rslt.getAsciiStream(columnIndex);
		return asciiStream;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	@Override
    @Deprecated
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		//$ANALYSIS-IGNORE
		InputStream unicodeStream = this.rslt.getUnicodeStream(columnIndex);
		return unicodeStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	@Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
		InputStream binaryStream = this.rslt.getBinaryStream(columnIndex);
		return binaryStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	@Override
    public String getString(String columnName) throws SQLException {
		String string = this.rslt.getString(columnName);
		return string;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	@Override
    public boolean getBoolean(String columnName) throws SQLException {
		return this.rslt.getBoolean(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	@Override
    public byte getByte(String columnName) throws SQLException {
		return this.rslt.getByte(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	@Override
    public short getShort(String columnName) throws SQLException {
		return this.rslt.getShort(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	@Override
    public int getInt(String columnName) throws SQLException {
		return this.rslt.getInt(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	@Override
    public long getLong(String columnName) throws SQLException {
		return this.rslt.getLong(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	@Override
    public float getFloat(String columnName) throws SQLException {
		return this.rslt.getFloat(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	@Override
    public double getDouble(String columnName) throws SQLException {
		return this.rslt.getDouble(columnName);
	}

/*
 * (non-Javadoc)
 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
 */
	@Override
    @Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale)
			throws SQLException {
		//$ANALYSIS-IGNORE
		BigDecimal bigDecimal = this.rslt.getBigDecimal(columnName, scale);
		return bigDecimal;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	@Override
    public byte[] getBytes(String columnName) throws SQLException {
		byte[] bytes = this.rslt.getBytes(columnName);
		return bytes;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	@Override
    public Date getDate(String columnName) throws SQLException {
		return this.rslt.getDate(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	@Override
    public Time getTime(String columnName) throws SQLException {
		return this.rslt.getTime(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	@Override
    public Timestamp getTimestamp(String columnName) throws SQLException {
		return this.rslt.getTimestamp(columnName);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	@Override
    public InputStream getAsciiStream(String columnName) throws SQLException {
		InputStream asciiStream = this.rslt.getAsciiStream(columnName);
		return asciiStream;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	@Override
    @Deprecated
	public InputStream getUnicodeStream(String columnName) throws SQLException {
		//$ANALYSIS-IGNORE
		InputStream unicodeStream = this.rslt.getUnicodeStream(columnName);
		return unicodeStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	@Override
    public InputStream getBinaryStream(String columnName) throws SQLException {
		InputStream binaryStream = this.rslt.getBinaryStream(columnName);
		return binaryStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getWarnings()
	 */
	@Override
    public SQLWarning getWarnings() throws SQLException {
		return this.rslt.getWarnings();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	@Override
    public void clearWarnings() throws SQLException {
		this.rslt.clearWarnings();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getCursorName()
	 */
	@Override
    public String getCursorName() throws SQLException {
		return this.rslt.getCursorName();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getMetaData()
	 */
	@Override
    public ResultSetMetaData getMetaData() throws SQLException {
		return this.rslt.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int)
	 */
	@Override
    public Object getObject(int columnIndex) throws SQLException {
		Object object = this.rslt.getObject(columnIndex);
		return object;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	@Override
    public Object getObject(String columnName) throws SQLException {
		Object object = this.rslt.getObject(columnName);
		return object;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	@Override
    public int findColumn(String columnName) throws SQLException {
		return this.rslt.findColumn(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	@Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
		Reader characterStream = this.rslt.getCharacterStream(columnIndex);
		return characterStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	@Override
    public Reader getCharacterStream(String columnName) throws SQLException {
		Reader characterStream = this.rslt.getCharacterStream(columnName);
		return characterStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	@Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		BigDecimal bigDecimal = this.rslt.getBigDecimal(columnIndex);
		return bigDecimal;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	@Override
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
		BigDecimal bigDecimal = this.rslt.getBigDecimal(columnName);
		return bigDecimal;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	@Override
    public boolean isBeforeFirst() throws SQLException {
		return this.rslt.isBeforeFirst();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	@Override
    public boolean isAfterLast() throws SQLException {
		return this.rslt.isAfterLast();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#isFirst()
	 */
	@Override
    public boolean isFirst() throws SQLException {
		return this.rslt.isFirst();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#isLast()
	 */
	@Override
    public boolean isLast() throws SQLException {
		return this.rslt.isLast();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	@Override
    public void beforeFirst() throws SQLException {
		this.rslt.beforeFirst();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#afterLast()
	 */
	@Override
    public void afterLast() throws SQLException {
		this.rslt.afterLast();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#first()
	 */
	@Override
    public boolean first() throws SQLException {
		return this.rslt.first();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#last()
	 */
	@Override
    public boolean last() throws SQLException {
		return this.rslt.last();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getRow()
	 */
	@Override
    public int getRow() throws SQLException {
		return this.rslt.getRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#absolute(int)
	 */
	@Override
    public boolean absolute(int row) throws SQLException {
		return this.rslt.absolute(row);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#relative(int)
	 */
	@Override
    public boolean relative(int rows) throws SQLException {
		return this.rslt.relative(rows);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#previous()
	 */
	@Override
    public boolean previous() throws SQLException {
		return this.rslt.previous();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#setFetchDirection(int)
	 */
	@Override
    public void setFetchDirection(int direction) throws SQLException {
		this.rslt.setFetchDirection(direction);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getFetchDirection()
	 */
	@Override
    public int getFetchDirection() throws SQLException {
		return this.rslt.getFetchDirection();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	@Override
    public void setFetchSize(int rows) throws SQLException {
		this.rslt.setFetchSize(rows);
	}


	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	@Override
    public int getFetchSize() throws SQLException {
		return this.rslt.getFetchSize();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getType()
	 */
	@Override
    public int getType() throws SQLException {
		return this.rslt.getType();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getConcurrency()
	 */
	@Override
    public int getConcurrency() throws SQLException {
		return this.rslt.getConcurrency();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	@Override
    public boolean rowUpdated() throws SQLException {
		return this.rslt.rowUpdated();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#rowInserted()
	 */
	@Override
    public boolean rowInserted() throws SQLException {
		return this.rslt.rowInserted();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	@Override
    public boolean rowDeleted() throws SQLException {
		return this.rslt.rowDeleted();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	@Override
    public void updateNull(int columnIndex) throws SQLException {
		this.rslt.updateNull(columnIndex);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	@Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		this.rslt.updateBoolean(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	@Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
		this.rslt.updateByte(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	@Override
    public void updateShort(int columnIndex, short x) throws SQLException {
		this.rslt.updateShort(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	@Override
    public void updateInt(int columnIndex, int x) throws SQLException {
		this.rslt.updateInt(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	@Override
    public void updateLong(int columnIndex, long x) throws SQLException {
		this.rslt.updateLong(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	@Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
		this.rslt.updateFloat(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	@Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
		this.rslt.updateDouble(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
    public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		this.rslt.updateBigDecimal(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateString(int, java.lang.String)
	 */
	@Override
    public void updateString(int columnIndex, String x) throws SQLException {
		this.rslt.updateString(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	@Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		this.rslt.updateBytes(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	@Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
		this.rslt.updateDate(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
	 */
	@Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
		this.rslt.updateTime(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
	 */
	@Override
    public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		this.rslt.updateTimestamp(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		this.rslt.updateAsciiStream(columnIndex, x, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		this.rslt.updateBinaryStream(columnIndex, x, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
	 */
	@Override
    public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		this.rslt.updateCharacterStream(columnIndex, x, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	@Override
    public void updateObject(int columnIndex, Object x, int scale)
			throws SQLException {
		this.rslt.updateObject(columnIndex, x, scale);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	@Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
		this.rslt.updateObject(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	@Override
    public void updateNull(String columnName) throws SQLException {
		this.rslt.updateNull(columnName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
	 */
	@Override
    public void updateBoolean(String columnName, boolean x) throws SQLException {
		this.rslt.updateBoolean(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
	 */
	@Override
    public void updateByte(String columnName, byte x) throws SQLException {
		this.rslt.updateByte(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	@Override
    public void updateShort(String columnName, short x) throws SQLException {
		this.rslt.updateShort(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	@Override
    public void updateInt(String columnName, int x) throws SQLException {
		this.rslt.updateInt(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	@Override
    public void updateLong(String columnName, long x) throws SQLException {
		this.rslt.updateLong(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	@Override
    public void updateFloat(String columnName, float x) throws SQLException {
		this.rslt.updateFloat(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	@Override
    public void updateDouble(String columnName, double x) throws SQLException {
		this.rslt.updateDouble(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	@Override
    public void updateBigDecimal(String columnName, BigDecimal x)
			throws SQLException {
		this.rslt.updateBigDecimal(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
	 */
	@Override
    public void updateString(String columnName, String x) throws SQLException {
		this.rslt.updateString(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
	 */
	@Override
    public void updateBytes(String columnName, byte[] x) throws SQLException {
		this.rslt.updateBytes(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	@Override
    public void updateDate(String columnName, Date x) throws SQLException {
		this.rslt.updateDate(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
	 */
	@Override
    public void updateTime(String columnName, Time x) throws SQLException {
		this.rslt.updateTime(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
    public void updateTimestamp(String columnName, Timestamp x)
			throws SQLException {
		this.rslt.updateTimestamp(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, int)
	 */
	@Override
    public void updateAsciiStream(String columnName, InputStream x, int length)
			throws SQLException {
		this.rslt.updateAsciiStream(columnName, x, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, int)
	 */
	@Override
    public void updateBinaryStream(String columnName, InputStream x, int length)
			throws SQLException {
		this.rslt.updateBinaryStream(columnName, x, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, int)
	 */
	@Override
    public void updateCharacterStream(String columnName, Reader reader,
			int length) throws SQLException {
		this.rslt.updateCharacterStream(columnName, reader, length);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	@Override
    public void updateObject(String columnName, Object x, int scale)
			throws SQLException {
		this.rslt.updateObject(columnName, x, scale);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	@Override
    public void updateObject(String columnName, Object x) throws SQLException {
		this.rslt.updateObject(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#insertRow()
	 */
	@Override
    public void insertRow() throws SQLException {
		this.rslt.insertRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateRow()
	 */
	@Override
    public void updateRow() throws SQLException {
		this.rslt.updateRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#deleteRow()
	 */
	@Override
    public void deleteRow() throws SQLException {
		this.rslt.deleteRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#refreshRow()
	 */
	@Override
    public void refreshRow() throws SQLException {
		this.rslt.refreshRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	@Override
    public void cancelRowUpdates() throws SQLException {
		this.rslt.cancelRowUpdates();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	@Override
    public void moveToInsertRow() throws SQLException {
		this.rslt.moveToInsertRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	@Override
    public void moveToCurrentRow() throws SQLException {
		this.rslt.moveToCurrentRow();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getStatement()
	 */
	@Override
    public Statement getStatement() throws SQLException {
		return this.rslt.getStatement();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getRef(int)
	 */
	@Override
    public Ref getRef(int i) throws SQLException {
		return this.rslt.getRef(i);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	@Override
    public Blob getBlob(int i) throws SQLException {
		return this.rslt.getBlob(i);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getClob(int)
	 */
	@Override
    public Clob getClob(int i) throws SQLException {
		return this.rslt.getClob(i);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getArray(int)
	 */
	@Override
    public Array getArray(int i) throws SQLException {
		return this.rslt.getArray(i);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	@Override
    public Ref getRef(String colName) throws SQLException {
		return this.rslt.getRef(colName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	@Override
    public Blob getBlob(String colName) throws SQLException {
		return this.rslt.getBlob(colName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	@Override
    public Clob getClob(String colName) throws SQLException {
		return this.rslt.getClob(colName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	@Override
    public Array getArray(String colName) throws SQLException {
		return this.rslt.getArray(colName);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	@Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return this.rslt.getDate(columnIndex, cal);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	@Override
    public Date getDate(String columnName, Calendar cal) throws SQLException {
		return this.rslt.getDate(columnName, cal);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	@Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return this.rslt.getTime(columnIndex, cal);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
    public Time getTime(String columnName, Calendar cal) throws SQLException {
		return this.rslt.getTime(columnName, cal);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	@Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return this.rslt.getTimestamp(columnIndex, cal);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
    public Timestamp getTimestamp(String columnName, Calendar cal)
			throws SQLException {
		return this.rslt.getTimestamp(columnName, cal);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getURL(int)
	 */
	@Override
    public URL getURL(int columnIndex) throws SQLException {
		URL url = this.rslt.getURL(columnIndex);
		return url;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	@Override
    public URL getURL(String columnName) throws SQLException {
		URL url = this.rslt.getURL(columnName);
		return url;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	@Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
		this.rslt.updateRef(columnIndex, x);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	@Override
    public void updateRef(String columnName, Ref x) throws SQLException {
		this.rslt.updateRef(columnName, x);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
	 */
	@Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
		this.rslt.updateBlob(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
	 */
	@Override
    public void updateBlob(String columnName, Blob x) throws SQLException {
		this.rslt.updateBlob(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	@Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
		this.rslt.updateClob(columnIndex, x);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	@Override
    public void updateClob(String columnName, Clob x) throws SQLException {
		this.rslt.updateClob(columnName, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
	 */
	@Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
		this.rslt.updateArray(columnIndex, x);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
	 */
	@Override
    public void updateArray(String columnName, Array x) throws SQLException {
		this.rslt.updateArray(columnName, x);
	}
	/**
	 * @return row read
	 */
	public int getRowsRead() {
		return this.rowsRead;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException {
		return this.rslt.getHoldability();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	@Override
	public Reader getNCharacterStream(int arg0) throws SQLException {
		Reader characterStream = this.rslt.getNCharacterStream(arg0);
		return characterStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getNCharacterStream(String arg0) throws SQLException {
		Reader characterStream = this.rslt.getNCharacterStream(arg0);
		return characterStream;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getNClob(int)
	 */
	@Override
	public NClob getNClob(int arg0) throws SQLException {
		NClob clob = this.rslt.getNClob(arg0);
		return clob;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob(String arg0) throws SQLException {
		NClob clob = this.rslt.getNClob(arg0);
		return clob;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getNString(int)
	 */
	@Override
	public String getNString(int arg0) throws SQLException {
		String string = this.rslt.getNString(arg0);
		return string;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	@Override
	public String getNString(String arg0) throws SQLException {
		String string = this.rslt.getNString(arg0);
		return string;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		Object object = this.rslt.getObject(arg0, arg1);
		return object;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		Object object = this.rslt.getObject(arg0, arg1);
		return object;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	@Override
	public RowId getRowId(int arg0) throws SQLException {
		return this.rslt.getRowId(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(String arg0) throws SQLException {
		return this.rslt.getRowId(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int arg0) throws SQLException {
		SQLXML sqlxml = this.rslt.getSQLXML(arg0);
		return sqlxml;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String arg0) throws SQLException {
		SQLXML sqlxml = this.rslt.getSQLXML(arg0);
		return sqlxml;
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return this.rslt.isClosed();
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(int arg0, InputStream arg1)
			throws SQLException {
		this.rslt.updateAsciiStream(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(String arg0, InputStream arg1)
			throws SQLException {
		this.rslt.updateAsciiStream(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		this.rslt.updateAsciiStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		this.rslt.updateAsciiStream(arg0, arg1, arg2);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(int arg0, InputStream arg1)
			throws SQLException {
		this.rslt.updateBinaryStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(String arg0, InputStream arg1)
			throws SQLException {
		this.rslt.updateBinaryStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		this.rslt.updateBinaryStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		this.rslt.updateBinaryStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
	 */
	@Override
	public void updateBlob(int arg0, InputStream arg1) throws SQLException {
		this.rslt.updateBlob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBlob(String arg0, InputStream arg1) throws SQLException {
		this.rslt.updateBlob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		this.rslt.updateBlob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		this.rslt.updateBlob(arg0, arg1, arg2);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(int arg0, Reader arg1)
			throws SQLException {
		this.rslt.updateCharacterStream(arg0, arg1);

	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		this.rslt.updateCharacterStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateCharacterStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateCharacterStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	@Override
	public void updateClob(int arg0, Reader arg1) throws SQLException {
		this.rslt.updateClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateClob(String arg0, Reader arg1) throws SQLException {
		this.rslt.updateClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateClob(int arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateClob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateClob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(int arg0, Reader arg1)
			throws SQLException {
		this.rslt.updateNCharacterStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		this.rslt.updateNCharacterStream(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateNCharacterStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateNCharacterStream(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	@Override
	public void updateNClob(int arg0, NClob arg1) throws SQLException {
		this.rslt.updateNClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void updateNClob(String arg0, NClob arg1) throws SQLException {
		this.rslt.updateNClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	@Override
	public void updateNClob(int arg0, Reader arg1) throws SQLException {
		this.rslt.updateNClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNClob(String arg0, Reader arg1) throws SQLException {
		this.rslt.updateNClob(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(int arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateNClob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		this.rslt.updateNClob(arg0, arg1, arg2);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	@Override
	public void updateNString(int arg0, String arg1) throws SQLException {
		this.rslt.updateNString(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNString(String arg0, String arg1) throws SQLException {
		this.rslt.updateNString(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
	 */
	@Override
	public void updateRowId(int arg0, RowId arg1) throws SQLException {
		this.rslt.updateRowId(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void updateRowId(String arg0, RowId arg1) throws SQLException {
		this.rslt.updateRowId(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
		this.rslt.updateSQLXML(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
		this.rslt.updateSQLXML(arg0, arg1);

	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.rslt.isWrapperFor(iface);
	}
	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return this.rslt.unwrap(iface);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int, java.lang.Class)
	 */
	@Override
	public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
		T object = this.rslt.getObject(arg0, arg1);
		return object;
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
		T object = this.rslt.getObject(arg0, arg1);
		return object;
	}
}
