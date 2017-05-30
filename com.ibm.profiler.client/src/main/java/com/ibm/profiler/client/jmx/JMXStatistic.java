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
public interface JMXStatistic extends Comparable<Object> {

    /**
     * 
     * @param conn connection
     * @param mbean bean info
     * @param one the object instance
     * @return the JMX statistics
     */
    public JMXStatistic parse( MBeanServerConnection conn, MBeanInfo mbean, ObjectInstance one );

    /**
     * @return are the statistics empty
     */
    public boolean isEmpty();

    /**
     * @return get the statistics key
     */
    public JMXStatisticKey getKey();

    /**
     * write a header
     */
    public void doHeader();

    /**
     * write details.
     */
    public void doDetail();

}
