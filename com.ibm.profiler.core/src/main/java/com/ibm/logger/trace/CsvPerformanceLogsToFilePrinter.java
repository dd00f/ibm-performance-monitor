/* 
 * Copyright 2014 IBM. All rights reserved.
 */
package com.ibm.logger.trace;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.commerce.cache.LoggingHelper;
import com.ibm.logger.PerformanceLogger;

/**
 * This class is used to periodically print the csv performance logs to a file
 */
public class CsvPerformanceLogsToFilePrinter implements Runnable {

	public static final String DATE_ISO_8601_TOKEN = "%DATE_ISO_8601%";

	public static final String FILE_HISTORY_COUNT_PROPERTY_NAME = "com.ibm.logger.trace.CsvPerformanceLogsToFilePrinter.fileHistoryCount";

	public static final String CSV_FILE_NAME_PATTERN_PROPERTY_NAME = "com.ibm.logger.trace.CsvPerformanceLogsToFilePrinter.csvFileNamePattern";

	public static Logger LOGGER = Logger
			.getLogger(CsvPerformanceLogsToFilePrinter.class.getName());

	private static final DateFormat DATE_FORMAT;

	static {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss'Z'");
		DATE_FORMAT.setTimeZone(tz);
	}

	private String csvFileNamePattern = "./performance-metrics-"
			+ DATE_ISO_8601_TOKEN + ".csv";

	private int fileHistoryCount = 3;

	private List<String> printedFileNameList = new LinkedList<String>();

	private boolean lastDeleteSuccessful = true;

	public CsvPerformanceLogsToFilePrinter() {
		super();
		initializePropertiesFromSystem();
	}

	private void initializePropertiesFromSystem() {
		csvFileNamePattern = PerformanceLogger.parseStringProperty(
				CSV_FILE_NAME_PATTERN_PROPERTY_NAME, csvFileNamePattern);
		fileHistoryCount = PerformanceLogger.parseIntegerProperty(
				FILE_HISTORY_COUNT_PROPERTY_NAME, fileHistoryCount);
	}

	public String getCsvFileNamePattern() {
		return csvFileNamePattern;
	}

	public int getFileHistoryCount() {
		return fileHistoryCount;
	}

	public void setCsvFileNamePattern(String csvFileNamePattern) {
		this.csvFileNamePattern = csvFileNamePattern;
	}

	public void setFileHistoryCount(int fileHistoryCount) {
		this.fileHistoryCount = fileHistoryCount;
	}

	@Override
	public synchronized void run() {
		dumpPerformanceLogsCsvToFile();

		deleteOldCsvFile();
	}

	private void deleteOldCsvFile() {
		try {
			while (printedFileNameList.size() > fileHistoryCount) {
				String oldestFile = printedFileNameList.remove(0);
				deleteFile(oldestFile);
			}
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER,
					CsvPerformanceLogsToFilePrinter.class.getName(),
					"deleteOldCsvFile", e);
		}
	}

	private void deleteFile(String fileToDelete) {
		try {
			// Java NIO requires Java 7+. 
			//Path deletePath = FileSystems.getDefault().getPath(fileToDelete);
			//Files.deleteIfExists(deletePath);
			
			File oldFile = new File(fileToDelete);
			if (oldFile.exists()) {
				boolean deleteSuccess = oldFile.delete();
				if (!deleteSuccess && lastDeleteSuccessful != deleteSuccess) {
					LOGGER.log(Level.WARNING,
							"Failed to delete old performance metric CSV file : "
									+ fileToDelete);
				}
				lastDeleteSuccessful = deleteSuccess;
			}
		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER,
					CsvPerformanceLogsToFilePrinter.class.getName(),
					"deleteFile", e);
		}
	}

	/**
	 * Dump the performance logs csv to the specified logger.
	 * 
	 * @param logger
	 *            The logger to use.
	 * @param level
	 *            The level to use.
	 */
	private void dumpPerformanceLogsCsvToFile() {
		Writer createReportWriter = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			String fileName = getCsvFileName();
			printedFileNameList.add(fileName);

			fos = new FileOutputStream(fileName);
			bos = new BufferedOutputStream(fos);
			createReportWriter = new OutputStreamWriter(bos, "UTF-8");

			String performanceLogsTable = PerformanceLogger
					.dumpPerformanceLogsCsvToString();

			createReportWriter.append(performanceLogsTable);


		} catch (Exception e) {
			LoggingHelper.logUnexpectedException(LOGGER,
					CsvPerformanceLogsToFilePrinter.class.getName(),
					"dumpPerformanceLogsCsvToFile", e);
		} finally {
			CacheUtilities.closeQuietly(createReportWriter);
			CacheUtilities.closeQuietly(fos);
			CacheUtilities.closeQuietly(bos);
		}
	}

	private String getCsvFileName() {

		Date currentDate = new Date();
		String nowAsISO = DATE_FORMAT.format(currentDate);

		String fileName = StringUtils.replace(csvFileNamePattern,
				DATE_ISO_8601_TOKEN, nowAsISO);

		return fileName;
	}

}
