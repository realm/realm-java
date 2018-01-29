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
 * result ({@link io.realm.OrderedCollectionChangeSet.State#INITIAL}.
 */
public class StatefulCollectionChangeSet implements OrderedCollectionChangeSet {

    private final OrderedCollectionChangeSet changeset;
    private final Throwable error;
    private final State state;
    private final boolean remoteDataSynchronized;

    /**
     * @param backingChangeset Underlying changeset backing this.
     */
    public StatefulCollectionChangeSet(OsCollectionChangeSet backingChangeset) {
        this.changeset = backingChangeset;

        // Calculate the state here since object is immutable
        boolean isInitial = backingChangeset.isFirstAsyncCallback();
        remoteDataSynchronized = backingChangeset.isRemoteDataLoaded();

        error = backingChangeset.getError();
        if (error != null) {
            state = State.ERROR;
        } else {
            state = (isInitial) ? State.INITIAL : State.UPDATE;
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

    @Override
    public boolean isCompleteResult() {
        return remoteDataSynchronized;
    }
}

