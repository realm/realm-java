package io.realm.annotations;

/**
 * This enum defines the possible ways class and field names can be mapped from what is used in Java
 * to the name used internally in Realm.
 * <p>
 * Examples where this can be useful is e.g:
 * <ul>
 *      <li>
 *          To support two model classes with the same name in different packages.
 *      </li>
 *      <li>
 *          To make it easier to work with cross platform schemas as naming conventions are different.
 *      </li>
 *      <li>
 *          To bring a class name below the normal 57 character limit.
 *      </li>
 * </ul>
 *
 * Depending on where the policy is applied, it will have slightly different semantics:
 * <ul>
 *     <li>
 *         If applied to {@link RealmModule#classNamingPolicy} all classes part of that module
 *         will be affected. If a class is part of multiple modules, the same naming policy must be
 *         applied to both modules, otherwise an error will be thrown.
 *         FIXME: Need test setting name
 *         FIXME: Need check for class being part of two modules with different policies
 *     </li>
 *     <li>
 *         If applied to {@link RealmModule#fieldNamingPolicy} all persistable fields in all classes
 *         part of this module will be affected.
 *     </li>
 *
 *      <li>
 *          If applied to {@link RealmClass#fieldNamingPolicy} only fields in that class will be
 *          affected. This will override any policy specified on a module.
 *      </li>
 * </ul>
 * An example of this:
 * <pre>
 * {@code
 * \@RealmClass(name = "persons", fieldNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
 * public class Person extends RealmObject { // is converted to "person"
 *     public string firstName; // Is converted to "first_name"
 * }
 * }
 * </pre>
 * <p>
 * Changing the internal name has the following implications:
 * <ul>
 *      <li>
 *          Queries on {@code DynamicRealm} must use the internal name. Queries on normal {@code Realm}
 *          instances should continue to use the name as it is defined in the Java class.
 *      </li>
 *      <li>
 *          Migrations must use the internal name when creating classes and fields.
 *      </li>
 *      <li>
 *          Interacting with the Realm schema using {@code RealmSchema} or {@code RealmObjectSchema} should
 *          be done using the internal name.
 *      </li>
 * </ul>
 * <p>
 * Note, that changing the internal name does not effect importing data from JSON. The JSON data
 * must still follow the names as defined in the Realm Java class.
 *
 * @see RealmModule
 * @see RealmClass
 * @see RealmField
 */
public enum RealmNamingPolicy {

    /**
     * No policy is applied. This policy will not override any policy set on a parent element, e.g.
     * if set in {@link RealmClass#fieldNamingPolicy}, the module policy will still apply to field
     * names.
     *
     * This policy is the default one.
     */
    NO_POLICY,

    /**
     * The name in the Java model class is used as is.
     */
    IDENTITY,

    /**
     * The name in the Java model class is converted to all lower case, and each word is separated
     * by {@code _}.
     * TODO Add examples
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
     * TODO Add examples
     */
    LOWER_CASE_WITH_DASHES,

    /**
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     * TODO Add examples
     */
     */
    CAMEL_CASE,

    /**
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     * TODO Add examples
     */
    PASCAL_CASE
}
