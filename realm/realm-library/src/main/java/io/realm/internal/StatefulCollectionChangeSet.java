package io.realm.internal;

/**
 * Created by cm on 12/4/17.
 */

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;

/**
 * A wrapper around {@link OsCollectionChangeSet} that makes it stateful with regard to how many
 * times it has been invoked (this information is not in Object Store, nor should it (probably)).
 */
public class StatefulCollectionChangeSet implements OrderedCollectionChangeSet {

    private final OsCollectionChangeSet changeset;
    private final Throwable error;
    private final State state;

    /**
     *
     * @param backingChangeset Underlying changeset backing this.
     * @param callbackCount How many times have the listener been called before (
     */
    public StatefulCollectionChangeSet(OsCollectionChangeSet backingChangeset, int callbackCount) {
        this.changeset = backingChangeset;

        // Calculate the state here since object is immutable
        boolean isInitial = callbackCount > 0;
        error = changeset.getError();
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
        return changeset.getInsertions();
    }

    @Override
    public int[] getChanges() {
        return changeset.getChanges();
    }

    @Override
    public Range[] getDeletionRanges() {
        return changeset.getDeletionRanges();
    }

    @Override
    public Range[] getInsertionRanges() {
        return changeset.getInsertionRanges();
    }

    @Override
    public Range[] getChangeRanges() {
        return changeset.getChangeRanges();
    }

    @Nullable
    @Override
    public Throwable getError() {
        return error;
    }
}
