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
package io.realm.internal;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.internal.android.AndroidCapabilities;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidCapabilitiesTest {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Test
    @RunTestInLooperThread()
    public void emulateMainThread_false() {
        assertFalse(new AndroidCapabilities().isMainThread());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void emulateMainThread_true() {
        assertTrue(new AndroidCapabilities().isMainThread());
        looperThread.testComplete();
    }

}
