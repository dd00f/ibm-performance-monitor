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
package com.ibm.profiler.client.jmx;


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
