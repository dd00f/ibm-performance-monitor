package com.ibm.issw.jdbc.wrappers;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.sql.ARRAY;
import oracle.sql.BFILE;
import oracle.sql.BLOB;
import oracle.sql.CHAR;
import oracle.sql.CLOB;
import oracle.sql.CustomDatum;
import oracle.sql.CustomDatumFactory;
import oracle.sql.DATE;
import oracle.sql.Datum;
import oracle.sql.INTERVALDS;
import oracle.sql.INTERVALYM;
import oracle.sql.NUMBER;
import oracle.sql.OPAQUE;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;
import oracle.sql.RAW;
import oracle.sql.REF;
import oracle.sql.ROWID;
import oracle.sql.STRUCT;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;

import com.ibm.issw.jdbc.profiler.JdbcEvent;

@SuppressWarnings("deprecation")
public class WrappedOracleCalculatedResultSet extends WrappedCalculatedResultSet implements
		OracleResultSet {

	private final OracleResultSet oracleRs;

	public WrappedOracleCalculatedResultSet(ResultSet resultSet, String reference,
			JdbcEvent event) {
		super(resultSet, reference, event);
		oracleRs = (OracleResultSet) resultSet;
	}

	@Override
	public ARRAY getARRAY(int paramInt) throws SQLException {
		return oracleRs.getARRAY(paramInt);
	}

	@Override
	public InputStream getAsciiStream(int paramInt) throws SQLException {
		return oracleRs.getAsciiStream(paramInt);
	}

	@Override
	public BFILE getBFILE(int paramInt) throws SQLException {
		return oracleRs.getBFILE(paramInt);
	}

	@Override
	public BFILE getBfile(int paramInt) throws SQLException {
		return oracleRs.getBfile(paramInt);
	}

	@Override
	public InputStream getBinaryStream(int paramInt) throws SQLException {
		return oracleRs.getBinaryStream(paramInt);
	}

	@Override
	public InputStream getBinaryStream(String paramString) throws SQLException {
		return oracleRs.getBinaryStream(paramString);
	}

	@Override
	public BLOB getBLOB(int paramInt) throws SQLException {
		return oracleRs.getBLOB(paramInt);
	}

	@Override
	public CHAR getCHAR(int paramInt) throws SQLException {
		return oracleRs.getCHAR(paramInt);
	}

	@Override
	public CLOB getCLOB(int paramInt) throws SQLException {
		return oracleRs.getCLOB(paramInt);
	}

	@Override
	public ResultSet getCursor(int paramInt) throws SQLException {
		return oracleRs.getCursor(paramInt);
	}

	@Override
	public CustomDatum getCustomDatum(int paramInt,
			CustomDatumFactory paramCustomDatumFactory) throws SQLException {
		return oracleRs.getCustomDatum(paramInt, paramCustomDatumFactory);
	}

	@Override
	public ORAData getORAData(int paramInt, ORADataFactory paramORADataFactory)
			throws SQLException {
		return oracleRs.getORAData(paramInt, paramORADataFactory);
	}

	@Override
	public DATE getDATE(int paramInt) throws SQLException {
		return oracleRs.getDATE(paramInt);
	}

	@Override
	public NUMBER getNUMBER(int paramInt) throws SQLException {
		return oracleRs.getNUMBER(paramInt);
	}

	@Override
	public OPAQUE getOPAQUE(int paramInt) throws SQLException {
		return oracleRs.getOPAQUE(paramInt);
	}

	@Override
	public Datum getOracleObject(int paramInt) throws SQLException {
		return oracleRs.getOracleObject(paramInt);
	}

	@Override
	public RAW getRAW(int paramInt) throws SQLException {
		return oracleRs.getRAW(paramInt);
	}

	@Override
	public REF getREF(int paramInt) throws SQLException {
		return oracleRs.getREF(paramInt);
	}

	@Override
	public ROWID getROWID(int paramInt) throws SQLException {
		return oracleRs.getROWID(paramInt);
	}

	@Override
	public STRUCT getSTRUCT(int paramInt) throws SQLException {
		return oracleRs.getSTRUCT(paramInt);
	}

	@Override
	public INTERVALYM getINTERVALYM(int paramInt) throws SQLException {
		return oracleRs.getINTERVALYM(paramInt);
	}

	@Override
	public INTERVALDS getINTERVALDS(int paramInt) throws SQLException {
		return oracleRs.getINTERVALDS(paramInt);
	}

	@Override
	public TIMESTAMP getTIMESTAMP(int paramInt) throws SQLException {
		return oracleRs.getTIMESTAMP(paramInt);
	}

	@Override
	public TIMESTAMPTZ getTIMESTAMPTZ(int paramInt) throws SQLException {
		return oracleRs.getTIMESTAMPTZ(paramInt);
	}

	@Override
	public TIMESTAMPLTZ getTIMESTAMPLTZ(int paramInt) throws SQLException {
		return oracleRs.getTIMESTAMPLTZ(paramInt);
	}

	@Override
	public InputStream getUnicodeStream(int paramInt) throws SQLException {
		return oracleRs.getUnicodeStream(paramInt);
	}

	@Override
	public InputStream getUnicodeStream(String paramString) throws SQLException {
		return oracleRs.getUnicodeStream(paramString);
	}

	@Override
	public ARRAY getARRAY(String paramString) throws SQLException {
		return oracleRs.getARRAY(paramString);
	}

	@Override
	public BFILE getBfile(String paramString) throws SQLException {

		return oracleRs.getBfile(paramString);
	}

	@Override
	public BFILE getBFILE(String paramString) throws SQLException {

		return oracleRs.getBFILE(paramString);
	}

	@Override
	public BLOB getBLOB(String paramString) throws SQLException {

		return oracleRs.getBLOB(paramString);
	}

	@Override
	public CHAR getCHAR(String paramString) throws SQLException {

		return oracleRs.getCHAR(paramString);
	}

	@Override
	public CLOB getCLOB(String paramString) throws SQLException {

		return oracleRs.getCLOB(paramString);
	}

	@Override
	public OPAQUE getOPAQUE(String paramString) throws SQLException {

		return oracleRs.getOPAQUE(paramString);
	}

	@Override
	public INTERVALYM getINTERVALYM(String paramString) throws SQLException {

		return oracleRs.getINTERVALYM(paramString);
	}

	@Override
	public INTERVALDS getINTERVALDS(String paramString) throws SQLException {

		return oracleRs.getINTERVALDS(paramString);
	}

	@Override
	public TIMESTAMP getTIMESTAMP(String paramString) throws SQLException {

		return oracleRs.getTIMESTAMP(paramString);
	}

	@Override
	public TIMESTAMPTZ getTIMESTAMPTZ(String paramString) throws SQLException {

		return oracleRs.getTIMESTAMPTZ(paramString);
	}

	@Override
	public TIMESTAMPLTZ getTIMESTAMPLTZ(String paramString) throws SQLException {

		return oracleRs.getTIMESTAMPLTZ(paramString);
	}

	@Override
	public ResultSet getCursor(String paramString) throws SQLException {

		return oracleRs.getCursor(paramString);
	}

	@Override
	public CustomDatum getCustomDatum(String paramString,
			CustomDatumFactory paramCustomDatumFactory) throws SQLException {

		return oracleRs.getCustomDatum(paramString, paramCustomDatumFactory);
	}

	@Override
	public ORAData getORAData(String paramString,
			ORADataFactory paramORADataFactory) throws SQLException {

		return oracleRs.getORAData(paramString, paramORADataFactory);
	}

	@Override
	public DATE getDATE(String paramString) throws SQLException {

		return oracleRs.getDATE(paramString);
	}

	@Override
	public NUMBER getNUMBER(String paramString) throws SQLException {

		return oracleRs.getNUMBER(paramString);
	}

	@Override
	public Datum getOracleObject(String paramString) throws SQLException {

		return oracleRs.getOracleObject(paramString);
	}

	@Override
	public RAW getRAW(String paramString) throws SQLException {

		return oracleRs.getRAW(paramString);
	}

	@Override
	public REF getREF(String paramString) throws SQLException {

		return oracleRs.getREF(paramString);
	}

	@Override
	public ROWID getROWID(String paramString) throws SQLException {

		return oracleRs.getROWID(paramString);
	}

	@Override
	public STRUCT getSTRUCT(String paramString) throws SQLException {

		return oracleRs.getSTRUCT(paramString);
	}

	@Override
	public void updateARRAY(int paramInt, ARRAY paramARRAY) throws SQLException {
		oracleRs.updateARRAY(paramInt, paramARRAY);

	}

	@Override
	public void updateARRAY(String paramString, ARRAY paramARRAY)
			throws SQLException {

		oracleRs.updateARRAY(paramString, paramARRAY);
	}

	@Override
	public void updateBfile(int paramInt, BFILE paramBFILE) throws SQLException {
		oracleRs.updateBfile(paramInt, paramBFILE);

	}

	@Override
	public void updateBFILE(int paramInt, BFILE paramBFILE) throws SQLException {
		oracleRs.updateBFILE(paramInt, paramBFILE);

	}

	@Override
	public void updateBfile(String paramString, BFILE paramBFILE)
			throws SQLException {
		oracleRs.updateBfile(paramString, paramBFILE);

	}

	@Override
	public void updateBFILE(String paramString, BFILE paramBFILE)
			throws SQLException {
		oracleRs.updateBFILE(paramString, paramBFILE);

	}

	@Override
	public void updateBLOB(int paramInt, BLOB paramBLOB) throws SQLException {
		oracleRs.updateBLOB(paramInt, paramBLOB);

	}

	@Override
	public void updateBLOB(String paramString, BLOB paramBLOB)
			throws SQLException {
		oracleRs.updateBLOB(paramString, paramBLOB);

	}

	@Override
	public void updateCHAR(int paramInt, CHAR paramCHAR) throws SQLException {
		oracleRs.updateCHAR(paramInt, paramCHAR);

	}

	@Override
	public void updateCHAR(String paramString, CHAR paramCHAR)
			throws SQLException {
		oracleRs.updateCHAR(paramString, paramCHAR);

	}

	@Override
	public void updateCLOB(int paramInt, CLOB paramCLOB) throws SQLException {
		oracleRs.updateCLOB(paramInt, paramCLOB);

	}

	@Override
	public void updateCLOB(String paramString, CLOB paramCLOB)
			throws SQLException {
		oracleRs.updateCLOB(paramString, paramCLOB);

	}

	@Override
	public void updateCustomDatum(int paramInt, CustomDatum paramCustomDatum)
			throws SQLException {
		oracleRs.updateCustomDatum(paramInt, paramCustomDatum);

	}

	@Override
	public void updateORAData(int paramInt, ORAData paramORAData)
			throws SQLException {
		oracleRs.updateORAData(paramInt, paramORAData);

	}

	@Override
	public void updateCustomDatum(String paramString,
			CustomDatum paramCustomDatum) throws SQLException {
		oracleRs.updateCustomDatum(paramString, paramCustomDatum);

	}

	@Override
	public void updateORAData(String paramString, ORAData paramORAData)
			throws SQLException {
		oracleRs.updateORAData(paramString, paramORAData);

	}

	@Override
	public void updateDATE(int paramInt, DATE paramDATE) throws SQLException {
		oracleRs.updateDATE(paramInt, paramDATE);

	}

	@Override
	public void updateDATE(String paramString, DATE paramDATE)
			throws SQLException {
		oracleRs.updateDATE(paramString, paramDATE);

	}

	@Override
	public void updateINTERVALYM(int paramInt, INTERVALYM paramINTERVALYM)
			throws SQLException {
		oracleRs.updateINTERVALYM(paramInt, paramINTERVALYM);

	}

	@Override
	public void updateINTERVALYM(String paramString, INTERVALYM paramINTERVALYM)
			throws SQLException {
		oracleRs.updateINTERVALYM(paramString, paramINTERVALYM);

	}

	@Override
	public void updateINTERVALDS(int paramInt, INTERVALDS paramINTERVALDS)
			throws SQLException {
		oracleRs.updateINTERVALDS(paramInt, paramINTERVALDS);

	}

	@Override
	public void updateINTERVALDS(String paramString, INTERVALDS paramINTERVALDS)
			throws SQLException {
		oracleRs.updateINTERVALDS(paramString, paramINTERVALDS);

	}

	@Override
	public void updateTIMESTAMP(int paramInt, TIMESTAMP paramTIMESTAMP)
			throws SQLException {
		oracleRs.updateTIMESTAMP(paramInt, paramTIMESTAMP);

	}

	@Override
	public void updateTIMESTAMP(String paramString, TIMESTAMP paramTIMESTAMP)
			throws SQLException {
		oracleRs.updateTIMESTAMP(paramString, paramTIMESTAMP);

	}

	@Override
	public void updateTIMESTAMPTZ(int paramInt, TIMESTAMPTZ paramTIMESTAMPTZ)
			throws SQLException {
		oracleRs.updateTIMESTAMPTZ(paramInt, paramTIMESTAMPTZ);

	}

	@Override
	public void updateTIMESTAMPTZ(String paramString,
			TIMESTAMPTZ paramTIMESTAMPTZ) throws SQLException {
		oracleRs.updateTIMESTAMPTZ(paramString, paramTIMESTAMPTZ);

	}

	@Override
	public void updateTIMESTAMPLTZ(int paramInt, TIMESTAMPLTZ paramTIMESTAMPLTZ)
			throws SQLException {
		oracleRs.updateTIMESTAMPLTZ(paramInt, paramTIMESTAMPLTZ);

	}

	@Override
	public void updateTIMESTAMPLTZ(String paramString,
			TIMESTAMPLTZ paramTIMESTAMPLTZ) throws SQLException {
		oracleRs.updateTIMESTAMPLTZ(paramString, paramTIMESTAMPLTZ);

	}

	@Override
	public void updateNUMBER(int paramInt, NUMBER paramNUMBER)
			throws SQLException {
		oracleRs.updateNUMBER(paramInt, paramNUMBER);

	}

	@Override
	public void updateNUMBER(String paramString, NUMBER paramNUMBER)
			throws SQLException {
		oracleRs.updateNUMBER(paramString, paramNUMBER);

	}

	@Override
	public void updateOracleObject(int paramInt, Datum paramDatum)
			throws SQLException {
		oracleRs.updateOracleObject(paramInt, paramDatum);

	}

	@Override
	public void updateOracleObject(String paramString, Datum paramDatum)
			throws SQLException {
		oracleRs.updateOracleObject(paramString, paramDatum);

	}

	@Override
	public void updateRAW(int paramInt, RAW paramRAW) throws SQLException {
		oracleRs.updateRAW(paramInt, paramRAW);

	}

	@Override
	public void updateRAW(String paramString, RAW paramRAW) throws SQLException {
		oracleRs.updateRAW(paramString, paramRAW);

	}

	@Override
	public void updateREF(int paramInt, REF paramREF) throws SQLException {
		oracleRs.updateREF(paramInt, paramREF);

	}

	@Override
	public void updateREF(String paramString, REF paramREF) throws SQLException {
		oracleRs.updateREF(paramString, paramREF);

	}

	@Override
	public void updateROWID(int paramInt, ROWID paramROWID) throws SQLException {
		oracleRs.updateROWID(paramInt, paramROWID);

	}

	@Override
	public void updateROWID(String paramString, ROWID paramROWID)
			throws SQLException {
		oracleRs.updateROWID(paramString, paramROWID);

	}

	@Override
	public void updateSTRUCT(int paramInt, STRUCT paramSTRUCT)
			throws SQLException {
		oracleRs.updateSTRUCT(paramInt, paramSTRUCT);

	}

	@Override
	public void updateSTRUCT(String paramString, STRUCT paramSTRUCT)
			throws SQLException {
		oracleRs.updateSTRUCT(paramString, paramSTRUCT);

	}

}
