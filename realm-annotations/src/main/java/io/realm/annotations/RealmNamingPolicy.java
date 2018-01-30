package io.realm.annotations;

/**
 * This enum defines the possible ways class and field names can be mapped from what is used in Java
 * to the name used internally in the Realm file.
 * <p>
 * Examples where this is useful:
 * <ul>
 *      <li>
 *          To support two model classes with the same simple name but in different packages.
 *      </li>
 *      <li>
 *          To make it easier to work with cross platform schemas as naming conventions are different.
 *      </li>
 *      <li>
 *          To use a Java class name that is longer than the 57 character limit enforced by Realm.
 *      </li>
 *      <li>
 *          To change a field name in Java without forcing app users through a migration process.
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
 * <p>
 * An example of this:
 * <pre>
 * {@code
 * \@RealmClass(name = "__person", fieldNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
 * public class Person implements RealmModel { // is converted to "__person" internally
 *     public string firstName; // Is converted to "first_name" internally
 * }
 * }
 * </pre>
 * <p>
 * Choosing an internal name that differs from the name used in the Java model classes has the
 * following implications:
 * <ul>
 *      <li>
 *          Queries on {@code DynamicRealm} must use the internal name. Queries on normal {@code Realm}
 *          instances must continue to use the name as it is defined in the Java class.
 *      </li>
 *      <li>
 *          Migrations must use the internal name when creating classes and fields.
 *      </li>
 *      <li>
 *          Schema errors reported will use the internal names.
 *      </li>
 * </ul>
 * <p>
 * When automatically converting Java variable names, each variable name is normalized by splitting
 * it into a list of words that are then joined using the rules of the target format. The following
 * heuristics are used for determining what constitutes a "word".
 * <ol>
 *     <li>
 *         Anytime a {@code _} or {@code $} is encountered.
 *         Examples are "_FirstName", "_First_Name" and "$First$Name" which all becomes "First" and "Name".
 *     </li>
 *     <li>
 *         Anytime you switch from a lower case character to an upper case character as
 *         identified by {@link Character#isUpperCase(int)} and {@link Character#isLowerCase(int)}.
 *         Example is "FirstName" which becomes "First" and "Name".
 *     </li>
 *     <li>
 *         Anytime you switch from more than one uppercase character to a lower case one. The last
 *         upper case letter is assumed to be part of the next word. This is identified by using
 *         {@link Character#isUpperCase(int)} and {@link Character#isLowerCase(int)}.
 *         Example is "FIRSTName" which becomes "FIRST" and "Name.
 *     </li>
 *     <li>
 *         Some characters like emojiis are neither uppercase nor lowercase characters, so they will
 *         be part of the current word.
 *         Examples are "my😁" and "MY😁" which are both treated as one word.
 *     </li>
 *     <li>
 *         Hungarian notation, i.e. variable names starting with lowercase "m" followed by uppercase
 *         letter is stripped and not considered part of any word.
 *         Example is "mFirstName" and "mFIRSTName" which becomes "First" and "Name.
 *     </li>
 * </ol>
 * <p>
 * Note that changing the internal name does <i>NOT</i> affect importing data from JSON. The JSON
 * data must still follow the names as defined in the Realm Java class.
 * <p>
 * When it comes to parsing JSON using standard libraries like Moshi, GSON or Jackson it is
 * important to keep in mind that these libraries define the transformation from JSON to Java
 * while setting internal Realm names define the transformation from Java to the Realm file.
 * <p>
 * This means that if you want to import data into Realm from JSON using these libraries you still
 * need to provide the annotations from both the JSON parser library and Realm.
 * <p>
 * Using Moshi, it would look something like this:
 * <pre>
 * {@code
 * public class Person extends RealmObject {
 *     \@Json(name = "first_name") // Name used in JSON input.
 *     \@RealmField(name = "first_name") // Name used internally in the Realm file.
 *     public string firstName; // name used in Java
 * }
 * }
 * </pre>
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
     * If two modules disagree on the policy and one of them is {@code NO_POLICY}, the other will
     * be chosen without an error being thrown.
     * <p>
     * This policy is the default.
     */
    NO_POLICY,

    /**
     * The name in the Java model class is used as is internally.
     */
    IDENTITY,

    /**
     * The name in the Java model class is converted to camelCase, i.e. all words are joined
     * together with the first letter in the first word lower cased, and the first letter of
     * all subsequent words upper cased. This is the standard naming schema in Java, Kotlin, Swift
     * and JavaScript.
     * <p>
     * Examples: "firstName", "FirstName", "mFirstName", "FIRST_NAME", "First$Name" all becomes
     * "firstName".
     */
    CAMEL_CASE,

    /**
     * The name in the Java model class is converted to PascalCase, i.e. all words are joined
     * together with the first letter of all words upper cased. This is the default naming scheme
     * in .NET.
     * <p>
     * Examples: "firstName", "FirstName", "mFirstName", "FIRST_NAME", "First$Name" all becomes
     * "FirstName".
     */
    PASCAL_CASE,

    /**
     * The name in the Java model class is converted lowercase with each word separated by {@code _}.
     * This is the default naming scheme in C++.
     * <p>
     * Examples: "firstName", "FirstName", "mFirstName", "FIRST_NAME", "First$Name" all becomes
     * "first_name".
     */
    LOWER_CASE_WITH_UNDERSCORES
}
