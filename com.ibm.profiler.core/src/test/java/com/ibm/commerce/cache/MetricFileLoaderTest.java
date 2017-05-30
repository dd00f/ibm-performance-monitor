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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


/**
 * MetricFileLoaderTest
 * 
 * @author Administrator
 */
public class MetricFileLoaderTest
{
    public MetricFileLoaderTest()
    {
        super();
    }


    @Test
    public void testSubstituteCassandraParameters()
    {
        assertEquals("select * from test where abc in (*?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in ('a')"));
        assertEquals("select * from test where abc in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in ('a','b','c')"));
        assertEquals("select * from test where abc in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in (1,2,3,'a''''''b')"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in (?,?,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in ( ?, ?,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in (? ,? ,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in (  ?,  ?,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteCassandraParameters("select * from test where abc in (  ?  ,  ?  ,1,2,3,'a''''''b') and in (1,2,3)"));

        
        StringBuilder testStr = new StringBuilder();
        testStr.append("select * from test where abc in ('a'");
        for( int i=0;i<10000;++i) {
        	testStr.append(",'a'");
        }
        testStr.append(")");

        assertEquals("select * from test where abc in (**?)", MetricFileLoader.substituteCassandraParameters(testStr.toString()));

        assertEquals("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=*? ORDER BY uc_name ASC LIMIT *?;", MetricFileLoader.substituteCassandraParameters("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8ddd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT 100;"));
        assertEquals("Batch : UPDATE replication.dc_d8cfbbb4_cc6f_4151_8271_8fbb3e59c22f SET cc_node_id=? WHERE payload_id=? AND segment_id=?;", MetricFileLoader.substituteCassandraParameters("Batch : UPDATE replication.dc_d8cfbbb4_cc6f_4151_8271_8fbb3e59c22f SET cc_node_id=? WHERE payload_id=? AND segment_id=?;"));

    }
    
    @Test
    public void testAddJdbcParameterSubstitution()
    {
        testJdbcAddParameterSubstitution("select * from test where abc in ('a')", "*1","'a'");
        testJdbcAddParameterSubstitution("select * from test where abc in ('a','b','c')", "*1","'a'","*2","'b'","*3","'c'");
        testJdbcAddParameterSubstitution("select * from test where abc in ('a''b')", "*1","'a''b'");
        testJdbcAddParameterSubstitution("select * from test where abc in ('a''b', 'a''''''b', 'a''c')", "*1","'a''b'", "*2","'a''''''b'", "*3","'a''c'");
        testJdbcAddParameterSubstitution("select * from test where abc in ('a  b')", "*1","'a  b'");
        testJdbcAddParameterSubstitution("select * from test where abc in (1)", "*1","1");
        testJdbcAddParameterSubstitution("select * from test where abc in (1,2,3)", "*1","1","*2","2","*3","3");
        testJdbcAddParameterSubstitution("select * from test where abc in (1,2,3,'a''''''b')", "*1","1","*2","2","*3","3", "*4","'a''''''b'");
        testJdbcAddParameterSubstitution("select * from test where abc in (-1)", "*1","-1");
        testJdbcAddParameterSubstitution("select * from test where abc in (-1.2)", "*1","-1.2");
        testJdbcAddParameterSubstitution("select * from test where abc in (-1.212341234)", "*1","-1.212341234");
        testJdbcAddParameterSubstitution("select * from test where abc in (-123412341324.12432341324)","*1", "-123412341324.12432341324");
    }
    
    @Test
    public void testAddCassandraParameterSubstitution()
    {
        testCassandraAddParameterSubstitution("select * from test where abc in ('a')", "*1","'a'");
        testCassandraAddParameterSubstitution("select * from test where abc in ('a','b','c')", "*1","'a'","*2","'b'","*3","'c'");
        testCassandraAddParameterSubstitution("select * from test where abc in ('a''b')", "*1","'a''b'");
        testCassandraAddParameterSubstitution("select * from test where abc in ('a''b', 'a''''''b', 'a''c')", "*1","'a''b'", "*2","'a''''''b'", "*3","'a''c'");
        testCassandraAddParameterSubstitution("select * from test where abc in ('a  b')", "*1","'a  b'");
        testCassandraAddParameterSubstitution("select * from test where abc in (1)", "*1","1");
        testCassandraAddParameterSubstitution("select * from test where abc in (1,2,3)", "*1","1","*2","2","*3","3");
        testCassandraAddParameterSubstitution("select * from test where abc in (1,2,3,'a''''''b')", "*1","1","*2","2","*3","3", "*4","'a''''''b'");
        testCassandraAddParameterSubstitution("select * from test where abc in (-1)", "*1","-1");
        testCassandraAddParameterSubstitution("select * from test where abc in (-1.2)", "*1","-1.2");
        testCassandraAddParameterSubstitution("select * from test where abc in (-1.212341234)", "*1","-1.212341234");
        testCassandraAddParameterSubstitution("select * from test where abc in (-123412341324.12432341324)","*1", "-123412341324.12432341324");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8ddd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT 100;","*1", "c8ddd189-5a10-411c-87fb-28cefeda6b38","*2", "100");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8dDd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT 100;","*1", "c8dDd189-5a10-411c-87fb-28cefeda6b38","*2", "100");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8dDd189-5a10-411c-87fb-28cefeda6b38;","*1", "c8dDd189-5a10-411c-87fb-28cefeda6b38");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8dDd189-5a10-411c-87fb-28cefeda6b38\n;","*1", "c8dDd189-5a10-411c-87fb-28cefeda6b38");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8dDd189-5a10-411c-87fb-28cefeda6b38\r;","*1", "c8dDd189-5a10-411c-87fb-28cefeda6b38");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8dDd189-5a10-411c-87fb-28cefeda6b38","*1", "c8dDd189-5a10-411c-87fb-28cefeda6b38");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8dGd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT;");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=ac8ddd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT;");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=8ddd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT;");
        testCassandraAddParameterSubstitution("SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8ddd189-5a10-411c-87fb-28cefeda6b38a ORDER BY uc_name ASC LIMIT;");
        testCassandraAddParameterSubstitution("Batch : UPDATE replication.dc_d8cfbbb4_cc6f_4151_8271_8fbb3e59c22f SET cc_node_id=? WHERE payload_id=? AND segment_id=?;");
        
        
        // Cassandra : SELECT * FROM mailbox.messages_by_name WHERE mbx_id=c8ddd189-5a10-411c-87fb-28cefeda6b38 ORDER BY uc_name ASC LIMIT 100;

        // Cassandra : Batch : UPDATE replication.dc_d8cfbbb4_cc6f_4151_8271_8fbb3e59c22f SET cc_node_id=? WHERE payload_id=? AND segment_id=?;

        
    }

    private void testJdbcAddParameterSubstitution(String operationName, String... expectedArray)
    {
        ArrayList<String> parameterList = new ArrayList<String>();
        MetricFileLoader.addJdbcParameterSubstitution(operationName, parameterList);
        List<String> expected = Arrays.asList(expectedArray);
        assertEquals(expected, parameterList);
    }
    
    private void testCassandraAddParameterSubstitution(String operationName, String... expectedArray)
    {
        ArrayList<String> parameterList = new ArrayList<String>();
        MetricFileLoader.addCassandraParameterSubstitution(operationName, parameterList);
        List<String> expected = Arrays.asList(expectedArray);
        assertEquals(expected, parameterList);
    }
    
    @Test
    public void testSubstituteJdbcParameters()
    {
        assertEquals("select * from test where abc in (*?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in ('a')"));
        assertEquals("select * from test where abc in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in ('a','b','c')"));
        assertEquals("select * from test where abc in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in (1,2,3,'a''''''b')"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in (?,?,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in ( ?, ?,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in (? ,? ,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in (  ?,  ?,1,2,3,'a''''''b') and in (1,2,3)"));
        assertEquals("select * from test where abc in (**?) and in (**?)", MetricFileLoader.substituteJdbcParameters("select * from test where abc in (  ?  ,  ?  ,1,2,3,'a''''''b') and in (1,2,3)"));

        
        StringBuilder testStr = new StringBuilder();
        testStr.append("select * from test where abc in ('a'");
        for( int i=0;i<10000;++i) {
        	testStr.append(",'a'");
        }
        testStr.append(")");
        
        assertEquals("select * from test where abc in (**?)", MetricFileLoader.substituteJdbcParameters(testStr.toString()));

        
    }
}
