package io.realm;

public interface RealmMigration {

    public void execute(Realm realm, int version);

}
