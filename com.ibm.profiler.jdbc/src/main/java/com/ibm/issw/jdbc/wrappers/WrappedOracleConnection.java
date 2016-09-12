package com.ibm.issw.jdbc.wrappers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Properties;
import java.util.TimeZone;

import com.ibm.issw.jdbc.profiler.JdbcProfiler;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleOCIFailover;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleSavepoint;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.aq.AQDequeueOptions;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQNotificationRegistration;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import oracle.jdbc.pool.OracleConnectionCacheCallback;
import oracle.sql.ARRAY;
import oracle.sql.BINARY_DOUBLE;
import oracle.sql.BINARY_FLOAT;
import oracle.sql.DATE;
import oracle.sql.INTERVALDS;
import oracle.sql.INTERVALYM;
import oracle.sql.NUMBER;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;
import oracle.sql.TypeDescriptor;

@SuppressWarnings("deprecation")
public class WrappedOracleConnection extends WrappedConnection implements
		OracleConnection {

	private final OracleConnection oracleConnection;

	public WrappedOracleConnection(Connection connection) {
		super(connection);
		oracleConnection = (OracleConnection) connection;
	}

	@Override
	public void commit(EnumSet<CommitOption> paramEnumSet) throws SQLException {
		oracleConnection.commit(paramEnumSet);

	}

	@Override
	public void archive(int paramInt1, int paramInt2, String paramString)
			throws SQLException {
		oracleConnection.archive(paramInt1, paramInt2, paramString);

	}

	@Override
	public void openProxySession(int paramInt, Properties paramProperties)
			throws SQLException {
		oracleConnection.openProxySession(paramInt, paramProperties);

	}

	@Override
	public boolean getAutoClose() throws SQLException {
		return oracleConnection.getAutoClose();
	}

	@Override
	public int getDefaultExecuteBatch() {
		return oracleConnection.getDefaultExecuteBatch();
	}

	@Override
	public int getDefaultRowPrefetch() {
		return oracleConnection.getDefaultRowPrefetch();
	}

	@Override
	public Object getDescriptor(String paramString) {
		return oracleConnection.getDescriptor(paramString);
	}

	@Override
	public String[] getEndToEndMetrics() throws SQLException {
		return oracleConnection.getEndToEndMetrics();
	}

	@Override
	public short getEndToEndECIDSequenceNumber() throws SQLException {
		return oracleConnection.getEndToEndECIDSequenceNumber();
	}

	@Override
	public boolean getIncludeSynonyms() {
		return oracleConnection.getIncludeSynonyms();
	}

	@Override
	public boolean getRestrictGetTables() {
		return oracleConnection.getRestrictGetTables();
	}

	@Override
	public Object getJavaObject(String paramString) throws SQLException {
		return oracleConnection.getJavaObject(paramString);
	}

	@Override
	public boolean getRemarksReporting() {
		return oracleConnection.getRemarksReporting();
	}

	@Override
	public String getSQLType(Object paramObject) throws SQLException {
		return oracleConnection.getSQLType(paramObject);
	}

	@Override
	public int getStmtCacheSize() {
		return oracleConnection.getStmtCacheSize();
	}

	@Override
	public short getStructAttrCsId() throws SQLException {
		return oracleConnection.getStructAttrCsId();
	}

	@Override
	public String getUserName() throws SQLException {
		return oracleConnection.getUserName();
	}

	@Override
	public String getCurrentSchema() throws SQLException {
		return oracleConnection.getCurrentSchema();
	}

	@Override
	public boolean getUsingXAFlag() {
		return oracleConnection.getUsingXAFlag();
	}

	@Override
	public boolean getXAErrorFlag() {
		return oracleConnection.getXAErrorFlag();
	}

	@Override
	public int pingDatabase() throws SQLException {
		return oracleConnection.pingDatabase();
	}

	@Override
	public int pingDatabase(int paramInt) throws SQLException {
		return oracleConnection.pingDatabase(paramInt);
	}

	@Override
	public void putDescriptor(String paramString, Object paramObject)
			throws SQLException {
		oracleConnection.putDescriptor(paramString, paramObject);

	}

	@Override
	public void registerSQLType(String paramString,
			@SuppressWarnings("rawtypes") Class paramClass) throws SQLException {
		oracleConnection.registerSQLType(paramString, paramClass);

	}

	@Override
	public void registerSQLType(String paramString1, String paramString2)
			throws SQLException {
		oracleConnection.registerSQLType(paramString1, paramString2);

	}

	@Override
	public void setAutoClose(boolean paramBoolean) throws SQLException {
		oracleConnection.setAutoClose(paramBoolean);

	}

	@Override
	public void setDefaultExecuteBatch(int paramInt) throws SQLException {
		oracleConnection.setDefaultExecuteBatch(paramInt);

	}

	@Override
	public void setDefaultRowPrefetch(int paramInt) throws SQLException {
		oracleConnection.setDefaultRowPrefetch(paramInt);

	}

	@Override
	public void setEndToEndMetrics(String[] paramArrayOfString, short paramShort)
			throws SQLException {
		oracleConnection.setEndToEndMetrics(paramArrayOfString, paramShort);

	}

	@Override
	public void setIncludeSynonyms(boolean paramBoolean) {
		oracleConnection.setIncludeSynonyms(paramBoolean);

	}

	@Override
	public void setRemarksReporting(boolean paramBoolean) {
		oracleConnection.setRemarksReporting(paramBoolean);

	}

	@Override
	public void setRestrictGetTables(boolean paramBoolean) {
		oracleConnection.setRestrictGetTables(paramBoolean);

	}

	@Override
	public void setStmtCacheSize(int paramInt) throws SQLException {
		oracleConnection.setStmtCacheSize(paramInt);

	}

	@Override
	public void setStmtCacheSize(int paramInt, boolean paramBoolean)
			throws SQLException {
		oracleConnection.setStmtCacheSize(paramInt, paramBoolean);

	}

	@Override
	public void setStatementCacheSize(int paramInt) throws SQLException {
		oracleConnection.setStatementCacheSize(paramInt);

	}

	@Override
	public int getStatementCacheSize() throws SQLException {
		return oracleConnection.getStatementCacheSize();
	}

	@Override
	public void setImplicitCachingEnabled(boolean paramBoolean)
			throws SQLException {
		oracleConnection.setImplicitCachingEnabled(paramBoolean);

	}

	@Override
	public boolean getImplicitCachingEnabled() throws SQLException {
		return oracleConnection.getImplicitCachingEnabled();
	}

	@Override
	public void setExplicitCachingEnabled(boolean paramBoolean)
			throws SQLException {
		oracleConnection.setExplicitCachingEnabled(paramBoolean);

	}

	@Override
	public boolean getExplicitCachingEnabled() throws SQLException {
		return oracleConnection.getExplicitCachingEnabled();
	}

	@Override
	public void purgeImplicitCache() throws SQLException {
		oracleConnection.purgeImplicitCache();

	}

	@Override
	public void purgeExplicitCache() throws SQLException {
		oracleConnection.purgeExplicitCache();

	}

	@Override
	protected WrappedCallableStatement wrapCallableStatement(String sql,
			String ref, CallableStatement prepareCall) {
		if (prepareCall instanceof OracleCallableStatement) {
			return new WrappedOracleCallableStatement(prepareCall, sql, ref,
					getTransactionIdentifier(), this);
		}
		return super.wrapCallableStatement(sql, ref, prepareCall);
	}

	@Override
	protected WrappedPreparedStatement wrapPreparedStatement(String sql,
			String ref, PreparedStatement prepareStatement) {
		if (prepareStatement instanceof OraclePreparedStatement) {
			return new WrappedOraclePreparedStatement(prepareStatement, sql,
					ref, getTransactionIdentifier(), this);
		}
		return super.wrapPreparedStatement(sql, ref, prepareStatement);
	}

	@Override
	protected WrappedStatement wrapStatement(Statement s, String ref) {
		if (s instanceof OracleStatement) {
			return new WrappedOracleStatement(s, ref,
					getTransactionIdentifier(), this);
		}
		return super.wrapStatement(s, ref);
	}

	@Override
	public PreparedStatement getStatementWithKey(String paramString)
			throws SQLException {

		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED,
					ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = oracleConnection
				.getStatementWithKey(paramString);
		if (wrappingEnabled) {

			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(paramString, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;

		}
		return prepareStatement;
	}

	@Override
	public CallableStatement getCallWithKey(String paramString)
			throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED,
					ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		CallableStatement callableStatement = oracleConnection
				.getCallWithKey(paramString);
		if (wrappingEnabled) {

			CallableStatement pstmt = null;
			pstmt = wrapCallableStatement(paramString, ref, callableStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return callableStatement;
	}

	@Override
	public void setUsingXAFlag(boolean paramBoolean) {
		oracleConnection.setUsingXAFlag(paramBoolean);

	}

	@Override
	public void setXAErrorFlag(boolean paramBoolean) {
		oracleConnection.setXAErrorFlag(paramBoolean);

	}

	@Override
	public void shutdown(DatabaseShutdownMode paramDatabaseShutdownMode)
			throws SQLException {
		oracleConnection.shutdown(paramDatabaseShutdownMode);

	}

	@Override
	public void startup(String paramString, int paramInt) throws SQLException {
		oracleConnection.startup(paramString, paramInt);

	}

	@Override
	public void startup(DatabaseStartupMode paramDatabaseStartupMode)
			throws SQLException {
		oracleConnection.startup(paramDatabaseStartupMode);

	}

	@Override
	public PreparedStatement prepareStatementWithKey(String paramString)
			throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED,
					ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		PreparedStatement prepareStatement = oracleConnection
				.prepareStatementWithKey(paramString);
		if (wrappingEnabled) {

			PreparedStatement pstmt = null;
			pstmt = wrapPreparedStatement(paramString, ref, prepareStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;

		}
		return prepareStatement;
	}

	@Override
	public CallableStatement prepareCallWithKey(String paramString)
			throws SQLException {
		boolean wrappingEnabled = isWrappingEnabled();
		String ref = null;
		if (wrappingEnabled) {
			ref = getNextRefCount();
			JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED,
					ref);
			JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
		}
		CallableStatement callableStatement = oracleConnection
				.prepareCallWithKey(paramString);
		if (wrappingEnabled) {

			CallableStatement pstmt = null;
			pstmt = wrapCallableStatement(paramString, ref, callableStatement);
			JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
			return pstmt;
		}
		return callableStatement;
	}

	@Override
	public void setCreateStatementAsRefCursor(boolean paramBoolean) {
		oracleConnection.setCreateStatementAsRefCursor(paramBoolean);

	}

	@Override
	public boolean getCreateStatementAsRefCursor() {
		return oracleConnection.getCreateStatementAsRefCursor();
	}

	@Override
	public void setSessionTimeZone(String paramString) throws SQLException {
		oracleConnection.setSessionTimeZone(paramString);

	}

	@Override
	public String getSessionTimeZone() {
		return oracleConnection.getSessionTimeZone();
	}

	@Override
	public String getSessionTimeZoneOffset() throws SQLException {
		return oracleConnection.getSessionTimeZoneOffset();
	}

	@Override
	public Properties getProperties() {
		return oracleConnection.getProperties();
	}

	@Override
	public Connection _getPC() {
		return oracleConnection._getPC();
	}

	@Override
	public boolean isLogicalConnection() {
		return oracleConnection.isLogicalConnection();
	}

	@Override
	public void registerTAFCallback(OracleOCIFailover paramOracleOCIFailover,
			Object paramObject) throws SQLException {
		oracleConnection.registerTAFCallback(paramOracleOCIFailover,
				paramObject);

	}

	@Override
	public OracleConnection unwrap() {
		return oracleConnection.unwrap();
	}

	@Override
	public void setWrapper(OracleConnection paramOracleConnection) {
		oracleConnection.setWrapper(paramOracleConnection);

	}

	@Override
	public oracle.jdbc.internal.OracleConnection physicalConnectionWithin() {
		return oracleConnection.physicalConnectionWithin();
	}

	@Override
	public OracleSavepoint oracleSetSavepoint() throws SQLException {
		return oracleConnection.oracleSetSavepoint();
	}

	@Override
	public OracleSavepoint oracleSetSavepoint(String paramString)
			throws SQLException {
		return oracleConnection.oracleSetSavepoint(paramString);
	}

	@Override
	public void oracleRollback(OracleSavepoint paramOracleSavepoint)
			throws SQLException {
		oracleConnection.oracleRollback(paramOracleSavepoint);

	}

	@Override
	public void oracleReleaseSavepoint(OracleSavepoint paramOracleSavepoint)
			throws SQLException {
		oracleConnection.oracleReleaseSavepoint(paramOracleSavepoint);

	}

	@Override
	public void close(Properties paramProperties) throws SQLException {
		oracleConnection.close(paramProperties);

	}

	@Override
	public void close(int paramInt) throws SQLException {
		oracleConnection.close(paramInt);

	}

	@Override
	public boolean isProxySession() {
		return oracleConnection.isProxySession();
	}

	@Override
	public void applyConnectionAttributes(Properties paramProperties)
			throws SQLException {
		oracleConnection.applyConnectionAttributes(paramProperties);

	}

	@Override
	public Properties getConnectionAttributes() throws SQLException {
		return oracleConnection.getConnectionAttributes();
	}

	@Override
	public Properties getUnMatchedConnectionAttributes() throws SQLException {
		return oracleConnection.getUnMatchedConnectionAttributes();
	}

	@Override
	public void registerConnectionCacheCallback(
			OracleConnectionCacheCallback paramOracleConnectionCacheCallback,
			Object paramObject, int paramInt) throws SQLException {
		oracleConnection.registerConnectionCacheCallback(
				paramOracleConnectionCacheCallback, paramObject, paramInt);

	}

	@Override
	public void setConnectionReleasePriority(int paramInt) throws SQLException {
		oracleConnection.setConnectionReleasePriority(paramInt);

	}

	@Override
	public int getConnectionReleasePriority() throws SQLException {

		return oracleConnection.getConnectionReleasePriority();
	}

	@Override
	public void setPlsqlWarnings(String paramString) throws SQLException {
		oracleConnection.setPlsqlWarnings(paramString);

	}

	@Override
	public AQNotificationRegistration[] registerAQNotification(
			String[] paramArrayOfString, Properties[] paramArrayOfProperties,
			Properties paramProperties) throws SQLException {
		return oracleConnection.registerAQNotification(paramArrayOfString,
				paramArrayOfProperties, paramProperties);
	}

	@Override
	public void unregisterAQNotification(
			AQNotificationRegistration paramAQNotificationRegistration)
			throws SQLException {
		oracleConnection
				.unregisterAQNotification(paramAQNotificationRegistration);

	}

	@Override
	public AQMessage dequeue(String paramString,
			AQDequeueOptions paramAQDequeueOptions, byte[] paramArrayOfByte)
			throws SQLException {
		return oracleConnection.dequeue(paramString, paramAQDequeueOptions,
				paramArrayOfByte);
	}

	@Override
	public AQMessage dequeue(String paramString1,
			AQDequeueOptions paramAQDequeueOptions, String paramString2)
			throws SQLException {
		return oracleConnection.dequeue(paramString1, paramAQDequeueOptions,
				paramString2);
	}

	@Override
	public void enqueue(String paramString,
			AQEnqueueOptions paramAQEnqueueOptions, AQMessage paramAQMessage)
			throws SQLException {
		oracleConnection.enqueue(paramString, paramAQEnqueueOptions,
				paramAQMessage);

	}

	@Override
	public DatabaseChangeRegistration registerDatabaseChangeNotification(
			Properties paramProperties) throws SQLException {
		return oracleConnection
				.registerDatabaseChangeNotification(paramProperties);
	}

	@Override
	public DatabaseChangeRegistration getDatabaseChangeRegistration(int paramInt)
			throws SQLException {
		return oracleConnection.getDatabaseChangeRegistration(paramInt);
	}

	@Override
	public void unregisterDatabaseChangeNotification(
			DatabaseChangeRegistration paramDatabaseChangeRegistration)
			throws SQLException {
		oracleConnection
				.unregisterDatabaseChangeNotification(paramDatabaseChangeRegistration);

	}

	@Override
	public void unregisterDatabaseChangeNotification(int paramInt1,
			String paramString, int paramInt2) throws SQLException {
		oracleConnection.unregisterDatabaseChangeNotification(paramInt1,
				paramString, paramInt2);

	}

	@Override
	public void unregisterDatabaseChangeNotification(int paramInt)
			throws SQLException {
		oracleConnection.unregisterDatabaseChangeNotification(paramInt);

	}

	@Override
	public ARRAY createARRAY(String paramString, Object paramObject)
			throws SQLException {
		return oracleConnection.createARRAY(paramString, paramObject);
	}

	@Override
	public BINARY_DOUBLE createBINARY_DOUBLE(double paramDouble)
			throws SQLException {
		return oracleConnection.createBINARY_DOUBLE(paramDouble);
	}

	@Override
	public BINARY_FLOAT createBINARY_FLOAT(float paramFloat)
			throws SQLException {

		return oracleConnection.createBINARY_FLOAT(paramFloat);
	}

	@Override
	public DATE createDATE(Date paramDate) throws SQLException {
		return oracleConnection.createDATE(paramDate);
	}

	@Override
	public DATE createDATE(Time paramTime) throws SQLException {
		return oracleConnection.createDATE(paramTime);
	}

	@Override
	public DATE createDATE(Timestamp paramTimestamp) throws SQLException {
		return oracleConnection.createDATE(paramTimestamp);
	}

	@Override
	public DATE createDATE(Date paramDate, Calendar paramCalendar)
			throws SQLException {
		return oracleConnection.createDATE(paramDate, paramCalendar);
	}

	@Override
	public DATE createDATE(Time paramTime, Calendar paramCalendar)
			throws SQLException {
		return oracleConnection.createDATE(paramTime, paramCalendar);

	}

	@Override
	public DATE createDATE(Timestamp paramTimestamp, Calendar paramCalendar)
			throws SQLException {
		return oracleConnection.createDATE(paramTimestamp, paramCalendar);

	}

	@Override
	public DATE createDATE(String paramString) throws SQLException {
		return oracleConnection.createDATE(paramString);

	}

	@Override
	public INTERVALDS createINTERVALDS(String paramString) throws SQLException {
		return oracleConnection.createINTERVALDS(paramString);
	}

	@Override
	public INTERVALYM createINTERVALYM(String paramString) throws SQLException {
		return oracleConnection.createINTERVALYM(paramString);
	}

	@Override
	public NUMBER createNUMBER(boolean paramBoolean) throws SQLException {
		return oracleConnection.createNUMBER(paramBoolean);
	}

	@Override
	public NUMBER createNUMBER(byte paramByte) throws SQLException {
		return oracleConnection.createNUMBER(paramByte);

	}

	@Override
	public NUMBER createNUMBER(short paramShort) throws SQLException {
		return oracleConnection.createNUMBER(paramShort);

	}

	@Override
	public NUMBER createNUMBER(int paramInt) throws SQLException {
		return oracleConnection.createNUMBER(paramInt);

	}

	@Override
	public NUMBER createNUMBER(long paramLong) throws SQLException {
		return oracleConnection.createNUMBER(paramLong);

	}

	@Override
	public NUMBER createNUMBER(float paramFloat) throws SQLException {
		return oracleConnection.createNUMBER(paramFloat);

	}

	@Override
	public NUMBER createNUMBER(double paramDouble) throws SQLException {
		return oracleConnection.createNUMBER(paramDouble);

	}

	@Override
	public NUMBER createNUMBER(BigDecimal paramBigDecimal) throws SQLException {
		return oracleConnection.createNUMBER(paramBigDecimal);

	}

	@Override
	public NUMBER createNUMBER(BigInteger paramBigInteger) throws SQLException {
		return oracleConnection.createNUMBER(paramBigInteger);

	}

	@Override
	public NUMBER createNUMBER(String paramString, int paramInt)
			throws SQLException {
		return oracleConnection.createNUMBER(paramString, paramInt);

	}

	@Override
	public TIMESTAMP createTIMESTAMP(Date paramDate) throws SQLException {
		return oracleConnection.createTIMESTAMP(paramDate);
	}

	@Override
	public TIMESTAMP createTIMESTAMP(DATE paramDATE) throws SQLException {
		return oracleConnection.createTIMESTAMP(paramDATE);
	}

	@Override
	public TIMESTAMP createTIMESTAMP(Time paramTime) throws SQLException {
		return oracleConnection.createTIMESTAMP(paramTime);
	}

	@Override
	public TIMESTAMP createTIMESTAMP(Timestamp paramTimestamp)
			throws SQLException {
		return oracleConnection.createTIMESTAMP(paramTimestamp);
	}

	@Override
	public TIMESTAMP createTIMESTAMP(String paramString) throws SQLException {
		return oracleConnection.createTIMESTAMP(paramString);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(Date paramDate) throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramDate);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(Date paramDate, Calendar paramCalendar)
			throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramDate, paramCalendar);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(Time paramTime) throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramTime);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(Time paramTime, Calendar paramCalendar)
			throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramTime, paramCalendar);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(Timestamp paramTimestamp)
			throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramTimestamp);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(Timestamp paramTimestamp,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection
				.createTIMESTAMPTZ(paramTimestamp, paramCalendar);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(String paramString)
			throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramString);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(String paramString,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramString, paramCalendar);
	}

	@Override
	public TIMESTAMPTZ createTIMESTAMPTZ(DATE paramDATE) throws SQLException {
		return oracleConnection.createTIMESTAMPTZ(paramDATE);
	}

	@Override
	public TIMESTAMPLTZ createTIMESTAMPLTZ(Date paramDate,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection.createTIMESTAMPLTZ(paramDate, paramCalendar);
	}

	@Override
	public TIMESTAMPLTZ createTIMESTAMPLTZ(Time paramTime,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection.createTIMESTAMPLTZ(paramTime, paramCalendar);
	}

	@Override
	public TIMESTAMPLTZ createTIMESTAMPLTZ(Timestamp paramTimestamp,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection.createTIMESTAMPLTZ(paramTimestamp,
				paramCalendar);
	}

	@Override
	public TIMESTAMPLTZ createTIMESTAMPLTZ(String paramString,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection.createTIMESTAMPLTZ(paramString, paramCalendar);
	}

	@Override
	public TIMESTAMPLTZ createTIMESTAMPLTZ(DATE paramDATE,
			Calendar paramCalendar) throws SQLException {
		return oracleConnection.createTIMESTAMPLTZ(paramDATE, paramCalendar);
	}

	@Override
	public void cancel() throws SQLException {
		oracleConnection.cancel();

	}

	@Override
	public void abort() throws SQLException {
		oracleConnection.abort();

	}

	@Override
	public TypeDescriptor[] getAllTypeDescriptorsInCurrentSchema()
			throws SQLException {
		return oracleConnection.getAllTypeDescriptorsInCurrentSchema();
	}

	@Override
	public TypeDescriptor[] getTypeDescriptorsFromListInCurrentSchema(
			String[] paramArrayOfString) throws SQLException {
		return oracleConnection
				.getTypeDescriptorsFromListInCurrentSchema(paramArrayOfString);
	}

	@Override
	public TypeDescriptor[] getTypeDescriptorsFromList(
			String[][] paramArrayOfString) throws SQLException {
		return oracleConnection.getTypeDescriptorsFromList(paramArrayOfString);
	}

	@Override
	public String getDataIntegrityAlgorithmName() throws SQLException {
		return oracleConnection.getDataIntegrityAlgorithmName();
	}

	@Override
	public String getEncryptionAlgorithmName() throws SQLException {
		return oracleConnection.getEncryptionAlgorithmName();
	}

	@Override
	public String getAuthenticationAdaptorName() throws SQLException {
		return oracleConnection.getAuthenticationAdaptorName();
	}

	@Override
	public boolean isUsable() {
		return oracleConnection.isUsable();
	}

	@Override
	public void setDefaultTimeZone(TimeZone paramTimeZone) throws SQLException {
		oracleConnection.setDefaultTimeZone(paramTimeZone);

	}

	@Override
	public TimeZone getDefaultTimeZone() throws SQLException {
		return oracleConnection.getDefaultTimeZone();
	}

}
