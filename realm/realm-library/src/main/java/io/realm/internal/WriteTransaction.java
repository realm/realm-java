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

package io.realm.internal;

public class WriteTransaction extends Group {

    private final SharedGroup db;
    private boolean committed;

    public void commit() {
        if (!committed) {
            db.commit();
            committed = true;
        }
        else {
            throw new IllegalStateException("You can only commit once after a WriteTransaction has been made.");
        }
    }

    public void rollback() {
        db.rollback();
    }

    @Override
    public void close() {
        if (!committed) {
            rollback();
        }
    }

    WriteTransaction(Context context,SharedGroup db, long nativePtr) {
        super(context, nativePtr, false);    // Group is mutable
        this.db = db;
        committed = false;
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}
