package io.realm.coroutines;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmResults;
import kotlinx.coroutines.flow.Flow;

/**
 * FIXME
 */
public interface CoroutinesFactory {
    /**
     * FIXME
     * @param realm
     * @param results
     * @param <T>
     * @return
     */
    <T> Flow<RealmResults<T>> from(@Nonnull Realm realm, @Nonnull RealmResults<T> results);
}
