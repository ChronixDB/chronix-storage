/*
 * Copyright (C) 2016 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.lucene.client.stream;

import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Class to handle  future callbacks
 *
 * @param <T> the element type
 * @author f.lautenschlager
 */
public class TimeSeriesHandler<T> implements FutureCallback<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesHandler.class);
    /**
     * The blocking queue containing elements of type t
     */
    private BlockingQueue<T> queue;

    /**
     * Constructs a time series callback handler
     *
     * @param nrOfTimeDocumentsPerBatch the max nr of elements in the queue
     */
    public TimeSeriesHandler(int nrOfTimeDocumentsPerBatch) {
        this.queue = new ArrayBlockingQueue<>(nrOfTimeDocumentsPerBatch);
    }

    /**
     * On success we add the result to our blocking queue
     *
     * @param result - the resulting time series
     */
    @Override
    public void onSuccess(T result) {
        try {
            LOGGER.debug("Putting {} into queue", result);
            queue.put(result);
        } catch (InterruptedException e) {
            LOGGER.warn("Exception occurred while putting the converted result in queue", e);
        }
    }

    /**
     * On failure we doing a log and going on
     *
     * @param t - the throwable that was thrown
     */
    @Override
    public void onFailure(Throwable t) {
        LOGGER.warn("Exception occurred while converting documents.", t);
    }

    /**
     * Gets the first element in the queue.
     * If no element is present within one minute.
     * Then it returns null.
     *
     * @return the first element of type <T> in the queue
     */
    public T take() {
        try {
            T object = queue.poll(1, TimeUnit.MINUTES);
            LOGGER.debug("Getting element from queue: {}", object);
            return object;

        } catch (InterruptedException e) {
            LOGGER.warn("InterruptedException occurred. Returning null value to callee.", e);
            throw new IllegalStateException("Try to poll time series records for more than 1 Minute. Stopping.", e);
        }
    }
}
