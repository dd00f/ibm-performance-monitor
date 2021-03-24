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
package com.ibm.profiler.mongo;

import com.ibm.commerce.cache.CommonMetricProperties;
import com.ibm.commerce.cache.OperationMetric;
import com.ibm.service.detailed.MongoLogger;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.MongoCursor;

/**
 * ProfiledMongoCursor
 * 
 * @author Steve McDuff
 */
public class ProfiledMongoCursor<TResult> implements MongoCursor<TResult> {

	private MongoCursor<TResult> profiledCursor;
	private final ProfiledCursorCreator creator;

	private boolean started = true;
	private boolean measureSize = MongoLogger.isResultSetSizeMeasured();
	private int readCount;
	private long readTime;
	private long maxReadTime;
	private long firstReadDuration = -1;
	private int readSize;
	private int maxReadSize;

	private OperationMetric metric = new OperationMetric();
	private long cursorCreationDuration;

	public ProfiledMongoCursor(ProfiledCursorCreator find) {
		super();

		this.creator = find;
		metric = find.startMetric("iterate", null);
	}

	protected void start(MongoCursor<TResult> cursor) {
		this.profiledCursor = cursor;
		if (metric != null) {
			cursorCreationDuration = System.nanoTime() - metric.getStartTime();
		}

	}

	@Override
	public void close() {
		profiledCursor.close();
		closeMeasurements();
	}

	@Override
	public boolean hasNext() {
		long startTime = System.nanoTime();
		boolean hasNext = profiledCursor.hasNext();
		readTime += System.nanoTime() - startTime;
		if (!hasNext) {
			closeMeasurements();
		}
		return hasNext;
	}

	@Override
	public TResult next() {
		TResult next = null;
		long startTime = System.nanoTime();
		try {
			next = profiledCursor.next();
		} finally {
			long duration = System.nanoTime() - startTime;
			addRead(duration, next);
		}
		return next;
	}

	private void addRead(long duration, TResult next) {
		if (metric == null) {
			return;
		}

		if (firstReadDuration == -1) {
			firstReadDuration = System.nanoTime() - metric.getStartTime();
		}

		readTime += duration;
		if (maxReadTime < duration) {
			maxReadTime = duration;
		}

		if (next == null) {
			closeMeasurements();
		} else {
			readCount += 1;
		}

		if (measureSize) {
			int length = next.toString().length();
			readSize += length;
			if (maxReadSize < length) {
				maxReadSize = length;
			}
		}
	}

	private void closeMeasurements() {
		if (!started) {
			return;
		}
		started = false;

		if (metric != null) {
			metric.setProperty(CommonMetricProperties.RESPONSE_OBJECT_COUNT, Integer.toString(readCount));
			metric.setProperty(CommonMetricProperties.READ_TIME_NS, Long.toString(readTime));
			metric.setProperty(CommonMetricProperties.MAXIMUM_READ_DURATION_NS, Long.toString(maxReadTime));
			metric.setProperty(CommonMetricProperties.FIRST_READ_DURATION_NS, Long.toString(firstReadDuration));
			metric.setProperty(CommonMetricProperties.MAXIMUM_RESPONSE_OBJECT_SIZE_BYTES,
					Integer.toString(maxReadSize));
			metric.setProperty(MongoProperties.MONGO_CURSOR_CREATION_DURATION_NS,
					Long.toString(cursorCreationDuration));

			creator.stopMetric(metric, this.readSize);
		}
	}

	@Override
	public TResult tryNext() {
		TResult next = null;
		long startTime = System.nanoTime();
		try {
			next = profiledCursor.tryNext();
		} finally {
			long duration = System.nanoTime() - startTime;
			addRead(duration, next);
		}
		return next;
	}

	@Override
	public ServerCursor getServerCursor() {
		return profiledCursor.getServerCursor();
	}

	@Override
	public ServerAddress getServerAddress() {
		return profiledCursor.getServerAddress();
	}
}
