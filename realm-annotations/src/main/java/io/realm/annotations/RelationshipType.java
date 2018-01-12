/*
 * Copyright 2018 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.annotations;

/**
 * Enum describing what kind of relationship exists between two Realm model classes A and B. A
 * relationship can either be {@link #WEAK} or {@link #STRONG}.
 *
 * The relationship is determined pr. field, i.e. it is possible for an object A to have
 * both a Weak and Strong relationship with object B if A contains multiple fields that
 * reference B.
 */
public enum RelationshipType {
    /**
     * Indicates that the relationship between object A and B is strong. This means that B
     * will be deleted automatically if A no longer holds a reference to B or if A is deleted itself.
     * This is also known as cascading deletes.
     * <p>
     * It is possible for multiple different classes to have a strong reference to the same object.
     * In that case the object is only deleted automatically when the last strong reference is removed
     * or all parent objects are deleted.
     * <p>
     * It is possible to create child objects (B) with no parents (A), these will only be deleted if
     * an parent actually existed at some point, e.g. If A references B, then it is possible to
     * create object B first and then later add the reference from A to B. B will only be
     * automatically deleted when A either explicitly removes its reference or is deleted itself.
     * <p>
     * <b>Warning:</b><br>
     * Some special corner cases exists for cyclic object graphs that are strongly connected:
     * <ul>
     *     <li>
     *         {@code ( A -> B <-> C )}:<br>
     *         Object A contains a reference to B and C that in turn reference each other.
     *         If the reference between A and B is removed, then B and C are an "disconnected island".
     *         In this case neither B nor C will be deleted automatically and this must be done
     *         manually.
     *     </li>
     *     <li>
     *         {@code ( A <-> B )}:<br>
     *         Object A and Object B strongly references each other in isolation.
     *         If either A or B breaks the reference so the graph becomes {@code (A -> B)} or
     *         {@code (B -> A)}, then both A and B is deleted. It is possible to avoid this
     *         behaviour by adding an outside strong reference, so the graph becomes:
     *         {@code ( C - > A <-> B )}.
     *     </li>
     * </ul>
     */
     STRONG,

    /**
     * Indicates that the relationship between object A and B is weak. This means that A and B
     * can exist independently regardless of what happens to the other.
     * <p>
     * This is the default relationship type between objects.
     * <p>
     * Fields marked with {@link LinkingObjects} is always considered to have a weak relationship.
     * </p>
     */
    WEAK,
}
