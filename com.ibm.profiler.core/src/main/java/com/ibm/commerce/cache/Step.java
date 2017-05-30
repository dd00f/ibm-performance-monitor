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


/**
 * A step is used to represent a numerical range between a start and a stop
 * number.
 */
public class Step {

	/**
	 * start number value
	 */
	private long start = 0;

	/**
	 * stop number value
	 */
	private long stop = Long.MAX_VALUE;

	/**
	 * gets the start value.
	 * 
	 * @return the start value.
	 */
	public long getStart() {
		return start;
	}

	/**
	 * get the stop value.
	 * 
	 * @return the stop value.
	 */
	public long getStop() {
		return stop;
	}

	/**
	 * Set the start value.
	 * 
	 * @param start
	 *            the start value.
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * Set the stop value.
	 * 
	 * @param stop
	 *            the stop value.
	 */
	public void setStop(long stop) {
		this.stop = stop;
	}

	/**
	 * toString method override.
	 * 
	 * @return the string representation of this step.
	 */
	@Override
    public String toString() {
		return start + "-" + stop;
	}
}