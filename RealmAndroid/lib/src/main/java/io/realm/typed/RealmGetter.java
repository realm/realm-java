package io.realm.typed;

import io.realm.TableOrView;

public interface RealmGetter {

    public Object get(TableOrView table, long rowIndex);

}
