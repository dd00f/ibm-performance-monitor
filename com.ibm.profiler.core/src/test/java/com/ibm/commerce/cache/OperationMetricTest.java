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
package com.ibm.commerce.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class OperationMetricTest {

	public OperationMetric metric;

	@Before
	public void setup() {
		metric = new OperationMetric();
	}

	@Test
	public void testCleanProperties() {
		assertNull(metric.getProperty("test"));
		metric.setProperty("test", "value1");
		metric.setProperty("test", "value2");
		metric.setProperty("test", "value3");
		assertEquals("value3", metric.getProperty("test"));
		metric.cleanProperties();
		assertNull(metric.getProperty("test"));
	}

	@Test
	public void testPrintProperty() throws IOException {
		StringWriter stringWriter = new StringWriter();
		metric.printProperty(stringWriter);
		assertEquals("", stringWriter.getBuffer().toString());

		metric.setProperty("test1", "value1");
		metric.setProperty("test2", "value2");

		stringWriter = new StringWriter();
		metric.printProperty(stringWriter);
		assertEquals(" test1=\"value1\" test2=\"value2\"", stringWriter
				.getBuffer().toString());
	}

	@Test
	public void testMatches() {
		OperationMetric metric1 = new OperationMetric();
		OperationMetric metric2 = new OperationMetric();
		OperationMetric metric3 = new OperationMetric();
		OperationMetric metric4 = new OperationMetric();

		metric1.startOperation("test", false, "test");
		metric2.startOperation("test", false, "test");
		metric3.startOperation("test1", false, "test");
		metric4.startOperation("test", false, "test2");

		assertTrue(metric1.matches(metric2));
		assertTrue(metric2.matches(metric1));

		assertFalse(metric1.matches(metric3));
		assertFalse(metric3.matches(metric1));

		assertFalse(metric1.matches(metric4));
		assertFalse(metric4.matches(metric1));
	}

	@Test
	public void testStopOperationIntBooleanBoolean() {
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true, false);

		assertEquals("test", metric.getOperationName());
		assertEquals(2, metric.getKeyValuePairList().size());
		assertEquals("a", metric.getKeyValuePairList().get(0));
		assertEquals("b", metric.getKeyValuePairList().get(1));
		assertEquals(true, metric.isOperationCacheEnabled());
		assertEquals(true, metric.isResultFetchedFromCache());
		assertEquals(false, metric.isSuccessful());
	}

	@Test
	public void testStopOperationIntBoolean() {
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true);

		assertEquals("test", metric.getOperationName());
		assertEquals(2, metric.getKeyValuePairList().size());
		assertEquals("a", metric.getKeyValuePairList().get(0));
		assertEquals("b", metric.getKeyValuePairList().get(1));
		assertEquals(true, metric.isOperationCacheEnabled());
		assertEquals(true, metric.isResultFetchedFromCache());
		assertEquals(true, metric.isSuccessful());
	}

	@Test
	public void testGetUniqueKey() {
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true);

		assertEquals("a:b:", metric.getUniqueKey());
	}

	@Test
	public void testToSerializedString() {
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true);

		String serializedString = metric.toSerializedString();

		String match = "[0-9]+ [0-9]+ test a:b: [0-9]+ [0-9]+ [0-9]+ [0-9]+ [0-9]+ true true true";
		assertEquals(true, serializedString.matches(match));
	}

	@Test
	public void testToSerializedStringWriter() throws IOException {
		StringWriter writer = new StringWriter();
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true);

		metric.toSerializedString(writer);

		String match = "[0-9]+ [0-9]+ test a:b: [0-9]+ [0-9]+ [0-9]+ [0-9]+ [0-9]+ true true true";
		assertEquals(true, writer.getBuffer().toString().matches(match));

		OperationMetric metric2 = new OperationMetric();
		metric2.fromSerializedString(writer.getBuffer().toString());

		assertEquals(metric, metric2);
	}

	@Test
	public void testToSerializedXmlString() throws Exception {
		StringWriter writer = new StringWriter();
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true, false);

		metric.toSerializedXmlString(writer);

		String match = " operation=\"test\" parameters=\"a=b\" id=\"[0-9]+\" parentId=\"[0-9]+\" startTime=\"[0-9]+\" stopTime=\"[0-9]+\" duration=\"[0-9]+\" durationMs=\"[0-9]+\" resultSize=\"123\" cacheHit=\"true\" cacheEnabled=\"true\" successful=\"false\"";
		String print = writer.getBuffer().toString();
		System.out.println(print);
		assertEquals(true, print.matches(match));

		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;

		String xml = "<exit " + print + " />";
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

		dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		Document parse = dBuilder.parse(bais);

		OperationMetric metric2 = new OperationMetric();
		metric2.fromXmlDocument(parse);

		assertEquals(metric, metric2);
	}

	@Test
	public void testToSerializedXmlStringEntryLog() throws IOException {
		StringWriter writer = new StringWriter();
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true, false);

		String match = " operation=\"test\" parameters=\"a=b\" id=\"[0-9]+\" parentId=\"[0-9]+\" startTime=\"[0-9]+\"";
		metric.toSerializedXmlStringEntryLog(writer);
		assertEquals(true, writer.getBuffer().toString().matches(match));
	}

	@Test
	public void testWriteEntryExitLog() {
		metric.startOperation("test", true, "a", "b");
		metric.stopOperation(123, true, false);

		String writeEntryExitLog = OperationMetric.writeEntryExitLog(metric,
				true);

		String match = "PerfLog <entry operation=\"test\" parameters=\"a=b\" id=\"[0-9]+\" parentId=\"[0-9]+\" startTime=\"[0-9]+\" />";
		assertEquals(true, writeEntryExitLog.matches(match));
		writeEntryExitLog = OperationMetric.writeEntryExitLog(metric, false);
		match = "PerfLog <exit operation=\"test\" parameters=\"a=b\" id=\"[0-9]+\" parentId=\"[0-9]+\" startTime=\"[0-9]+\" stopTime=\"[0-9]+\" duration=\"[0-9]+\" durationMs=\"[0-9]+\" resultSize=\"123\" cacheHit=\"true\" cacheEnabled=\"true\" successful=\"false\" />";
		assertEquals(true, writeEntryExitLog.matches(match));
	}

	@Test
	public void testFromSerializedString() {
		OperationMetric metric2 = new OperationMetric();
		metric2.fromSerializedString("6173354814655692812 6173354814655692808 test a:b: "
				+ "1909144488185765 1909144488188529 2764 0 123 true true false");

		assertEquals("test", metric2.getOperationName());
		assertEquals(2, metric2.getKeyValuePairList().size());
		assertEquals("a", metric2.getKeyValuePairList().get(0));
		assertEquals("b", metric2.getKeyValuePairList().get(1));
		assertEquals(6173354814655692812l, metric2.getIdentifier());
		assertEquals(6173354814655692808l, metric2.getParentIdentifier());
		assertEquals(1909144488185765l, metric2.getStartTime());
		assertEquals(1909144488188529l, metric2.getStopTime());
		assertEquals(2764l, metric2.getDuration());
		assertEquals(123, metric2.getResultSize());
		assertEquals(true, metric2.isResultFetchedFromCache());
		assertEquals(true, metric2.isOperationCacheEnabled());
		assertEquals(false, metric2.isSuccessful());
	}

	@Test
	public void testFromSerializedStringNoSuccess() {
		OperationMetric metric2 = new OperationMetric();
		metric2.fromSerializedString("6173354814655692812 6173354814655692808 test a:b: "
				+ "1909144488185765 1909144488188529 2764 0 123 true true");

		assertEquals("test", metric2.getOperationName());
		assertEquals(2, metric2.getKeyValuePairList().size());
		assertEquals("a", metric2.getKeyValuePairList().get(0));
		assertEquals("b", metric2.getKeyValuePairList().get(1));
		assertEquals(6173354814655692812l, metric2.getIdentifier());
		assertEquals(6173354814655692808l, metric2.getParentIdentifier());
		assertEquals(1909144488185765l, metric2.getStartTime());
		assertEquals(1909144488188529l, metric2.getStopTime());
		assertEquals(2764l, metric2.getDuration());
		assertEquals(123, metric2.getResultSize());
		assertEquals(true, metric2.isResultFetchedFromCache());
		assertEquals(true, metric2.isOperationCacheEnabled());
		assertEquals(true, metric2.isSuccessful());
	}

	@Test
	public void testFromXmlDocument() throws Exception {

		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;

		String xml = "<exit operation=\"test\" parameters=\"a=b\" "
				+ "id=\"6173354814655692814\" parentId=\"6173354814655692808\" "
				+ "startTime=\"1909144489660053\" stopTime=\"1909144489663606\" "
				+ "duration=\"3553\" durationMs=\"0\" resultSize=\"123\" "
				+ "cacheHit=\"true\" cacheEnabled=\"true\" successful=\"false\"/>";
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

		dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		Document parse = dBuilder.parse(bais);

		OperationMetric metric2 = new OperationMetric();
		metric2.fromXmlDocument(parse);

		assertEquals("test", metric2.getOperationName());
		assertEquals(2, metric2.getKeyValuePairList().size());
		assertEquals("a", metric2.getKeyValuePairList().get(0));
		assertEquals("b", metric2.getKeyValuePairList().get(1));
		assertEquals(6173354814655692814l, metric2.getIdentifier());
		assertEquals(6173354814655692808l, metric2.getParentIdentifier());
		assertEquals(1909144489660053l, metric2.getStartTime());
		assertEquals(1909144489663606l, metric2.getStopTime());
		assertEquals(3553, metric2.getDuration());
		assertEquals(123, metric2.getResultSize());
		assertEquals(true, metric2.isResultFetchedFromCache());
		assertEquals(true, metric2.isOperationCacheEnabled());
		assertEquals(false, metric2.isSuccessful());
	}

	@Test
	public void testFromXmlDocumentNoSuccess() throws Exception {

		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;

		String xml = "<exit operation=\"test\" parameters=\"a=b\" id=\"6173354814655692814\" "
				+ "parentId=\"6173354814655692808\" startTime=\"1909144489660053\" "
				+ "stopTime=\"1909144489663606\" duration=\"3553\" durationMs=\"0\""
				+ " resultSize=\"123\" cacheHit=\"true\" cacheEnabled=\"true\" />";
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

		dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		Document parse = dBuilder.parse(bais);

		OperationMetric metric2 = new OperationMetric();
		metric2.fromXmlDocument(parse);
		assertTrue(metric2.isSuccessful());

		assertEquals("test", metric2.getOperationName());
		assertEquals(2, metric2.getKeyValuePairList().size());
		assertEquals("a", metric2.getKeyValuePairList().get(0));
		assertEquals("b", metric2.getKeyValuePairList().get(1));
		assertEquals(6173354814655692814l, metric2.getIdentifier());
		assertEquals(6173354814655692808l, metric2.getParentIdentifier());
		assertEquals(1909144489660053l, metric2.getStartTime());
		assertEquals(1909144489663606l, metric2.getStopTime());
		assertEquals(3553, metric2.getDuration());
		assertEquals(123, metric2.getResultSize());
		assertEquals(true, metric2.isResultFetchedFromCache());
		assertEquals(true, metric2.isOperationCacheEnabled());
		assertEquals(true, metric2.isSuccessful());
	}

	@Test
	public void testCustomFieldSanitization() throws Exception {

		OperationMetric metric2 = new OperationMetric();
		
		metric2.setProperty("random", "value");
		metric2.setIdentifier(123);
		
		String print = OperationMetric.writeEntryExitLog(metric2, true);
		assertEquals("PerfLog <entry operation=\"\" parameters=\"\" id=\"123\" parentId=\"0\" startTime=\"0\" random=\"value\" />",print);
		
		metric2.setProperty(OperationMetric.FIELD_CACHE_ENABLED, "value1");
		metric2.setProperty(OperationMetric.FIELD_CACHE_HIT, "value2");
		metric2.setProperty(OperationMetric.FIELD_DURATION, "value3");
		metric2.setProperty(OperationMetric.FIELD_DURATION_MS, "value4");
		metric2.setProperty(OperationMetric.FIELD_ID, "value5");
		metric2.setProperty(OperationMetric.FIELD_OPERATION, "value6");
		metric2.setProperty(OperationMetric.FIELD_PARAMETERS, "value7");
		metric2.setProperty(OperationMetric.FIELD_PARENT_ID, "value8");
		metric2.setProperty(OperationMetric.FIELD_RESULT_SIZE, "value9");
		metric2.setProperty(OperationMetric.FIELD_START_TIME, "valuea");
		metric2.setProperty(OperationMetric.FIELD_STOP_TIME, "valueb");
		metric2.setProperty(OperationMetric.FIELD_SUCCESSFUL, "valuec");

		print = OperationMetric.writeEntryExitLog(metric2, true);
		assertEquals(
				"PerfLog <entry operation=\"\" parameters=\"\" id=\"123\" parentId=\"0\" startTime=\"0\" "
						+ "_cacheEnabled=\"value1\" "
						+ "_cacheHit=\"value2\" "
						+ "_duration=\"value3\" "
						+ "_durationMs=\"value4\" "
						+ "_id=\"value5\" "
						+ "_operation=\"value6\" "
						+ "_parameters=\"value7\" "
						+ "_parentId=\"value8\" "
						+ "_resultSize=\"value9\" "
						+ "_startTime=\"valuea\" "
						+ "_stopTime=\"valueb\" "
						+ "_successful=\"valuec\" "
						+ "random=\"value\" />", print);
		
		
		print = OperationMetric.writeEntryExitLog(metric2, false);
		assertEquals(
				"PerfLog <exit operation=\"\" parameters=\"\" id=\"123\" parentId=\"0\" startTime=\"0\" stopTime=\"0\" duration=\"0\" durationMs=\"0\" resultSize=\"0\" cacheHit=\"false\" cacheEnabled=\"false\" successful=\"true\" />", print);

	}

}
