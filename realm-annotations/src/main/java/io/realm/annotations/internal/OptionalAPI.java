package io.realm.annotations.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark an API as optional with one or more class {@link #dependencies()}. The bytecode
 * transformer will decide on build time to remove the corresponding API if it doesn't fulfill the dependencies.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface OptionalAPI {
    String[] dependencies();
}
