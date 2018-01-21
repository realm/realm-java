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
 *          To use a Java class name that is longer than the 57 character limit enforced by Realm.
 *      </li>
 * </ul>
 *
 * Depending on where the policy is applied, it will have slightly different semantics:
 * <ul>
 *     <li>
 *         If applied to {@link RealmModule#classNamingPolicy} all classes part of that module
 *         will be affected. If a class is part of multiple modules, the same naming policy must be
 *         applied to both modules, otherwise an error will be thrown.
 *     </li>
 *     <li>
 *         If applied to {@link RealmModule#fieldNamingPolicy} all persistable fields in all classes
 *         part of this module will be affected.
 *     </li>
 *
 *      <li>
 *          If applied to {@link RealmClass#fieldNamingPolicy} all fields in that class will be
 *          affected. This will override any field naming policy specified on a module.
 *      </li>
 * </ul>
 *
 * An example of this:
 * <pre>
 * {@code
 * \@RealmClass(name = "__person", fieldNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
 * public class Person extends RealmObject { // is converted to "__person" internally
 *     public string firstName; // Is converted to "first_name" internally
 * }
 * }
 * </pre>
 * <p>
 *
 * Choosing an internal name that differs from the name used in the Java model classes has the
 * following implications:
 * <ul>
 *      <li>
 *          Queries on {@code DynamicRealm} must use the internal name. Queries on normal {@code Realm}
 *          instances should continue to use the name as it is defined in the Java class.
 *      </li>
 *      <li>
 *          Migrations must use the internal name when creating classes and fields.
 *      </li>
 *      <li>
 *          Schema errors reported will use the internal names.
 *      </li>
 * </ul>
 * <p>
 * Note, that changing the internal name does <i>NOT</i> effect importing data from JSON. The JSON data
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
     * <p>
     * If two modules disagree on the policy and one of them is {@code NO_POLICY}, the other one
     * will be chosen without an error being thrown.
     * <p>
     * This policy is the default one.
     */
    NO_POLICY,

    /**
     * The name in the Java model class is used as is internally.
     */
    IDENTITY,

    /**
     * The name in the Java model class is split into words using upper case letters and {@code _}.
     * Each word is separated by {@code _} and then lower cased.
     * <p>
     * Examples:
     * <ul>
     *     <li>
     *         "firstName" and "FirstName" becomes "first_name".
     *     </li>
     *     <li>
     *         "mFirstName" becomes "m_first_name"
     *     </li>
     *     <li>
     *         "FIRST_NAME" becomes "f_i_r_s_t_n_a_m_e"
     *     </li>
     * </ul>
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    LOWER_CASE_WITH_UNDERSCORES,

    /**
     * The name in the Java model class is converted to all lower case, and each word is separated
     * by {@code -}.
     * <p>
     * Examples:
     * <ul>
     *     <li>
     *         "firstName" becomes "first_name"
     *     </li>
     *     <li>
     *         "FIRSTName" becomes "
     *     </li>
     * </ul>
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    LOWER_CASE_WITH_DASHES,

    /**
     * The name in the Java model class is converted to camelCase, treating {@code _} and upper
     * case letter as marking the beginning of a new word.
     * <p>
     * Examples:
     * <ul>
     *     <li>
     *         "firstName" becomes "first_name"
     *     </li>
     *     <li>
     *         "FIRSTName" becomes "
     *     </li>
     * </ul>
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    CAMEL_CASE,

    /**
     * The name in the Java model class is converted to PascalCase, treating {@code _} and upper
     * case letter as marking the beginning of a new word.
     * <p>
     * Examples:
     * <ul>
     *     <li>
     *         "firstName" becomes "first_name"
     *     </li>
     *     <li>
     *         "FIRSTName" becomes "
     *     </li>
     * </ul>
     *
     * Only ASCII strings are supported. The conversion on non-ASCII characters are undefined.
     */
    PASCAL_CASE
}
