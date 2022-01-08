package io.realm.internal.objectstore;

import java.util.Iterator;

import javax.annotation.Nullable;

import io.realm.RealmQuery;
import io.realm.internal.NativeObject;
import io.realm.internal.RealmProxyMediator;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SubscriptionSet;

public class OsSubscriptionSet implements NativeObject, SubscriptionSet {

    public static final byte STATE_VALUE_UNCOMMITTED = 0;
    public static final byte STATE_VALUE_PENDING = 1;
    public static final byte STATE_VALUE_BOOTSTRAPPING = 2;
    public static final byte STATE_VALUE_COMPLETE = 3;
    public static final byte STATE_VALUE_ERROR = 4;
    public static final byte STATE_VALUE_SUPERCEDED = 5;

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();
    protected final RealmProxyMediator schema;
    private final long nativePtr;

    public OsSubscriptionSet(long nativePtr, RealmProxyMediator schema) {
        this.nativePtr = nativePtr;
        this.schema = schema;
    }

    @Override
    public long getNativePtr() {
        return this.nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetFinalizerMethodPtr();

    @Nullable
    @Override
    public Subscription find(RealmQuery query) {
        long subscriptionPtr = nativeFindByQuery(nativePtr, query.query.getNativePtr());
        if (subscriptionPtr != -1) {
            return new OsSubscription(subscriptionPtr);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Subscription findByName(String name) {
        long subscriptionPtr = nativeFindByName(nativePtr, name);
        if (subscriptionPtr != -1) {
            return new OsSubscription(subscriptionPtr);
        } else {
            return null;
        }
    }

    @Override
    public SubscriptionSet.State getState() {
        byte value = nativeState(nativePtr);
        return SubscriptionSet.State.fromNativeValue(value);
    }

    @Override
    public int size() {
        return (int) nativeSize(nativePtr);
    }

    @Override
    public String getErrorMessage() {
        return nativeErrorMessage(nativePtr);
    }

    @Override
    public boolean waitForSynchronization() {
        long updatedSubsPtr = nativeWaitForSynchronization(nativePtr);
        return updatedSubsPtr != -1; // FIXME native interface
    }

    @Override
    public SubscriptionSet update(UpdateCallback action) {
        OsMutableSubscriptionSet mutableSubs = new OsMutableSubscriptionSet(nativeCreateMutableSubscriptionSet(nativePtr), schema);
        action.update(mutableSubs);
        // FIXME: What about errors?
        return mutableSubs.commit();
    }

    @Override
    public void refresh() {
        nativeRefresh(nativePtr);
    }

    @Override
    public Iterator<Subscription> iterator() {
        // This iterator does not support removals.
        return new Iterator<Subscription>() {
            private int cursor = 0;
            private final int size = size();

            @Override
            public boolean hasNext() {
                return (cursor < size);
            }

            @Override
            public Subscription next() {
                long subscriptionPtr = nativeSubscriptionAt(nativePtr, cursor);
                cursor++;
                return new OsSubscription(subscriptionPtr);
            }
        };
    }

    private static native long nativeSize(long nativePtr);
    private static native byte nativeState(long nativePtr);
    private static native String nativeErrorMessage(long nativePtr);
    private static native long nativeCreateMutableSubscriptionSet(long nativePtr);
    private static native long nativeSubscriptionAt(long nativePtr, int index);
    private static native long nativeWaitForSynchronization(long nativePtr);
    private static native long nativeFindByName(long nativePtr, String name);
    private static native long nativeFindByQuery(long nativePtr, long queryPtr);
    private static native void nativeRefresh(long nativePtr);
}
