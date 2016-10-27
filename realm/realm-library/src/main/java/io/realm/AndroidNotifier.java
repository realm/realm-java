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

package io.realm;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import io.realm.internal.HandlerControllerConstants;
import io.realm.internal.RealmNotifier;
import io.realm.internal.async.QueryUpdateTask;
import io.realm.log.RealmLog;

/**
 * Implementation of {@link RealmNotifier} for Android based on {@link Handler}.
 */
class AndroidNotifier implements RealmNotifier {
    private Handler handler;

    public AndroidNotifier(HandlerController handlerController) {
        if (isAutoRefreshAvailable()) {
            handler = new Handler(handlerController);
        }
    }

    // Called by Java when transaction committed to send LOCAL_COMMIT to current thread's handler.
    @Override
    public void notifyCommitByLocalThread() {
        if (handler == null) {
            return;
        }

        // Force any updates on the current thread to the front the queue. Doing this is mostly
        // relevant on the UI thread where it could otherwise process a motion event before the
        // REALM_CHANGED event. This could in turn cause a UI component like ListView to crash. See
        // https://github.com/realm/realm-android-adapters/issues/11 for such a case.
        // Other Looper threads could process similar events. For that reason all looper threads will
        // prioritize local commits.
        //
        // If a user is doing commits inside a RealmChangeListener this can cause the Looper thread to get
        // event starved as it only starts handling Realm events instead. This is an acceptable risk as
        // that behaviour indicate a user bug. Previously this would be hidden as the UI would still
        // be responsive.
        Message msg = Message.obtain();
        msg.what = HandlerControllerConstants.LOCAL_COMMIT;
        if (!handler.hasMessages(HandlerControllerConstants.LOCAL_COMMIT)) {
            handler.removeMessages(HandlerControllerConstants.REALM_CHANGED);
            handler.sendMessageAtFrontOfQueue(msg);
        }
    }

    // This is called by OS when other thread/process changes the Realm.
    // This is getting called on the same thread which created the Realm.
    // |---------------------------------------------------------------+--------------+------------------------------------------------|
    // | Thread A                                                      | Thread B     | Daemon Thread                                  |
    // |---------------------------------------------------------------+--------------+------------------------------------------------|
    // |                                                               | Make changes |                                                |
    // |                                                               |              | Detect and notify thread A through JNI ALooper |
    // | Call OS's Realm::notify() from OS's ALooper callback          |              |                                                |
    // | Realm::notify() calls JavaBindingContext:change_available()   |              |                                                |
    // | change_available calls into this method to send REALM_CHANGED |              |                                                |
    // |---------------------------------------------------------------+--------------+------------------------------------------------|
    @Override
    public void notifyCommitByOtherThread() {
        if (handler == null) {
            return;
        }

        // Note there is a race condition with handler.hasMessages() and handler.sendEmptyMessage()
        // as the target thread consumes messages at the same time. In this case it is not a problem as worst
        // case we end up with two REALM_CHANGED messages in the queue.
        boolean messageHandled = true;
        if (!handler.hasMessages(HandlerControllerConstants.REALM_CHANGED) &&
                !handler.hasMessages(HandlerControllerConstants.LOCAL_COMMIT)) {
            messageHandled = handler.sendEmptyMessage(HandlerControllerConstants.REALM_CHANGED);
        }
        if (!messageHandled) {
            RealmLog.warn("Cannot update Looper threads when the Looper has quit. Use realm.setAutoRefresh(false) " +
                    "to prevent this.");
        }
    }

    @Override
    public void post(Runnable runnable) {
        Looper looper = handler.getLooper();
        if (looper.getThread().isAlive()) {     // The receiving thread is alive
            handler.post(runnable);
        }
    }

    @Override
    public boolean isValid() {
        return handler != null;
    }

    @Override
    public void close() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public void completeAsyncResults(QueryUpdateTask.Result result) {
        Looper looper = handler.getLooper();
        if (looper.getThread().isAlive()) {     // The receiving thread is alive
            handler.obtainMessage(HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS, result).sendToTarget();
        }
    }

    @Override
    public void completeAsyncObject(QueryUpdateTask.Result result) {
        Looper looper = handler.getLooper();
        if (looper.getThread().isAlive()) {     // The receiving thread is alive
            handler.obtainMessage(HandlerControllerConstants.COMPLETED_ASYNC_REALM_OBJECT, result).sendToTarget();
        }
    }

    @Override
    public void throwBackgroundException(Throwable throwable) {
        Looper looper = handler.getLooper();
        if (looper.getThread().isAlive()) {     // The receiving thread is alive
            handler.obtainMessage(
                    HandlerControllerConstants.REALM_ASYNC_BACKGROUND_EXCEPTION, new Error(throwable)).sendToTarget();
        }
    }

    @Override
    public void completeUpdateAsyncQueries(QueryUpdateTask.Result result) {
        Looper looper = handler.getLooper();
        if (looper.getThread().isAlive()) {     // The receiving thread is alive
            handler.obtainMessage(HandlerControllerConstants.COMPLETED_UPDATE_ASYNC_QUERIES, result).sendToTarget();
        }
    }

    private static boolean isAutoRefreshAvailable() {
        return (Looper.myLooper() != null && !isIntentServiceThread());
    }

    private static boolean isIntentServiceThread() {
        // Tries to determine if a thread is an IntentService thread. No public API can detect this,
        // so use the thread name as a heuristic:
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/IntentService.java#108
        String threadName = Thread.currentThread().getName();
        return threadName != null && threadName.startsWith("IntentService[");
    }

    // For testing purpose only. Should be removed ideally.
    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
