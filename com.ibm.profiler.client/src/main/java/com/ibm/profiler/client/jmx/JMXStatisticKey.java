package com.ibm.profiler.client.jmx;

//IBM Confidential OCO Source Material
//5725-F81 (C) COPYRIGHT International Business Machines Corp. 2011,2014
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.

/**
 * @author Bryan Johnson
 * 
 */
public class JMXStatisticKey implements Comparable<Object> {
    private final String domain;

    private final String type;

    public JMXStatisticKey( String domain, String type ) {
        super();
        this.domain = domain;
        this.type = type;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        JMXStatisticKey other = (JMXStatisticKey) obj;
        if ( domain == null ) {
            if ( other.domain != null )
                return false;
        } else if ( !domain.equals( other.domain ) )
            return false;
        if ( type == null ) {
            if ( other.type != null )
                return false;
        } else if ( !type.equals( other.type ) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( domain == null ) ? 0 : domain.hashCode() );
        result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
        return result;
    }

    @Override
    public String toString() {
        return "StatisticKey [domain=" + domain + ", type=" + type + "]";
    }

    @Override
    public int compareTo( Object arg0 ) {
        if ( arg0 == null )
            return -1;
        try {
            if ( domain == null )
                return -1;
            if ( ( (JMXStatisticKey) arg0 ).domain != null )
                if ( ( (JMXStatisticKey) arg0 ).domain.compareTo( domain ) != 0 ) {
                    return ( (JMXStatisticKey) arg0 ).domain.compareTo( domain );
                }
            if ( type == null )
                return -1;
            if ( ( (JMXStatisticKey) arg0 ).type != null )
                if ( ( (JMXStatisticKey) arg0 ).type.compareTo( type ) != 0 ) {
                    return ( (JMXStatisticKey) arg0 ).type.compareTo( type );
                }
            return 0;
        } catch ( Exception e ) {
            e.printStackTrace();
            return 0;
        }
    }
}
