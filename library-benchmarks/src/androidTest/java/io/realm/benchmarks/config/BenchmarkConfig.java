/*
 * Copyright 2016 Realm Inc.
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

package io.realm.benchmarks.config;

import android.os.Environment;

import java.io.File;
import java.util.concurrent.TimeUnit;

import dk.ilios.spanner.SpannerConfig;
import dk.ilios.spanner.config.RuntimeInstrumentConfig;
import dk.ilios.spanner.output.ResultProcessor;

/**
 * Static helper class for creating benchmark configurations
 * */
public class BenchmarkConfig {

    public static SpannerConfig getConfiguration(String className) {
        // Document directory is located at: /sdcard/realm-benchmarks
        // Benchmarks results should be saved in <documentFolder>/results/<className>.json
        // Baseline data should be found in <documentFolder>/baselines/<className>.json
        // Custom CSV files should be found in <documentFolder>/csv/<className>.csv
        File externalDocuments = new File(Environment.getExternalStorageDirectory(), "realm-benchmarks");
        if (!externalDocuments.exists() && !externalDocuments.mkdir()) {
            throw new RuntimeException("Could not create benchmark directory: " + externalDocuments);
        }
        File resultsDir = new File(externalDocuments, "results");
        File baselineDir = new File(externalDocuments, "baselines");
        File baselineFile = new File(baselineDir, className + ".json");
        File csvDir = new File(externalDocuments, "csv");
        csvDir.mkdir();
        File csvFile = new File(csvDir, className + ".csv");
        ResultProcessor csvResultProcessor = new CSVResultProcessor(csvFile);

        // General configuration for running benchmarks.
        // Always saves result files. CI will determine if it wants to store them.
        SpannerConfig.Builder builder = new SpannerConfig.Builder()
                .saveResults(resultsDir, className + ".json")
                .trialsPrExperiment(1)
                .maxBenchmarkThreads(1)
                .addInstrument(new RuntimeInstrumentConfig.Builder()
                                .gcBeforeEachMeasurement(true)
                                .warmupTime(0, TimeUnit.SECONDS)
                                .timingInterval(500, TimeUnit.MILLISECONDS)
                                .measurements(9)
                                .build()
                )
                .addResultProcessor(csvResultProcessor);

        // Only uses baseline file if it exists.
        if (baselineFile.exists()) {
            builder.useBaseline(baselineFile);
            // Tests that 25. , 50. and 75. percentile doesn't change by more than 15%.
            builder.percentileFailureLimit(25f, 0.15f);
            builder.percentileFailureLimit(50f, 0.15f);
            builder.percentileFailureLimit(75f, 0.15f);
        }

        return builder.build();
    }
}
