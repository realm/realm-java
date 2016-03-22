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

import io.realm.internal.async.BadVersionException;

public class ImplicitTransaction extends Group {

    private final SharedGroup parent;

    public ImplicitTransaction(Context context, SharedGroup sharedGroup, long nativePtr) {
        super(context, nativePtr, true);
        parent = sharedGroup;
    }

    /**
     * Positions the shared group to the latest version.
     */
    public void advanceRead() {
        assertNotClosed();
        parent.advanceRead();
    }

    /**
     * Positions the shared group at the specified version.
     *
     * @param versionID version of the shared group.
     */
    public void advanceRead(SharedGroup.VersionID versionID) throws BadVersionException {
        assertNotClosed();
        parent.advanceRead(versionID);
    }

    public void promoteToWrite() {
        assertNotClosed();
        if (!immutable) {
            throw new IllegalStateException("Nested transactions are not allowed. Use commitTransaction() after each beginTransaction().");
        }
        immutable = false;
        parent.promoteToWrite();
    }

    public void commitAndContinueAsRead() {
        assertNotClosed();
        if (immutable) {
            throw new IllegalStateException("Not inside a transaction.");
        }
        parent.commitAndContinueAsRead();
        immutable = true;
    }

    public void endRead() {
        assertNotClosed();
        parent.endRead();
    }

    public void rollbackAndContinueAsRead() {
        assertNotClosed();
        if (immutable) {
            throw new IllegalStateException("Not inside a transaction.");
        }
        parent.rollbackAndContinueAsRead();
        immutable = true;
    }

    private void assertNotClosed() {
        if (isClosed() || parent.isClosed()) {
            throw new IllegalStateException("Cannot use ImplicitTransaction after it or its parent has been closed.");
        }
    }

    /**
     * Returns the absolute path to the Realm file backing this transaction.
     */
    public String getPath() {
        return parent.getPath();
    }

    protected void finalize() {} // Nullify the actions of Group.finalize()
}
