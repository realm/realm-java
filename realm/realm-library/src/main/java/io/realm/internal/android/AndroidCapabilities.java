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
package io.realm.internal.android;

import android.os.Looper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;

import io.realm.internal.Capabilities;


/**
 * Realm capabilities for Android.
 */
public class AndroidCapabilities implements Capabilities {

    // Public so it can be set from tests.
    // If set, it will treat the current looper thread as the main thread.
    // It is up to the caller to handle any race conditions around this. Right now only
    // RunInLooperThread.java does this as part of setting up the test.
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static boolean EMULATE_MAIN_THREAD = false;

    private final Looper looper;
    private final boolean isIntentServiceThread;

    public AndroidCapabilities() {
        looper = Looper.myLooper();
        isIntentServiceThread = isIntentServiceThread();
    }

    @Override
    public boolean canDeliverNotification() {
        return hasLooper() && !isIntentServiceThread;
    }

    @Override
    public void checkCanDeliverNotification(@Nullable String exceptionMessage) {
        if (!hasLooper()) {
            throw new IllegalStateException(exceptionMessage == null ? "" : (exceptionMessage + " ") +
                    "Realm cannot be automatically updated on a thread without a looper.");
        }
        if (isIntentServiceThread) {
            throw new IllegalStateException(exceptionMessage == null ? "" : (exceptionMessage + " ") +
                    "Realm cannot be automatically updated on an IntentService thread.");
        }
    }

    @Override
    public boolean isMainThread() {
        return looper != null && (EMULATE_MAIN_THREAD || looper == Looper.getMainLooper());
    }

    private boolean hasLooper() {
        return looper != null;
    }

    private static boolean isIntentServiceThread() {
        // Tries to determine if a thread is an IntentService thread. No public API can detect this,
        // so use the thread name as a heuristic:
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/IntentService.java#108
        String threadName = Thread.currentThread().getName();
        return threadName != null && threadName.startsWith("IntentService[");
    }
}
