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

package io.realm.examples.concurrency.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Person;

// Note that an IntentService operates in its own thread-pool facilitated by Android
public class TransactionService extends IntentService {

    public static final String TAG = TransactionService.class.getName();

    private Boolean mQuitting = false;

    private int mInsertCount = 0;

    public static final String REALM_TESTTYPE_EXTRA = "TestTypeExtra";
    public static final String ITERATION_COUNT      = "TestIterationCount";

    public enum TestType {
        MANY_INSERTS_ONE_TRANSACTION,
        MANY_TRANSACTIONS
    }

    private Realm realm = null;

    public TransactionService() {
        super(TransactionService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, TAG + " is starting");

        realm = Realm.getInstance(this);

        TestType type = (TestType)intent.getSerializableExtra(REALM_TESTTYPE_EXTRA);
        mInsertCount = intent.getIntExtra(ITERATION_COUNT, 0);

        switch(type) {
            case MANY_INSERTS_ONE_TRANSACTION:
                doSingleTransaction();
                break;
            case MANY_TRANSACTIONS:
                doSeveralTransactions();
                break;
            default:
        }

        Log.d(TAG, TAG + " has quit");
    }

    @Override
    public void onDestroy() {
        this.mQuitting = true;
        realm.close();
    }

    // This method creates mInsertCount injections into the Realm
    // inside >>ONE transaction<<
    private void doSingleTransaction() {
        int iterCount = 0;

        // NOTE:  If you attempt to make writes from the UI while this loop is running
        // it will block because of the write transaction being open.
        realm.beginTransaction();
        while (iterCount < mInsertCount && !mQuitting) {
            if ((iterCount % 1000) == 0) {
                Log.d(TAG, "WriteOperation#: " + iterCount + "," + Thread.currentThread().getName());
            }

            Person person = realm.createObject(Person.class);
            person.setName("Foo" + iterCount);
            iterCount++;
        }
        realm.commitTransaction();
    }

    // This method creates mInsertCount injections into the Realm
    // using >>one transaction for EACH insert<<
    private void doSeveralTransactions() {
        int iterCount = 0;

        while (iterCount < mInsertCount && !mQuitting) {
            if ((iterCount % 1000) == 0) {
                Log.d(TAG, "WriteOperation#: " + iterCount + "," + Thread.currentThread().getName());
            }
            realm.beginTransaction();
            Person person = realm.createObject(Person.class);
            person.setName("Foo" + iterCount);
            iterCount++;
            realm.commitTransaction();
        }
    }
}
