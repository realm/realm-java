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

package io.realm.internal;

import io.realm.internal.async.QueryUpdateTask;

// Interface needs to be implemented by Java and pass to OS in order to get notifications when other thread/process
// changes the Realm file.
public interface RealmNotifier {
    // This is called from Java when the changes have been made on the same thread.
    void notifyByLocalThread();
    // This is called in OS's JavaBindingContext::changes_available.
    // This is getting called on the same thread which created this Realm when the same Realm file has been changed by
    // other thread. The changes on the same thread should not trigger this call.
    @SuppressWarnings("unused")
    void notifyByOtherThread();

    // Is the current notifier valid? eg. Notifier created on non-looper thread cannot be notified.
    boolean isValid();

    // FIXME: These are for decoupling handler from async query. Async query needs refactor to either adapt the OS or
    //        abstract the logic from Android handlers.
    void completeAsyncResults(QueryUpdateTask.Result result);
    void completeAsyncObject(QueryUpdateTask.Result result);
    void throwBackgroundException(Throwable throwable);
    void completeUpdateAsyncQueries(QueryUpdateTask.Result result);
}
