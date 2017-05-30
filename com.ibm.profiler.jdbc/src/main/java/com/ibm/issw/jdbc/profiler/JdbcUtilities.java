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
package com.ibm.issw.jdbc.profiler;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.ibm.commerce.cache.LoggingHelper;

/**
 * JDBC profiling utility methods.
 */
public class JdbcUtilities {

	private static final String CLASSNAME = JdbcUtilities.class.getName();

	private static final Logger LOGGER = Logger.getLogger(CLASSNAME);

	/**
	 * Utility method to remove all the registered JDBC drivers from the driver
	 * manager. Used to ensure that the instrumented version of the drivers
	 * takes precedence over the standard ones.
	 */
	public void clearRegisteredJdbcDrivers() {
		final String methodName = "clearRegisteredJdbcDrivers()";

		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver nextElement = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(nextElement);
			} catch (SQLException e) {
				LoggingHelper.logUnexpectedException(LOGGER, CLASSNAME,
						methodName, e);
			}
		}
	}

}
