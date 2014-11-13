/*
 * Copyright 2014 Realm Inc.
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

package io.realm.performancetest.eventbus;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class JobCompletedEvent {

    private final String description;
    private final long[] results; // Results in millis. -1 means error and run is discounted.
    private DescriptiveStatistics calculatedResults;

    private JobCompletedEvent(String description, long[] results) {
        this.description = description;
        this.results = results;
        calculateResults();
    }

    private void calculateResults() {
        List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < results.length; i++) {
            if (results[i] > -1) {
                list.add((double) results[i]);
            }
        }
        double[] realDataList = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            realDataList[i] = list.get(i);
        }

        calculatedResults = new DescriptiveStatistics(realDataList);
    }

    public String getDescription() {
        return description;
    }

    public long[] getResults() {
        return results;
    }

    public long getMin() {
        return (long) calculatedResults.getMin();
    }

    public long getMax() {
        return (long) calculatedResults.getMax();
    }

    public double getMedian() {
        return calculatedResults.getPercentile(50);
    }

    public double getAverage() {
        return calculatedResults.getMean();
    }

}
