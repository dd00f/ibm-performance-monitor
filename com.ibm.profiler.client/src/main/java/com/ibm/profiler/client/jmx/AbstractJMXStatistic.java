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



import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;

/**
 * @author Bryan Johnson
 * 
 */
public abstract class AbstractJMXStatistic implements JMXStatistic {
    private JMXStatisticKey key = null;

    public abstract void _parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one );

    @Override
    public int compareTo( Object arg0 ) {
        if ( arg0 == null )
            return -1;
        try {
            if ( ( (AbstractJMXStatistic) arg0 ).key != null )
                if ( ( (AbstractJMXStatistic) arg0 ).key.compareTo( key ) != 0 ) {
                    return ( (AbstractJMXStatistic) arg0 ).key.compareTo( key );
                }

            return ( (AbstractJMXStatistic) arg0 )._compareTo( this );

        } catch ( Exception e ) {
            return 0;
        }
    }


    @Override
    public JMXStatisticKey getKey() {
        return key;
    }

    protected abstract int _compareTo( AbstractJMXStatistic abstractMEGJMXStatistics );

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AbstractJMXStatistic other = (AbstractJMXStatistic) obj;
        if ( key == null ) {
            if ( other.key != null )
                return false;
        } else if ( !key.equals( other.key ) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
        return result;
    }

    @Override
    public final JMXStatistic parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one ) {

        if ( one == null ) {
            return this;
        }
        key = new JMXStatisticKey( one.getObjectName().getDomain(), one.getClassName() );
        _parse( conn, mbean, one );
        return this;
    }

    @Override
    public String toString() {
        return "AbstractJMXStatistics [key=" + key + "]";
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

}
