package io.realm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation will generate an extra subset of fields inside the perspective Fields class
 * to allow the annotated Object to be queried by its fields.
 * <p>
 *
 * Example:
 * * <pre>
 * {@code
 *   public class Dog extends RealmObject {
 *       private int age;
 *   }
 *
 *   public class Person extends RealmObject {
 *      @QueryFieldBy(fields = "age")
 *      private Dog dog;
 *   }
 *
 *   // DOG_AGE field will now be generated in PersonFields class.
 *   public final class PersonFields {
 *       public static final String DOG_AGE = "dog.age";
 *   }
 *
 *   // Query all people who have a dog which is 4 years old.
 *   realm.where(Person.class).equalTo(PersonFields.DOG_AGE, 4);
 * }
 * </pre>
 *
 * <p>
 * Primitive types are not allowed to be annotated with {@link QueryFieldBy}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface QueryFieldBy {

    String[] fields();
}
