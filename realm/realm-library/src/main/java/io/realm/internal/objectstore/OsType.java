package io.realm.internal.objectstore;

/**
 * Hackish way to tell Object Store code what kind of object a jlong pointer reference is.
 * Used so we can correctly cast pointers coming from Java instead of having to create a method for each variant.
 */
public class OsType {
    public static final int LIST = 0;
    public static final int OBJECT = 1;
    public static final int QUERY = 2;
    public static final int RESULT = 3;

    boolean isValid(int val) {
        return val >= LIST && val <= RESULT;
    }
}
