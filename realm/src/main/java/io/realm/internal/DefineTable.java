package io.realm.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark the classes that serve as entity description.
 * For each such class, e.g. Xyz, the classes XyzTable, XyzView, XyzRow and
 * XyzQuery will be generated.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DefineTable {

    String row() default "";

    String table() default "";

    String view() default "";

    String query() default "";

}
