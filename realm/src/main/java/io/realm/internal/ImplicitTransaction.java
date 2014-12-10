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

public class ImplicitTransaction extends Group {

    private final SharedGroup parent;

    public ImplicitTransaction(Context context, SharedGroup sharedGroup, long nativePtr) {
        super(context, nativePtr, true);
        parent = sharedGroup;
    }

    public void advanceRead() {
        assertNotClosed();
        parent.advanceRead();
    }

    public void promoteToWrite() {
        assertNotClosed();
        if (immutable) {
            immutable = false;
            parent.promoteToWrite();
        } else {
            throw new IllegalStateException("Nested transactions are not allowed. Use commitTransaction() after each beginTransaction().");
        }
    }

    public void commitAndContinueAsRead() {
        assertNotClosed();
        parent.commitAndContinueAsRead();
        immutable = true;
    }

    public void endRead() {
        assertNotClosed();
        parent.endRead();
    }

    public void rollbackAndContinueAsRead() {
        assertNotClosed();
        if (!immutable) {
            parent.rollbackAndContinueAsRead();
            immutable = true;
        } else {
            throw new IllegalStateException("Cannot cancel a non-write transaction.");
        }
    }

    private void assertNotClosed() {
        if (isClosed() || parent.isClosed()) {
            throw new IllegalStateException("Cannot use ImplicitTransaction after it or its parent has been closed.");
        }
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}
