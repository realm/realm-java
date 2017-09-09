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

package io.realm.internal;

import android.annotation.SuppressLint;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * We are testing characteristic of the {@link ObserverPairList} here, such as:
 * Ownership of the listeners, equality of the pair and all public APIs for the class.
 */
@RunWith(AndroidJUnit4.class)
public class ObserverPairListTests {

    private static class TestListener<T> {
        void onChange(T integer) {
        }
    }

    private static class TestObserverPair extends ObserverPairList.ObserverPair<Integer, TestListener>  {
        TestObserverPair(Integer observer, TestListener listener) {
            super(observer, listener);
        }
    }

    private ObserverPairList<TestObserverPair> observerPairs;
    private TestListener testListener = new TestListener<Integer>();

    private static final Integer ONE = 1;
    private static final Integer TWO = 2;
    private static final Integer THREE = 3;

    @Before
    public void setUp() {
        observerPairs = new ObserverPairList<TestObserverPair>();
    }

    @After
    public void tearDown() {
        observerPairs = null;
    }

    @Test
    public void add() {
        TestObserverPair pair = new TestObserverPair(ONE, testListener);
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());

        // Same observer object, different listener.
        pair = new TestObserverPair(ONE, new TestListener<Integer>());
        observerPairs.add(pair);
        assertEquals(2, observerPairs.size());

        // Different observer object, different listener.
        pair = new TestObserverPair(TWO, new TestListener<Integer>());
        observerPairs.add(pair);
        assertEquals(3, observerPairs.size());

