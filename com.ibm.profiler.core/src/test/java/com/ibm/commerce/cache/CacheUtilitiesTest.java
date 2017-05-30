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

import static org.junit.Assert.*;

import org.junit.Test;

public class CacheUtilitiesTest {

	@Test
	public void testGetDurationShortText() {
		assertEquals("1ns", CacheUtilities.getDurationShortText(1));
		assertEquals("1" + CacheUtilities.MICROSECOND_SUFFIX,
				CacheUtilities.getDurationShortText(1000));
		assertEquals(
				"1ms",
				CacheUtilities
						.getDurationShortText(CacheUtilities.MILLISECOND_IN_NANOSECONDS));
		assertEquals(
				"1s",
				CacheUtilities
						.getDurationShortText(CacheUtilities.SECOND_IN_NANOSECONDS));
		assertEquals(
				"1m",
				CacheUtilities
						.getDurationShortText(CacheUtilities.MINUTE_IN_NANOSECONDS));
		assertEquals(
				"1h",
				CacheUtilities
						.getDurationShortText(CacheUtilities.HOUR_IN_NANOSECONDS));
		assertEquals(
				"1d",
				CacheUtilities
						.getDurationShortText(CacheUtilities.DAY_IN_NANOSECONDS));

		assertEquals(
				"-1m",
				CacheUtilities
						.getDurationShortText(-CacheUtilities.MINUTE_IN_NANOSECONDS));
		
		long twoOfEachDuration = 2002002002l + 2
				* CacheUtilities.MINUTE_IN_NANOSECONDS + 2
				* CacheUtilities.HOUR_IN_NANOSECONDS + 2
				* CacheUtilities.DAY_IN_NANOSECONDS;
		assertEquals(
				"2d2h2m2s2ms2" + CacheUtilities.MICROSECOND_SUFFIX + "2ns",
				CacheUtilities.getDurationShortText(twoOfEachDuration));
	}

	@Test
	public void testGetNanoDurationFromShortText() {
		assertEquals(1, CacheUtilities.getNanoDurationFromShortText("1ns"));
		assertEquals(
				CacheUtilities.MICROSECOND_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1"
						+ CacheUtilities.MICROSECOND_SUFFIX));
		assertEquals(CacheUtilities.MICROSECOND_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1us"));
		assertEquals(CacheUtilities.MILLISECOND_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1ms"));
		assertEquals(CacheUtilities.SECOND_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1s"));
		assertEquals(CacheUtilities.MINUTE_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1m"));
		assertEquals(CacheUtilities.HOUR_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1h"));
		assertEquals(CacheUtilities.DAY_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("1d"));

		assertEquals(-CacheUtilities.DAY_IN_NANOSECONDS,
				CacheUtilities.getNanoDurationFromShortText("-1d"));
		
	}
	
    @Test
    public void testSafeToString()
    {

        Object badOne = new Object()
        {
            @Override
            public String toString()
            {
                throw new UnsupportedOperationException();
            }
        };

        Object[] nullArray = null;
        Object nullObject = null;

        assertEquals("null", CacheUtilities.safeToString(nullObject));
        assertEquals("error", CacheUtilities.safeToString(badOne));
        assertEquals("123", CacheUtilities.safeToString(123));
        assertEquals("123", CacheUtilities.safeToString("123"));
        assertArrayEquals(new String[]
        { "123" }, CacheUtilities.safeToString(new Object[]
        { "123" }));
        assertArrayEquals(new String[]
        { "123" }, CacheUtilities.safeToString(new Object[]
        { 123 }));
        assertTrue(CacheUtilities.safeToString(new int[]
        { 123 }).startsWith("["));
        assertArrayEquals(new String[0], CacheUtilities.safeToString(nullArray));

        assertEquals("null", CacheUtilities.deepSafeToString(nullObject));
        assertEquals("error", CacheUtilities.deepSafeToString(badOne));
        assertEquals("123", CacheUtilities.deepSafeToString(123));
        assertEquals("123", CacheUtilities.deepSafeToString("123"));
        assertArrayEquals(new String[]
        { "123" }, CacheUtilities.deepSafeToString(new Object[]
        { "123" }));
        assertArrayEquals(new String[]
        { "123" }, CacheUtilities.deepSafeToString(new Object[]
        { 123 }));
        assertEquals("[123]",CacheUtilities.deepSafeToString(new int[]
        { 123 }));

        assertEquals("[123]",CacheUtilities.deepSafeToString(new byte[]
        { 123 }));
        assertEquals("[{]",CacheUtilities.deepSafeToString(new char[]
        { 123 }));
        assertEquals("[123]",CacheUtilities.deepSafeToString(new short[]
        { 123 }));
        assertEquals("[123]",CacheUtilities.deepSafeToString(new int[]
        { 123 }));
        assertEquals("[123]",CacheUtilities.deepSafeToString(new long[]
        { 123 }));
        assertEquals("[false]",CacheUtilities.deepSafeToString(new boolean[]
        { false }));
        assertEquals("[123.0]",CacheUtilities.deepSafeToString(new float[]
        { 123 }));
        assertEquals("[123.0]",CacheUtilities.deepSafeToString(new double[]
        { 123 }));

        
        
        assertArrayEquals(new String[0], CacheUtilities.deepSafeToString(nullArray));
        
        assertArrayEquals(new String[]{"error","null","null","[error, hello, [123, 456]]"},CacheUtilities.deepSafeToString(new Object[]
        { badOne, nullArray, nullObject, new Object[]{ badOne, "hello", new int[]{123,456}} }));

    }

}
