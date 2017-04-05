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

import io.realm.internal.Capabilities;


/**
 * Realm capabilities for Android.
 */
public class AndroidCapabilities implements Capabilities {

    private final boolean hasLooper;
    private final boolean isIntentServiceThread;

    public AndroidCapabilities() {
        hasLooper = Looper.myLooper() != null;
        isIntentServiceThread = isIntentServiceThread();
    }

    @Override
    public boolean canDeliverNotification() {
        return hasLooper && !isIntentServiceThread;
    }

    @Override
    public void checkCanDeliverNotification(String exceptionMessage) {
        if (!hasLooper) {
            throw new IllegalStateException(exceptionMessage == null ? "" : (exceptionMessage + " ") +
                    "Realm cannot be automatically updated on a thread without a looper.");
        }
        if (isIntentServiceThread) {
            throw new IllegalStateException(exceptionMessage == null ? "" : (exceptionMessage + " ") +
                    "Realm cannot be automatically updated on an IntentService thread.");
        }
    }

    private static boolean isIntentServiceThread() {
        // Tries to determine if a thread is an IntentService thread. No public API can detect this,
        // so use the thread name as a heuristic:
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/IntentService.java#108
        String threadName = Thread.currentThread().getName();
        return threadName != null && threadName.startsWith("IntentService[");
    }
}
