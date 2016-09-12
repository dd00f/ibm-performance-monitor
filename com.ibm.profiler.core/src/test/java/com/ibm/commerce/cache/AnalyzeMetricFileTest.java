package com.ibm.commerce.cache;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class AnalyzeMetricFileTest {

    @Test
    public void getTimeIncrementFromDivision() {
        assertEquals( "3 nanosecond increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 3 ) );
        assertEquals( "3 microsecond increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 3000 ) );
        assertEquals( "3 millisecond increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 3000000 ) );
        assertEquals( "3 second increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 3000000000l ) );
        assertEquals( "3 second increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 3000000000l ) );
        assertEquals( "3 minute increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 180000000000l ) );
        assertEquals( "3 hour increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 10800000000000l ) );
        assertEquals( "3 day increment", AnalyzeMetricFile.getTimeIncrementFromDivision( 259200000000000l ) );
    }

    
    @Test
    public void formatCsvToHtmlString() {
    	assertEquals( "this is, my, \\\"content\\\" with quotes.\\n\"+\n\"this is the other line", AnalyzeMetricFile.formatCsvToHtmlString("this is, my, \"content\" with quotes.\n\rthis is the other line"));
    }
    
    
    @Test
    public void testBigDecimalPrinting() {
       BigDecimal bd = new BigDecimal(1.41623852E13);
       System.out.println(bd);
       
       Double bi = new Double("1.41623852E13");
       System.out.println(bi);
       System.out.printf("dexp: %f\n", bi);
       
       Float fl = new Float("1.41623852E13");
       System.out.println(fl);
       System.out.printf("dexp: %f\n", fl);
    }
    
}