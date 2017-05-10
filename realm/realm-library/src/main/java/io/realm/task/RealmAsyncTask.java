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
package io.realm.task;

import android.os.AsyncTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.ThreadSafeRef;


public abstract class RealmAsyncTask<P extends RealmObject, R extends RealmObject> {

    private class Task extends AsyncTask<ThreadSafeRef<P>, Void, ThreadSafeRef<R>> {

        @Override
        protected ThreadSafeRef<R> doInBackground(ThreadSafeRef<P>... params) {
            int n = params.length;
            Realm localRealm = Realm.getInstance(configuration);
            Object[] nakedParams = new Object[n]; // can't say "new P[]"
            for (int i = 0; i < n; i++) {
                nakedParams[i] = params[i].resolve(localRealm);
            }
            R ret = RealmAsyncTask.this.doInBackground(localRealm, (P[]) nakedParams);
            return ThreadSafeRef.createThreadSafeRef(localRealm, retClazz, ret);
        }

        @Override
        protected void onPostExecute(ThreadSafeRef<R> ref) {
            RealmAsyncTask.this.onPostExecute(ref.resolve(realm));
        }
    }

    private final Realm realm;
    private final RealmConfiguration configuration;
    private final Class<P> paramClazz;
    private final Class<R> retClazz;

    public RealmAsyncTask(Realm realm, RealmConfiguration configuration, Class<P> paramClazz, Class<R> retClazz) {
        this.realm = realm;
        this.configuration = configuration;
        this.paramClazz = paramClazz;
        this.retClazz = retClazz;
    }

    protected abstract R doInBackground(Realm realm, P... params);

    @SuppressWarnings("UnusedParameters")
    protected void onPostExecute(R result) { }

    public final void execute(P... args) {
        int n = args.length;
        ThreadSafeRef<P>[] threadSafeArgs = new ThreadSafeRef[n];
        for (int i = 0; i < n; i++) {
            threadSafeArgs[i] = ThreadSafeRef.createThreadSafeRef(realm, paramClazz, args[i]);
        }

        new Task().execute(threadSafeArgs);
    }
}
