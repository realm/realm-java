package io.realm.annotations;

/**
 * This enum is used when defining a {@link RealmName} for a class or field. Instead of
 * manual mapping the field name, a pre-defined policy can be applied.
 */
public enum RealmNamePolicy {

    /**
     * No policy is applied. This policy will not override any policy set on a parent element, e.g.
     * if set on a Class type, the module policy will still apply to field names.
     */
    NO_POLICY,

    /**
     * The name in the Java model class is used as is.
     */
    IDENTITY,

    /**
     * The name in the Java model class is converted to all lower case, and each word is separated
     * by {@code _}.
     * <p>
     * Examples:
     * <ul>
     *     <li>
     *         "firstName" becomes "first_name"
     *     </li>
     * </ul>
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    LOWER_CASE_WITH_UNDERSCORES,

    /**
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    LOWER_CASE_WITH_DASHES,

    /**
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    CAMEL_CASE,

    /**
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    PASCAL_CASE
}
