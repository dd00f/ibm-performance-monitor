package com.ibm.issw.jdbc.wrappers;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.ietf.jgss.GSSCredential;

import com.ibm.db2.jcc.DB2Connection;
import com.ibm.db2.jcc.DB2SystemMonitor;
import com.ibm.issw.jdbc.profiler.JdbcProfiler;

/**
 * 
 * WrappedDB2Connection
 */
public class WrappedDB2Connection extends WrappedConnection implements DB2Connection{

	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;
	
	private final DB2Connection connection;

	/**
	 * ctor
	 * @param connection the connection
	 */
	public WrappedDB2Connection(DB2Connection connection) {
		super(connection);
		this.connection = connection;
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#alternateWasUsedOnConnect()
	 */
	@Override
	public boolean alternateWasUsedOnConnect() throws SQLException {
		return connection.alternateWasUsedOnConnect();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#changeDB2Password(java.lang.String, java.lang.String)
	 */
	@Override
	public void changeDB2Password(String arg0, String arg1) throws SQLException {
		connection.changeDB2Password(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#deregisterDB2XmlObject(java.lang.String, java.lang.String)
	 */
	@Override
	public void deregisterDB2XmlObject(String arg0, String arg1)
			throws SQLException {
		connection.deregisterDB2XmlObject(arg0, arg1);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#enableJccDateTimeMutation(boolean)
	 */
	@Override
	public void enableJccDateTimeMutation(boolean arg0) throws SQLException {
		connection.enableJccDateTimeMutation(arg0);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#fct()
	 */
	@Override
	public void fct() {
		connection.fct();
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2ClientAccountingInformation()
	 */
	@Deprecated
	@Override
	public String getDB2ClientAccountingInformation() throws SQLException {
		//$ANALYSIS-IGNORE
		return connection.getDB2ClientAccountingInformation();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2ClientApplicationInformation()
	 */
	@Deprecated
	@Override
	public String getDB2ClientApplicationInformation() throws SQLException {
		//$ANALYSIS-IGNORE
		return connection.getDB2ClientApplicationInformation();
	}
	/*
	 * 
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2ClientProgramId()
	 */
	@Override
	public String getDB2ClientProgramId() throws SQLException {
		return connection.getDB2ClientProgramId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2ClientUser()
	 */
	@Deprecated
	@Override
	public String getDB2ClientUser() throws SQLException {
		//$ANALYSIS-IGNORE
		return connection.getDB2ClientUser();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2ClientWorkstation()
	 */
	@Deprecated
	@Override
	public String getDB2ClientWorkstation() throws SQLException {
		//$ANALYSIS-IGNORE
		return connection.getDB2ClientWorkstation();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2Correlator()
	 */
	@Override
	public String getDB2Correlator() throws SQLException {
		return connection.getDB2Correlator();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2CurrentPackagePath()
	 */
	@Override
	public String getDB2CurrentPackagePath() throws SQLException {
		return connection.getDB2CurrentPackagePath();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2CurrentPackageSet()
	 */
	@Override
	public String getDB2CurrentPackageSet() throws SQLException {
		
		return connection.getDB2CurrentPackageSet();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2SecurityMechanism()
	 */
	@Override
	public int getDB2SecurityMechanism() throws SQLException {
		
		return connection.getDB2SecurityMechanism();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDB2SystemMonitor()
	 */
	@Override
	public DB2SystemMonitor getDB2SystemMonitor() throws SQLException {
		
		return connection.getDB2SystemMonitor();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getDBProgressiveStreaming()
	 */
	@Override
	public int getDBProgressiveStreaming() throws SQLException {
		
		return connection.getDBProgressiveStreaming();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getJCCLogWriter()
	 */
	@Override
	public PrintWriter getJCCLogWriter() throws SQLException {
		
		return connection.getJCCLogWriter();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#getJccLogWriter()
	 */
	@Override
	public PrintWriter getJccLogWriter() throws SQLException {
		
		return connection.getJccLogWriter();
	}
//	/*
//	 * (non-Javadoc)
//	 * @see com.ibm.db2.jcc.DB2Connection#getJccSpecialRegisterProperties()
//	 */
//	@Override
//	public Properties getJccSpecialRegisterProperties() throws SQLException {
//		
//		return connection.getJccSpecialRegisterProperties();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.ibm.db2.jcc.DB2Connection#getMaxRowsetSize()
//	 */
//	@Override
//	public int getMaxRowsetSize() throws SQLException {
//		
//		return connection.getMaxRowsetSize();
//	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#installDB2JavaStoredProcedure(java.io.InputStream, int, java.lang.String)
	 */
	@Override
	public void installDB2JavaStoredProcedure(InputStream arg0, int arg1,
			String arg2) throws SQLException {
		
		connection.installDB2JavaStoredProcedure(arg0, arg1, arg2);
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#isDB2Alive()
	 */
	@Deprecated
	@Override
	public boolean isDB2Alive() throws SQLException {
		
		//$ANALYSIS-IGNORE
		return connection.isDB2Alive();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#isDB2GatewayConnection()
	 */
	@Override
	public boolean isDB2GatewayConnection() throws SQLException {
		
		return connection.isDB2GatewayConnection();
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#isDBValid(boolean, int)
	 */
	@Override
	public boolean isDBValid(boolean arg0, int arg1) throws SQLException {
		
		return connection.isDBValid(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#isInDB2UnitOfWork()
	 */
	@Override
	public boolean isInDB2UnitOfWork() throws SQLException {
		
		return connection.isInDB2UnitOfWork();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#prepareDB2OptimisticLockingQuery(java.lang.String, int)
	 */
	@Override
    public PreparedStatement prepareDB2OptimisticLockingQuery(String arg0, int arg1) throws SQLException
    {

        boolean wrappingEnabled = isWrappingEnabled();
        String ref = null;
        if (wrappingEnabled)
        {
            ref = getNextRefCount();
            JdbcProfiler.getInstance().setStatementType(JdbcProfiler.PREPARED, ref);
            JdbcProfiler.getInstance().start(JdbcProfiler.OP_PREPARE, ref);
        }
        PreparedStatement prepareStatement = connection.prepareDB2OptimisticLockingQuery(arg0, arg1);
        if (wrappingEnabled)
        {

            PreparedStatement pstmt = null;
            pstmt = wrapPreparedStatement(arg0, ref, prepareStatement);
            JdbcProfiler.getInstance().stop(JdbcProfiler.OP_PREPARE, ref);
            return pstmt;

        }
        return prepareStatement;
    }
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#reconfigureDB2Connection(java.util.Properties)
	 */
	@Override
	public void reconfigureDB2Connection(Properties arg0) throws SQLException {
		
		connection.reconfigureDB2Connection(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#registerDB2XmlDtd(java.lang.String[], java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void registerDB2XmlDtd(String[] arg0, String[] arg1, String arg2,
			String arg3, String arg4) throws SQLException {
		
		connection.registerDB2XmlDtd(arg0, arg1, arg2, arg3, arg4);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#registerDB2XmlDtd(java.lang.String[], java.lang.String[], java.lang.String, java.lang.String, java.io.InputStream, int)
	 */
	@Override
	public void registerDB2XmlDtd(String[] arg0, String[] arg1, String arg2,
			String arg3, InputStream arg4, int arg5) throws SQLException {
		
		connection.registerDB2XmlDtd(arg0, arg1, arg2, arg3, arg4, arg5);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#registerDB2XmlExternalEntity(java.lang.String[], java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void registerDB2XmlExternalEntity(String[] arg0, String[] arg1,
			String arg2, String arg3, String arg4) throws SQLException {
		
		connection.registerDB2XmlExternalEntity(arg0, arg1, arg2, arg3, arg4);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#registerDB2XmlExternalEntity(java.lang.String[], java.lang.String[], java.lang.String, java.lang.String, java.io.InputStream, int)
	 */
	@Override
	public void registerDB2XmlExternalEntity(String[] arg0, String[] arg1,
			String arg2, String arg3, InputStream arg4, int arg5)
			throws SQLException {
		
		connection.registerDB2XmlExternalEntity(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#registerDB2XmlSchema(java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String, boolean)
	 */
	@Override
	public void registerDB2XmlSchema(String[] arg0, String[] arg1,
			String[] arg2, String[] arg3, String[] arg4, String arg5,
			boolean arg6) throws SQLException {
		
		connection.registerDB2XmlSchema(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#registerDB2XmlSchema(java.lang.String[], java.lang.String[], java.lang.String[], java.io.InputStream[], int[], java.io.InputStream[], int[], java.io.InputStream, int, boolean)
	 */
	@Override
	public void registerDB2XmlSchema(String[] arg0, String[] arg1,
			String[] arg2, InputStream[] arg3, int[] arg4, InputStream[] arg5,
			int[] arg6, InputStream arg7, int arg8, boolean arg9)
			throws SQLException {
		
		connection.registerDB2XmlSchema(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#removeDB2JavaStoredProcedure(java.lang.String)
	 */
	@Override
	public void removeDB2JavaStoredProcedure(String arg0) throws SQLException {
		
		connection.removeDB2JavaStoredProcedure(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#replaceDB2JavaStoredProcedure(java.io.InputStream, int, java.lang.String)
	 */
	@Override
	public void replaceDB2JavaStoredProcedure(InputStream arg0, int arg1,
			String arg2) throws SQLException {
		
		connection.replaceDB2JavaStoredProcedure(arg0, arg1, arg2);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#resetDB2Connection()
	 */
	@Override
	public void resetDB2Connection() throws SQLException {
		
		connection.resetDB2Connection();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#resetDB2Connection(java.lang.String, java.lang.String)
	 */
	@Override
	public void resetDB2Connection(String arg0, String arg1)
			throws SQLException {
		
		connection.resetDB2Connection(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#reuseDB2Connection(java.util.Properties)
	 */
	@Override
	public void reuseDB2Connection(Properties arg0) throws SQLException {
		
		connection.reuseDB2Connection(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#reuseDB2Connection(org.ietf.jgss.GSSCredential, java.util.Properties)
	 */
	@Override
	public void reuseDB2Connection(GSSCredential arg0, Properties arg1)
			throws SQLException {
		connection.reuseDB2Connection(arg0, arg1);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#reuseDB2Connection(java.lang.String, java.lang.String, java.util.Properties)
	 */
	@Override
	public void reuseDB2Connection(String arg0, String arg1, Properties arg2)
			throws SQLException {
		
		connection.reuseDB2Connection(arg0, arg1, arg2);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#reuseDB2Connection(byte[], org.ietf.jgss.GSSCredential, java.lang.String, byte[], java.lang.String, java.util.Properties)
	 */
	@Override
	public void reuseDB2Connection(byte[] arg0, GSSCredential arg1,
			String arg2, byte[] arg3, String arg4, Properties arg5)
			throws SQLException {
		
		connection.reuseDB2Connection(arg0, arg1, arg2, arg3, arg4, arg5);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#reuseDB2Connection(byte[], java.lang.String, java.lang.String, java.lang.String, byte[], java.lang.String, java.util.Properties)
	 */
	@Override
	public void reuseDB2Connection(byte[] arg0, String arg1, String arg2,
			String arg3, byte[] arg4, String arg5, Properties arg6)
			throws SQLException {
		
		connection.reuseDB2Connection(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientAccountingInformation(java.lang.String)
	 */
	@Deprecated
	@Override
	public void setDB2ClientAccountingInformation(String arg0)
			throws SQLException {
		
		//$ANALYSIS-IGNORE
		connection.setDB2ClientAccountingInformation(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientApplicationInformation(java.lang.String)
	 */
	@Deprecated
	@Override
	public void setDB2ClientApplicationInformation(String arg0)
			throws SQLException {
		//$ANALYSIS-IGNORE
		connection.setDB2ClientApplicationInformation(arg0);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientDebugInfo(java.lang.String)
	 */
	@Override
	public void setDB2ClientDebugInfo(String arg0) throws SQLException {
		connection.setDB2ClientDebugInfo(arg0);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientDebugInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void setDB2ClientDebugInfo(String arg0, String arg1)
			throws SQLException {
		
		connection.setDB2ClientDebugInfo(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientProgramId(java.lang.String)
	 */
	@Override
	public void setDB2ClientProgramId(String arg0) throws SQLException {
		
		connection.setDB2ClientProgramId(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientUser(java.lang.String)
	 */
	@Deprecated
	@Override
	public void setDB2ClientUser(String arg0) throws SQLException {
		
		//$ANALYSIS-IGNORE
		connection.setDB2ClientUser(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2ClientWorkstation(java.lang.String)
	 */
	@Deprecated
	@Override
	public void setDB2ClientWorkstation(String arg0) throws SQLException {
		
		//$ANALYSIS-IGNORE
		connection.setDB2ClientWorkstation(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2CurrentPackagePath(java.lang.String)
	 */
	@Override
	public void setDB2CurrentPackagePath(String arg0) throws SQLException {
		
		connection.setDB2CurrentPackagePath(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2CurrentPackageSet(java.lang.String)
	 */
	@Override
	public void setDB2CurrentPackageSet(String arg0) throws SQLException {
		
		connection.setDB2CurrentPackageSet(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDB2eWLMCorrelator(byte[])
	 */
	@Override
	public void setDB2eWLMCorrelator(byte[] arg0) throws SQLException {
		
		connection.setDB2eWLMCorrelator(arg0);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setDBProgressiveStreaming(int)
	 */
	@Override
	public void setDBProgressiveStreaming(int arg0) throws SQLException {
		connection.setDBProgressiveStreaming(arg0);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setJCCLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setJCCLogWriter(PrintWriter arg0) throws SQLException {
		
		connection.setJCCLogWriter(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setJCCLogWriter(java.io.PrintWriter, int)
	 */
	@Override
	public void setJCCLogWriter(PrintWriter arg0, int arg1) throws SQLException {
		
		connection.setJCCLogWriter(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setJccLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setJccLogWriter(PrintWriter arg0) throws SQLException {
		
		connection.setJccLogWriter(arg0);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setJccLogWriter(java.io.PrintWriter, int)
	 */
	@Override
	public void setJccLogWriter(PrintWriter arg0, int arg1) throws SQLException {
		
		connection.setJccLogWriter(arg0, arg1);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setJccLogWriter(java.lang.String, boolean, int)
	 */
	@Override
	public void setJccLogWriter(String arg0, boolean arg1, int arg2)
			throws SQLException {
		connection.setJccLogWriter(arg0, arg1, arg2);
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#setMaxRowsetSize(int)
	 */
//	@Override
//	public void setMaxRowsetSize(int arg0) throws SQLException {
//		
//		connection.setMaxRowsetSize(arg0);
//	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#unsetJccLogWriter()
	 */
	@Override
	public void unsetJccLogWriter() throws SQLException {
		
		connection.unsetJccLogWriter();
	}
	/*
	 * (non-Javadoc)
	 * @see com.ibm.db2.jcc.DB2Connection#updateDB2XmlSchema(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void updateDB2XmlSchema(String arg0, String arg1, String arg2,
			String arg3, boolean arg4) throws SQLException {
		
		connection.updateDB2XmlSchema(arg0, arg1, arg2, arg3, arg4);
	}
	@Override
	public String getDB2ClientCorrelationToken() throws SQLException {
		return connection.getDB2ClientCorrelationToken();
	}
	@Override
	public int getDBConcurrentAccessResolution() throws SQLException {
		return connection.getDBConcurrentAccessResolution();
	}
	@Override
	public int getDBStatementConcentrator() throws SQLException {
		return connection.getDBStatementConcentrator();
	}
	@Override
	public Properties getJccSpecialRegisterProperties() throws SQLException {
		return connection.getJccSpecialRegisterProperties();
	}
	@Override
	public int getMaxRowsetSize() throws SQLException {
		return connection.getMaxRowsetSize();
	}
	@Override
	public boolean getSavePointUniqueOption() throws SQLException {
		return connection.getSavePointUniqueOption();
	}
	@Override
	public void setDB2ClientCorrelationToken(String arg0) throws SQLException {
		connection.setDB2ClientCorrelationToken(arg0);
	}
	@Override
	public void setDBConcurrentAccessResolution(int arg0) throws SQLException {
		connection.setDBConcurrentAccessResolution(arg0);
	}
	@Override
	public void setDBStatementConcentrator(int arg0) throws SQLException {
		connection.setDBStatementConcentrator(arg0);
		
	}
	@Override
	public void setGlobalSessionVariable(String arg0, String arg1)
			throws SQLException {
		connection.setGlobalSessionVariable(arg0, arg1);
		
	}
	@Override
	public void setMaxRowsetSize(int arg0) throws SQLException {
		connection.setMaxRowsetSize(arg0);
		
	}
	@Override
	public void setSavePointUniqueOption(boolean arg0) throws SQLException {
		connection.setSavePointUniqueOption(arg0);
	}

}
