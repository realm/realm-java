package io.realm.query;

/**
 * This class describes the Case settings used in Realm queries.
 *
 * @see io.realm.RealmQuery#equalTo(String, String, Case)
 * @see io.realm.RealmQuery#contains(String, String, Case)
 * @see io.realm.RealmQuery#beginsWith(String, String, Case)
 * @see io.realm.RealmQuery#endsWith(String, String, Case)
 */
public enum Case {
    SENSITIVE(true),
    INSENSITIVE(false);

    private final boolean value;

    Case(boolean value) {
        this.value = value;
    }

    /**
     * Returns the value for this setting that is used by the underlying query engine.
     * @return The value used by the underlying query engine to indicate this value.
     */
    public boolean getValue() {
        return value;
    }
}
