/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal.util;

/**
 * Container to ease passing around a tuple of two objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 */
public class Pair<F, S> {
    /**
     * Implementation notes:
     *
     * Copy from the Android framework to avoid the dependency on Android classes + slight adjustment
     * to support older versions of Android.
     *
     * Original source: https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/util/Pair.java
     */
    public F first;
    public S second;

    /**
     * Constructor for a Pair.
     *
     * @param first the first object in the Pair.
     * @param second the second object in the pair.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Pair} to which this one is to be checked for equality.
     * @return true if the underlying objects of the Pair are both considered
     *         equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return equals(p.first, first) && (equals(p.second, second));
    }

    private boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects.
     *
     * @return a hashcode of the Pair.
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    public String toString() {
        return "Pair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param a the first object in the Pair.
     * @param b the second object in the pair.
     * @return a Pair that is templatized with the types of a and b.
     */
    public static <A, B> Pair <A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }
}