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

import com.google.common.io.Files;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

import dk.ilios.spanner.model.Trial;
import dk.ilios.spanner.output.ResultProcessor;

/**
 * Converts the result of a benchmark to CSV for easier processing by other data/graph programs.
 *
 * Output is the following.
 * methodname, trialNumber, params, measurements, min, max, average, 25pct, 50pct, 75pct.
 */
public class CSVResultProcessor implements ResultProcessor {

    private  static final boolean APPLY_QUOTES = true;
    private static final DecimalFormat decimalFormater = new DecimalFormat("#.00");

    private final File resultFile;
    private final File workFile;
    private final CSVWriter writer;

    public CSVResultProcessor(File resultFile) {
        this.resultFile = resultFile;
        this.workFile = new File(resultFile.getPath() + ".tmp");
        try {
            writer = new CSVWriter(Files.newWriter(resultFile, Charset.forName("UTF-8")));
            addLabels();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addLabels() {
        String[] labels = new String[] {
                "Method name",
                "Trial",
                "Measurements",
                "Min.",
                "Max.",
                "Mean",
                "25pct.",
                "50pct.",
                "75pct.",
        };

        writer.writeNext(labels, APPLY_QUOTES);
    }

    @Override
    public void processTrial(Trial trial) {
        String methodName = trial.experiment().instrumentation().benchmarkMethod().getName();
        int trialNo = trial.getTrialNumber();
        int measurements = trial.measurements().size();
        double min = trial.getMin();
        double max = trial.getMax();
        double mean = trial.getMean();
        double percentile25 = trial.getPercentile(25);
        double percentile50 = trial.getMedian();
        double percentile75 = trial.getPercentile(75);

        String[] resultLine = new String[] {
                methodName,
                Integer.toString(trialNo),
                Integer.toString(measurements),
                decimalFormater.format(min),
                decimalFormater.format(max),
                decimalFormater.format(mean),
                decimalFormater.format(percentile25),
                decimalFormater.format(percentile50),
                decimalFormater.format(percentile75)
        };

        writer.writeNext(resultLine);
    }

    @Override
    public void close() throws IOException {
        writer.close();
        if (workFile.exists()) {
            Files.move(workFile, resultFile);
        }
    }
}
