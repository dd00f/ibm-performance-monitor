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
package com.ibm.logger;

import java.util.logging.Logger;

import com.ibm.logger.trace.Print;
import com.ibm.logger.trace.PrintMode;

@SuppressWarnings("unused")
public class FakeClassWithAspect {

    public static Logger LOGGER = Logger.getLogger( FakeClassWithAspect.class.getName() );

    public static Logger SERVICE_LOGGER = Logger.getLogger( "com.ibm.service.fake" );

    public FakeClassWithAspect() {

    }

    public FakeClassWithAspect( String firstConstructor ) {

    }

    public FakeClassWithAspect( int secondConstructor ) {

    }

    public String methodWithTrace() {
        return "Hello world1";
    }

    public String methodWithTrace( String throwAnException ) {
        throw new NullPointerException( "my npe" );
    }

    public String methodWithTrace( String arg1, String arg2, int arg3, int[] arg4 ) {
        return "Hello world2";
    }


    @com.ibm.logger.trace.Print( PrintMode.MASK )
    @Dummy
    public String methodWithTrace( @Print( PrintMode.FULL ) String arg1, @Print( PrintMode.HASH ) String arg2, @Print( PrintMode.MASK ) int arg3, @Print( PrintMode.NO_DISPLAY ) int[] arg4, @Dummy String filter ) {
        return "Hello world3";
    }
    
    

    public String methodWithServiceLogger() {
        return "Hello to you too";
    }

    public String[] methodWithServiceLogger( String arg1, String arg2, int arg3, int[] arg4 ) {
        return new String[] { "Hello world", "this is CNM", "Isnt this fun." };
    }
}
