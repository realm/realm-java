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
 *
 */

package io.realm.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.BuildConfig;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.log.RealmLog;

/**
 * Base class for all Realm instances.
 *
 * Developer note: Due to this class being in io.realm.internal. No JavaDoc can be used from this
 * class. This means that all classes that you want JavaDoc for should be implemented in the
 * subclass :(
 */
public abstract class RealmInstance implements Closeable {

    protected static final int REALM_CHANGED = 14930352; // Hopefully it won't clash with other message IDs.
    protected static final String INCORRECT_THREAD_CLOSE_MESSAGE = "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";
    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";
    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";

    // Reference count for how many open Realm instances there currently are on this thread.
    protected static final ThreadLocal<Map<RealmConfiguration, Integer>> referenceCount =
            new ThreadLocal<Map<RealmConfiguration,Integer>>() {
                @Override
                protected Map<RealmConfiguration, Integer> initialValue() {
                    return new HashMap<RealmConfiguration, Integer>();
                }
            };

    // Map between a Handler and the canonical path to a Realm file
    public static final Map<Handler, String> handlers = new ConcurrentHashMap<Handler, String>();

    protected final List<WeakReference<RealmChangeListener>> changeListeners =
            new CopyOnWriteArrayList<WeakReference<RealmChangeListener>>();

    protected long threadId;
    protected RealmConfiguration configuration;
    protected RealmFileWrapper realmFile;
    protected boolean autoRefresh;
    protected Handler handler;

    static {
        RealmLog.add(BuildConfig.DEBUG ? new DebugAndroidLogger() : new ReleaseAndroidLogger());
    }

    public RealmInstance(RealmConfiguration configuration, boolean autoRefresh) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.realmFile = new RealmFileWrapper(configuration);
        setAutoRefresh(autoRefresh);
    }

    protected void setAutoRefresh(boolean autoRefresh) {
        if (autoRefresh && Looper.myLooper() == null) {
            throw new IllegalStateException("Cannot set auto-refresh in a Thread without a Looper");
        }

        if (autoRefresh && !this.autoRefresh) { // Switch it on
            handler = new Handler(new RealmCallback());
            handlers.put(handler, configuration.getPath());
        } else if (!autoRefresh && this.autoRefresh && handler != null) { // Switch it off
            removeHandler(handler);
        }
        this.autoRefresh = autoRefresh;
    }

    protected boolean isAutoRefresh() {
        return autoRefresh;
    }

    /**
     * Add a change listener to the Realm
     */
    protected void addChangeListener(RealmChangeListener listener) {
        checkIfValid();
        for (WeakReference<RealmChangeListener> ref : changeListeners) {
            if (ref.get() == listener) {
                // It has already been added before
                return;
            }
        }

        changeListeners.add(new WeakReference<RealmChangeListener>(listener));
    }

    /**
     * Remove the specified change listener
     */
    protected void removeChangeListener(RealmChangeListener listener) {
        checkIfValid();
        WeakReference<RealmChangeListener> weakRefToRemove = null;
        for (WeakReference<RealmChangeListener> weakRef : changeListeners) {
            if (listener == weakRef.get()) {
                weakRefToRemove = weakRef;
                // There won't be duplicated entries, checking is done when adding
                break;
            }
        }
        if (weakRefToRemove != null) {
            changeListeners.remove(weakRefToRemove);
        }
    }

    /**
     * Remove all user-defined change listeners
     *
     * @see io.realm.RealmChangeListener
     */
    protected void removeAllChangeListeners() {
        checkIfValid();
        changeListeners.clear();
    }

    public void checkIfValid() {
        // Check if the Realm instance has been closed
        if (realmFile != null && !realmFile.isOpen()) {
            throw new IllegalStateException(CLOSED_REALM_MESSAGE);
        }

        // Check if we are in the right thread
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }
    }

    protected void removeHandler(Handler handler) {
        handler.removeCallbacksAndMessages(null);
        handlers.remove(handler);
    }

    protected void sendNotifications() {
        Iterator<WeakReference<RealmChangeListener>> iterator = changeListeners.iterator();
        List<WeakReference<RealmChangeListener>> toRemoveList = null;
        while (iterator.hasNext()) {
            WeakReference<RealmChangeListener> weakRef = iterator.next();
            RealmChangeListener listener = weakRef.get();
            if (listener == null) {
                if (toRemoveList == null) {
                    toRemoveList = new ArrayList<WeakReference<RealmChangeListener>>(changeListeners.size());
                }
                toRemoveList.add(weakRef);
            } else {
                listener.onChange();
            }
        }
        if (toRemoveList != null) {
            changeListeners.removeAll(toRemoveList);
        }
    }

    /**
     * Closes a single instance of a Realm.
     * @return {@code true} if it was the last instance close, {@code false} otherwise.
     */
    protected boolean closeInstance(RealmConfiguration configuration) {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_CLOSE_MESSAGE);
        }

        boolean wasLastInstance = false;
        Map<RealmConfiguration, Integer> localRefCount = referenceCount.get();
        String canonicalPath = configuration.getPath();
        Integer references = localRefCount.get(configuration);
        if (references == null) {
            references = 0;
        }

        if (references == 1) {
            wasLastInstance = true;
            realmFile.close();
            realmFile = null;
            RealmFileWrapper.closeFile(configuration);
        }

        int refCount = references - 1;
        if (refCount < 0) {
            RealmLog.w("Calling close() on a Realm that is already closed: " + canonicalPath);
        }
        localRefCount.put(configuration, Math.max(0, refCount));

        if (handler != null && refCount <= 0) {
            removeHandler(handler);
        }

        return wasLastInstance;
    }

    private class RealmCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == REALM_CHANGED) {
                realmFile.advanceRead();
                sendNotifications();
            }
            return true;
        }
    }
}
