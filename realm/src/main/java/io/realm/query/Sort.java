package io.realm.query;

/**
 * This class describes the sorting order used in Realm queries.
 *
 * @see io.realm.Realm#allObjectsSorted(Class, String, Sort)
 * @see io.realm.RealmQuery#findAllSorted(String, Sort)
 */
public enum Sort {
    ASCENDING(true),
    DESCENDING(false);

    private final boolean value;

    Sort(boolean value) {
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
