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
package com.ibm.profiler.cassandra;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.cassandra.CassandraLogger;

public class ProfilingUtilitiesTest {

    private Object badToStringObject = new Object() {
        @Override
        public String toString() {
            throw new NullPointerException();
        }
    };

    @Test
    public void testGetIntegerString() {
        for ( int i = 0; i < 101; ++i ) {
            String expected = Integer.toString( i );
            String actual = ProfilingUtilities.getIntegerString( i );
            assertEquals( "Expect converted string to be equal.", expected, actual );
        }
    }

    @Test
    public void testConvertArgumentMapToArray() {

        // test empty map
        Map<String, Object> convert = new HashMap<String, Object>();
        String[] convertArgumentMapToArray = null;
        convertArgumentMapToArray = ProfilingUtilities.convertArgumentMapToArray( convert );
        assertNotNull( "Function should never returns null. ", convertArgumentMapToArray );
        assertEquals( "Expect empty array. ", 0, convertArgumentMapToArray.length );

        // test null input
        convertArgumentMapToArray = ProfilingUtilities.convertArgumentMapToArray( null );
        assertNotNull( "Function should never returns null. ", convertArgumentMapToArray );
        assertEquals( "Expect empty array. ", 0, convertArgumentMapToArray.length );

        // test with values, including null and an object that crashes.
        convert.put( "c", "c" );
        convert.put( "b", 42 );
        convert.put( "a", null );
        convert.put( "d", badToStringObject );

        convertArgumentMapToArray = ProfilingUtilities.convertArgumentMapToArray( convert );

        assertNotNull( "Function should never returns null. ", convertArgumentMapToArray );
        assertEquals( "Expect 8 elements. ", 8, convertArgumentMapToArray.length );
        assertEquals( "Expect 1st elements to be sorted by name. ", "a", convertArgumentMapToArray[0] );
        assertEquals( "Expect 2nd elements. ", "null", convertArgumentMapToArray[1] );
        assertEquals( "Expect 3rd elements. ", "b", convertArgumentMapToArray[2] );
        assertEquals( "Expect 4th elements. ", "42", convertArgumentMapToArray[3] );
        assertEquals( "Expect 5th elements. ", "c", convertArgumentMapToArray[4] );
        assertEquals( "Expect 6th elements. ", "c", convertArgumentMapToArray[5] );
        assertEquals( "Expect 7th elements. ", "d", convertArgumentMapToArray[6] );
        assertEquals( "Expect 8th elements. ", "unknown", convertArgumentMapToArray[7] );
    }

    @Test
    public void testConvertArgumentArrayToNumericArgumentMap() {
        // test empty map
        Object[] convert = new Object[0];
        Map<String, Object> result = new HashMap<String, Object>();
        ProfilingUtilities.convertArgumentArrayToNumericArgumentMap( result, convert );
        assertEquals( "Expect empty map. ", 0, result.size() );

        // test null input
        ProfilingUtilities.convertArgumentArrayToNumericArgumentMap( result, (Object[]) null );
        assertEquals( "Expect empty map. ", 0, result.size() );

        // test with values, including null and an object that crashes.

        convert = new Object[] { "c", 42, null, badToStringObject };

        ProfilingUtilities.convertArgumentArrayToNumericArgumentMap( result, convert );

        assertEquals( "Expect map element count. ", 4, result.size() );
        assertEquals( "Expect 1st elements. ", "c", result.get( "0" ) );
        assertEquals( "Expect 2nd elements. ", 42, result.get( "1" ) );
        assertEquals( "Expect 3rd elements. ", null, result.get( "2" ) );
        Assert.assertSame( "Expect 4th elements. ", badToStringObject, result.get( "3" ) );
    }

    @Test
    public void testConvertArgumentArrayToNumericArgumentArray() {
        // test empty map
        Object[] convert = new Object[0];
        String[] resultArray = null;
        resultArray = ProfilingUtilities.convertArgumentArrayToNumericArgumentArray( convert );
        assertNotNull( "Function should never returns null. ", resultArray );
        assertEquals( "Expect empty array. ", 0, resultArray.length );

        // test null input
        resultArray = ProfilingUtilities.convertArgumentArrayToNumericArgumentArray( (Object[]) null );
        assertNotNull( "Function should never returns null. ", resultArray );
        assertEquals( "Expect empty array. ", 0, resultArray.length );

        // test with values, including null and an object that crashes.

        convert = new Object[] { "c", 42, null, badToStringObject };

        resultArray = ProfilingUtilities.convertArgumentArrayToNumericArgumentArray( convert );

        assertNotNull( "Function should never returns null. ", resultArray );
        assertEquals( "Expect 8 elements. ", 8, resultArray.length );
        assertEquals( "Expect 1st elements to be sorted by name. ", "0", resultArray[0] );
        assertEquals( "Expect 2nd elements. ", "c", resultArray[1] );
        assertEquals( "Expect 3rd elements. ", "1", resultArray[2] );
        assertEquals( "Expect 4th elements. ", "42", resultArray[3] );
        assertEquals( "Expect 5th elements. ", "2", resultArray[4] );
        assertEquals( "Expect 6th elements. ", "null", resultArray[5] );
        assertEquals( "Expect 7th elements. ", "3", resultArray[6] );
        assertEquals( "Expect 8th elements. ", "unknown", resultArray[7] );
    }

    @Test
    public void testInitializeMetric() {

        @SuppressWarnings("deprecation")
        Logger logger = CassandraLogger.METRIC_GATHERER.getLogger();
        logger.addHandler( new ConsoleHandler() );

        logger.setLevel( Level.FINE );

        OperationMetric initializeMetric = ProfilingUtilities.initializeMetric( "my statement", "arg1", "arg22" );

        assertNotNull( initializeMetric );

        ProfilingUtilities.logMetric( initializeMetric, null );
    }

}
