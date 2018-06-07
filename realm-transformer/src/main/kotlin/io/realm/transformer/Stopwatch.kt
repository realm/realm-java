/*
 * Copyright 2018 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.transformer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class Stopwatch {

    val logger: Logger = LoggerFactory.getLogger("realm-stopwatch")

    var start: Long = -1L
    var lastSplit: Long = -1L
    lateinit var label: String

    /*
     * Start the stopwatch.
     */
    fun start(label: String) {
        if (start != -1L) {
            throw IllegalStateException("Stopwatch was already started");
        }
        this.label = label
        start = System.nanoTime();
        lastSplit = start;
    }

    /*
     * Reports the split time.
     *
     * @param label Label to use when printing split time
     * @param reportDiffFromLastSplit if `true` report the time from last split instead of the start
     */
    fun splitTime(label: String, reportDiffFromLastSplit: Boolean = true) {
        val split = System.nanoTime()
        val diff = if (reportDiffFromLastSplit) { split - lastSplit } else { split - start }
        lastSplit = split;
        logger.debug("$label: ${TimeUnit.NANOSECONDS.toMillis(diff)} ms.")
    }

    /**
     * Stops the timer and report the result.
     */
    fun stop() {
        val stop = System.nanoTime()
        val diff = stop - start
        logger.debug("$label: ${TimeUnit.NANOSECONDS.toMillis(diff)} ms.")
    }
}