        // Different observer object, same listener.
        pair = new TestObserverPair(TWO, testListener);
        observerPairs.add(pair);
        assertEquals(4, observerPairs.size());
    }

    @Test
    // The Observer pair is treated as the same when the observer is the same object and the listener is the same too.
    public void add_noDuplicate() {
        TestObserverPair pair = new TestObserverPair(ONE, testListener);
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());

        pair = new TestObserverPair(ONE, testListener);
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());
    }

    // 1. add 2. clear 3. add 4. Check if the last listener can still be called.
    @Test
    public void add_worksAfterClears() {
        final AtomicBoolean foreachCalled = new AtomicBoolean(false);
        TestObserverPair pair = new TestObserverPair(ONE, testListener);
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());

        observerPairs.clear();

        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                assertEquals(ONE, observer);
                foreachCalled.set(true);
            }
        });
        assertTrue(foreachCalled.get());
    }

    @SuppressLint({"UseValueOf", "BoxedPrimitiveConstructor"})
    @Test
    public void remove() {
        TestObserverPair pair = new TestObserverPair(ONE, testListener);
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());

        // Create a new Integer 1 to see if the equality is checked by the same object.
        //noinspection UnnecessaryBoxing
        observerPairs.remove(new Integer(1), testListener);
        assertEquals(1, observerPairs.size());

        // Different listener
        observerPairs.remove(ONE, new TestListener());
        assertEquals(1, observerPairs.size());

        // Should remove now
        observerPairs.remove(ONE, testListener);
        assertEquals(0, observerPairs.size());
    }

    @Test
    public void removeByObserver() {
        TestObserverPair pair = new TestObserverPair(ONE, testListener);
        observerPairs.add(pair);
        pair = new TestObserverPair(ONE, new TestListener());
        observerPairs.add(pair);
        assertEquals(2, observerPairs.size());

        // An different observer
        //noinspection UnnecessaryBoxing
        pair = new TestObserverPair(TWO, testListener);
        observerPairs.add(pair);
        assertEquals(3, observerPairs.size());

        observerPairs.removeByObserver(ONE);
        assertEquals(1, observerPairs.size());

        observerPairs.removeByObserver(TWO);
        assertEquals(0, observerPairs.size());
    }

    @Test
    public void clear() {
        TestObserverPair pair = new TestObserverPair(ONE, new TestListener());
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());
        observerPairs.clear();
        assertEquals(0, observerPairs.size());
    }

    @Test
    public void isEmpty() {
        assertTrue(observerPairs.isEmpty());
        TestObserverPair pair = new TestObserverPair(ONE, new TestListener());
        observerPairs.add(pair);
        assertFalse(observerPairs.isEmpty());
        observerPairs.clear();
        assertTrue(observerPairs.isEmpty());
    }

    @Test
    public void foreach() {
        final boolean[] onChangesCalled = {false, false};
        TestListener<Integer> listener = new TestListener<Integer>() {
            @Override
            void onChange(Integer i) {
                onChangesCalled[i-1] = true;
            }
        };

        TestObserverPair pair = new TestObserverPair(ONE, listener);
        observerPairs.add(pair);
        pair = new TestObserverPair(TWO, listener);
        observerPairs.add(pair);
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                //noinspection unchecked
                pair.listener.onChange(observer);
            }
        });
        assertTrue(onChangesCalled[0] && onChangesCalled[1]);
    }

    // Test if the observer is GCed, the relevant listener should be removed when foreach called.
    @Test
    public void foreach_shouldRemoveWeakRefs() {
        TestObserverPair pair = new TestObserverPair(ONE, new TestListener());
        observerPairs.add(pair);
        assertEquals(1, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                // There is no guaranteed way to release the WeakReference,
                // just clear it.
                pair.observerRef.clear();
            }
        });
        assertEquals(1, observerPairs.size());

        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                fail();
            }
        });
        assertEquals(0, observerPairs.size());
    }

    @Test
    public void foreach_canRemove() {
        final AtomicInteger count = new AtomicInteger(0);
        final TestObserverPair pair1 = new TestObserverPair(ONE, new TestListener());
        final TestListener listener2 = new TestListener();
        final TestObserverPair pair2 = new TestObserverPair(TWO, listener2);
        final TestObserverPair pair3 = new TestObserverPair(THREE, new TestListener());
        observerPairs.add(pair1);
        observerPairs.add(pair2);
        observerPairs.add(pair3);
        assertEquals(3, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                assertFalse(((Integer) observer) == 2);
                observerPairs.remove(TWO, listener2);
                count.getAndIncrement();
            }
        });
        assertEquals(2, observerPairs.size());
        assertEquals(2, count.get());
    }

    @Test
    public void foreach_canClear() {
        final AtomicInteger count = new AtomicInteger(0);
        final TestObserverPair pair1 = new TestObserverPair(ONE, new TestListener());
        final TestObserverPair pair2 = new TestObserverPair(TWO, new TestListener());
        final TestObserverPair pair3 = new TestObserverPair(THREE, new TestListener());
        observerPairs.add(pair1);
        observerPairs.add(pair2);
        observerPairs.add(pair3);
        assertEquals(3, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                assertFalse(((Integer) observer) == 2);
                assertFalse(((Integer) observer) == 3);
                observerPairs.clear();
                count.getAndIncrement();
            }
        });
        assertEquals(0, observerPairs.size());
        assertEquals(1, count.get());

        observerPairs.add(pair1);
        assertEquals(1, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                assertTrue(((Integer) observer) == 1);
            }
        });
    }

    @Test
    public void foreach_canAdd() {
        final AtomicInteger count = new AtomicInteger(0);
        final TestObserverPair pair1 = new TestObserverPair(ONE, new TestListener());
        final TestObserverPair pair2 = new TestObserverPair(TWO, new TestListener());
        observerPairs.add(pair1);
        assertEquals(1, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                observerPairs.add(pair2);
                count.getAndIncrement();
            }
        });
        assertEquals(2, observerPairs.size());
        assertEquals(1, count.get());

        count.set(0);
        assertEquals(2, observerPairs.size());
        observerPairs.foreach(new ObserverPairList.Callback<TestObserverPair>() {
            @Override
            public void onCalled(TestObserverPair pair, Object observer) {
                count.getAndIncrement();
            }
        });
        assertEquals(2, count.get());
    }
}
