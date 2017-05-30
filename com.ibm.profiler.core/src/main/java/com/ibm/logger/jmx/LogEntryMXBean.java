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
package com.ibm.logger.jmx;

public interface LogEntryMXBean {

    public void clear();

    public double getAverage();

    public String getId();

    public float getMaximum();

    public float getMinimum();

    public String getName();

    public long getNumCalls();

    public long getFailedCalls();

    public float getTotal();
    
    public String getType();


}
