/*
 * Copyright 2017 Realm Inc.
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

package io.realm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

@Ignore("FIXME: RealmApp refactor")
@RunWith(AndroidJUnit4.class)
public class ProgressTests {

    @Test
    public void getFractionTransferred() {
        Object[][] testData = {
            { 0L, 0L, 1.0D },
            { 0L, 1L, 0.0D },
            { 1L, 1L, 1.0D },
            { 1L, 2L, 0.5D }
        };

        for (Object[] test : testData) {
            long transferredBytes = (long) test[0];
            long transferableBytes = (long) test[1];
            double fraction = (double) test[2];
            Progress progress = new Progress(transferredBytes, transferableBytes);
            String errorMessage = String.format(Locale.US, "Failed with: (%d, %d)", transferredBytes, transferableBytes);
            assertEquals(errorMessage, fraction, progress.getFractionTransferred(), 0.0D);
        }
    }

    @Test
    public void getTransferredBytes () {
        long[] testData = { 0, Long.MAX_VALUE };

        for (long transferredBytes : testData) {
            String errorMessage = String.format(Locale.US, "Failed with: %d", transferredBytes);
            Progress progress = new Progress(transferredBytes, Long.MAX_VALUE);
            assertEquals(errorMessage, transferredBytes, progress.getTransferredBytes());
        }
    }

    @Test
    public void getTransferableBytes () {
        long[] testData = { 0, Long.MAX_VALUE };

        for (long transferableBytes : testData) {
            String errorMessage = String.format(Locale.US, "Failed with: %d", transferableBytes);
            Progress progress = new Progress(0, transferableBytes);
            assertEquals(errorMessage, transferableBytes, progress.getTransferableBytes());
        }
    }

}
