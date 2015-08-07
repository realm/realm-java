package io.realm.dynamic;

import io.realm.RealmQuery;
import io.realm.base.BaseRealmResults;
import io.realm.internal.TableOrView;
import io.realm.internal.TableView;

/**
 * TODO Add JavaDoc
 */
public class DynamicRealmResults extends BaseRealmResults<DynamicRealmObject, DynamicRealmQuery> {

    private final DynamicRealm realm;
    private final String className;

    public DynamicRealmResults(DynamicRealm realm, TableView queryResult, String className) {
        super(queryResult);
        this.realm = realm;
        this.className = className;
    }

    public DynamicRealm getRealm() {
        return null;
    }

    @Override
    protected TableOrView getTable() {
        if (table == null) {
            return realm.getTable(className);
        } else {
            return table;
        }
    }

    @Override
    protected DynamicRealmQuery getQuery() {
        return new DynamicRealmQuery(realm, className);
    }

    @Override
    protected DynamicRealmObject getObject(long sourceRowIndex) {
        return null;
    }

    @Override
    protected void checkIsRealmValid() {
        realm.checkIsValid();
    }
}
