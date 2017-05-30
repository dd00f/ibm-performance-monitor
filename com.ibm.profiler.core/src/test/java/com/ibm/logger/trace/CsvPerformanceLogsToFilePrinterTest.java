/*
 * Copyright 2017 Steve McDuff
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.logger.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.commerce.cache.CacheUtilities;
import com.ibm.logger.PerformanceLogger;

public class CsvPerformanceLogsToFilePrinterTest {

	public static final String CSV_PRINTER_TEST_OUTPUT_FOLDER = "./csv-printer-test-output";
	private static final String CSV_FILE_NAME_PATTERN = CSV_PRINTER_TEST_OUTPUT_FOLDER
			+ "/output-"
			+ CsvPerformanceLogsToFilePrinter.DATE_ISO_8601_TOKEN
			+ ".csv";
	public File directory;

	@Before
	public void init() {
		directory = new File(CSV_PRINTER_TEST_OUTPUT_FOLDER);

		directory.mkdirs();

		File[] listFiles = directory.listFiles();
		for (File file : listFiles) {
			file.delete();
		}
		
		PerformanceLogger.setEnabled(true);
		PerformanceLogger.clear();
	}

	@After
	public void cleanup() {

		File[] listFiles = directory.listFiles();
		for (File file : listFiles) {
			file.delete();
		}
		
		directory.delete();

	}

	@Test
	public void testCsvPrinting() throws Exception {

		PerformanceLogger.setEnabled(true);
		PerformanceLogger.addStatistic("testActive", 123000000);
		PerformanceLogger.addStatistic("testInactive", 456000000);

		CsvPerformanceLogsToFilePrinter printer = new CsvPerformanceLogsToFilePrinter();
		printer.setCsvFileNamePattern(CSV_FILE_NAME_PATTERN);
		printer.setFileHistoryCount(3);

		printer.run();

		File[] listFiles = directory.listFiles();

		assertEquals(1, listFiles.length);

		File firstFile = listFiles[0];
		FileInputStream fis = new FileInputStream(firstFile);
		InputStreamReader isr = new InputStreamReader(
				fis, "UTF-8");
		BufferedReader in = new BufferedReader(isr);

		String str;

		str = in.readLine();
		assertEquals("Name,Number of calls,Average Duration milliseconds,Minimum Duration milliseconds,Maximum Duration milliseconds,Total Duration milliseconds,Average Size,Maximum Size,Total Size,Cache enabled count,Cache hit count, Error count", str);
		
		String strActive = in.readLine();
		String strInactive = in.readLine();
		
		if(strActive.startsWith("testInactive")) {
		    str = strInactive;
		    strInactive = strActive;
		    strActive = str;
		}
		
		assertEquals("testActive,1,123.000,123,123,123.000,0.000,0,0.000,0,0,0", strActive);
		assertEquals("testInactive,1,456.000,456,456,456.000,0.000,0,0.000,0,0,0", strInactive);
		str = in.readLine();
		assertEquals(null, str);
		
		CacheUtilities.closeQuietly(in);
		CacheUtilities.closeQuietly(isr);
		CacheUtilities.closeQuietly(fis);
				
		
		Thread.sleep(2000);
		printer.run();
		listFiles = directory.listFiles();
		assertEquals(2, listFiles.length);

		Thread.sleep(2000);
		printer.run();
		listFiles = directory.listFiles();
		assertEquals(3, listFiles.length);

		Thread.sleep(2000);
		printer.run();
		listFiles = directory.listFiles();
		assertEquals(3, listFiles.length);
		
		assertFalse( firstFile.exists());
		

		for (File file : listFiles) {
			assertTrue(file.delete());
		}
	}
	
	@Test
	public void testCsvPrintingDeleteOldFiles() throws Exception {
		

		PerformanceLogger.setEnabled(true);
		PerformanceLogger.addStatistic("testActive", 123000000);
		PerformanceLogger.addStatistic("testInactive", 456000000);
		
		CsvPerformanceLogsToFilePrinter printer = new CsvPerformanceLogsToFilePrinter();
		printer.setCsvFileNamePattern(CSV_FILE_NAME_PATTERN);
		printer.setFileHistoryCount(3);
		printer.run();
		
		File[] listFiles = directory.listFiles();
		assertEquals(1, listFiles.length);
		File oldFile = listFiles[0];
		
		Pattern fileNamePatternMatcher = printer.getFileNamePatternMatcher();
		FilenameFilter createFileNamePatternMatcher = printer.createFileNamePatternMatcher(fileNamePatternMatcher);
		
		assertTrue( createFileNamePatternMatcher.accept(directory, oldFile.getName()));
		
		File dummyFile = new File(directory, "dummy.txt");
		assertEquals( true, dummyFile.createNewFile() );
		
		assertEquals( true, dummyFile.exists() );
		
		// ensure the new file has a new name
		Thread.sleep(2000);
		
		// start a second printer, ensure that it cleans up old files.
		printer = new CsvPerformanceLogsToFilePrinter();
		printer.setCsvFileNamePattern(CSV_FILE_NAME_PATTERN);
		printer.setFileHistoryCount(3);
		printer.run();
		
		listFiles = directory.listFiles();
		assertEquals(2, listFiles.length);
		assertEquals( true, dummyFile.exists() );
		assertEquals( false, oldFile.exists() );
		
		dummyFile.delete();
		
	}

}
