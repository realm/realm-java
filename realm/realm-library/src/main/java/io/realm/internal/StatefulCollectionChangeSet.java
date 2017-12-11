package io.realm.internal;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.log.RealmLog;

/**
 * A wrapper around {@link OsCollectionChangeSet} that makes it stateful with regard to how many
 * times it has been invoked.
 *
 * Note that Object Store will calculate the changes between the query was registered and when it
 * completes. This information is not useful and might even be misleading when reporting first
 * result ({@link io.realm.OrderedCollectionChangeSet.State#INITIAL} or
 * {@link io.realm.OrderedCollectionChangeSet.State#INITIAL_INCOMPLETE}. So in those cases
 * we override the result and return empty changeset arrays.
 */
public class StatefulCollectionChangeSet implements OrderedCollectionChangeSet {

    private static final int[] NO_INDEX_CHANGES = new int[0];
    private static final Range[] NO_RANGE_CHANGES = new Range[0];
    private final OrderedCollectionChangeSet changeset;
    private final Throwable error;
    private final State state;

    /**
     * @param backingChangeset Underlying changeset backing this.
     * @param callbackCount How many times have the listener been called before (
     */
    public StatefulCollectionChangeSet(OsCollectionChangeSet backingChangeset, int callbackCount) {
        this.changeset = backingChangeset;
        RealmLog.error(String.format("%s - %s", backingChangeset.getOldStatusCode(), backingChangeset.getNewStatusCode()));
        // Calculate the state here since object is immutable
        boolean isInitial = (callbackCount == 0);

        error = backingChangeset.getError();
        if (error != null) {
            state = State.ERROR;
        } else if (backingChangeset.isRemoteDataLoaded()) {
            state = (isInitial) ? State.INITIAL : State.UPDATE;
        } else {
            state = (isInitial) ? State.INITIAL_INCOMPLETE : State.UPDATE_INCOMPLETE;
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public int[] getDeletions() {
        return changeset.getDeletions();
    }

    @Override
    public int[] getInsertions() {
        if (state == State.INITIAL || state == State.INITIAL_INCOMPLETE) {
            return NO_INDEX_CHANGES;
        } else {
            return changeset.getInsertions();
        }
    }

    @Override
    public int[] getChanges() {
        if (state == State.INITIAL || state == State.INITIAL_INCOMPLETE) {
            return NO_INDEX_CHANGES;
        } else {
            return changeset.getChanges();
        }
    }

    @Override
    public Range[] getDeletionRanges() {
        if (state == State.INITIAL || state == State.INITIAL_INCOMPLETE) {
            return NO_RANGE_CHANGES;
        } else {
            return changeset.getDeletionRanges();
        }
    }

    @Override
    public Range[] getInsertionRanges() {
        if (state == State.INITIAL || state == State.INITIAL_INCOMPLETE) {
            return NO_RANGE_CHANGES;
        } else {
            return changeset.getInsertionRanges();
        }
    }

    @Override
    public Range[] getChangeRanges() {
        if (state == State.INITIAL || state == State.INITIAL_INCOMPLETE) {
            return NO_RANGE_CHANGES;
        } else {
            return changeset.getChangeRanges();
        }
    }

    @Nullable
    @Override
    public Throwable getError() {
        return error;
    }
}
