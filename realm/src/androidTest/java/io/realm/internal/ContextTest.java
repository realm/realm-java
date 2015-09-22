/*
 * Copyright 2015 Realm Inc.
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

import android.test.AndroidTestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.Queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContextTest extends AndroidTestCase {

    @Override
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
    }

    public void testCleanRows() throws Exception {
        // Queue queue the will contain the actual references
        final Queue<NativeObjectReference> references = new LinkedList<NativeObjectReference>();

        // Create the mock referenceQueue that uses the real queue.
        // This is done because ReferenceQueue.enqueue() is package protected
        ReferenceQueue mockedReferenceQueue = mock(ReferenceQueue.class);
        when(mockedReferenceQueue.poll()).thenAnswer(new Answer<NativeObjectReference>() {
            @Override
            public NativeObjectReference answer(InvocationOnMock invocation) throws Throwable {
                return references.poll();
            }
        });

        // Mock a native object
        NativeObjectReference mockedNativeObjectReference = mock(NativeObjectReference.class);

        // Add it to the reference queue
        references.add(mockedNativeObjectReference);

        // Create the context with the injected mocked reference queue
        Context context = new Context(mockedReferenceQueue);

        try {
            // This should fail when trying to access the field of the mocked object
            // This is sub-optimal, but the alternative is to create a real RealmObject and
            // expose its UncheckedRow
            context.cleanRows();
            fail();
        } catch (UnsatisfiedLinkError e) {
            verify(mockedReferenceQueue).poll();
            assertEquals(0, references.size());
        }

    }
}
