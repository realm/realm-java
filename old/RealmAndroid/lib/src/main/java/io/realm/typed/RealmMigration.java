package io.realm.typed;

public interface RealmMigration {

    public void execute(Realm realm, int version);

}
