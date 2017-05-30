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
package com.ibm.logger.trace;

/**
 * Mode that can be used to print a parameter.
 */
public enum PrintMode {

    /**
     * Full parameter value printing using toString.
     */
    FULL,

    /**
     * The parameter shouldn't be displayed at all.
     */
    NO_DISPLAY,

    /**
     * The parameter value should be masked. Used to hide secure values.
     */
    MASK,

    /**
     * Display the hash value of the parameter. Used to avoid printing large strings.
     */
    HASH

}
