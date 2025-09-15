- Feature Name: Primitive List API
- Start Date: 2017-02-07
- RFC PR:
- Version: 1

# Summary

Currently Realm only supports lists of Realm Objects, but we should also support lists
of primitive types like `int`, `String` and `boolean`. This document describes how such
an API might look like.

# Motivation

With Core working on support for primitive lists, we need to consider how the public API is going to look like as it
might influence some of the design choices for the underlying implementation. This RFC is mostly to explore the
possible options for the public API and elicit feedback.

# Variant 1: Relax generic type arguments

In this variant, we relax the type requirement on RealmList, RealmQuery and RealmResults to `Object`.

1) Relax type requirement for `RealmCollection`, `OrderedRealmCollection`, `RealmResults`, `RealmList` and `RealmQuery`

```
// Current signature
public class RealmList<E extends RealmModel> extends AbstractList<E> implements OrderedRealmCollection<E>
public class RealmQuery<E extends RealmModel> {
public class RealmResults<E extends RealmModel> extends AbstractList<E> implements OrderedRealmCollection<E>
public interface OrderedRealmCollection<E extends RealmModel> extends List<E>, RealmCollection<E>
public interface RealmCollection<E extends RealmModel> extends Collection<E>

// New signature
public class RealmList<E> extends AbstractList<E> implements OrderedRealmCollection<E>
public class RealmQuery<E>
public class RealmResults<E> extends AbstractList<E> implements OrderedRealmCollection<E>
public interface OrderedRealmCollection<E> extends List<E>, RealmCollection<E>
public interface RealmCollection<E> extends Collection<E>
```

2) New optional methods in `RealmCollection`

```
average();
max();
min();
maxDate();
minDate();
sum()
```

3) New optional methods in `OrderedRealmCollection`

```
sort();
sort(Sort);
```


4a) Add new query methods for querying primitive arrays, i.e only accept value, not column name.

```
// New RealmQuery methods (leaving out the column name)
RealmQuery<Integer> query = obj.getList().where();

beginsWith(String)
beginsWith(String, Case)
between(Date, Date)
between(long, long)
between(float, float)
between(double, double)
contains(String)
contains(String, Case)
distinct()
endsWith(String)
endsWith(String, Case)
equalTo(Boolean)
equalTo(Byte)
equalTo(byte[])
equalTo(Date)
equalTo(Double)
equalTo(Float)
equalTo(Integer)
equalTo(Long)
equalTo(Short)
equalTo(String)
equalTo(String, Case)
findAllSorted()
findAllSortedAsync()
greaterThan(Date)
greaterThan(double)
greaterThan(float)
greaterThan(long)
greaterThanOrEqualTo(Date)
greaterThanOrEqualTo(double)
greaterThanOrEqualTo(float)
greaterThanOrEqualTo(long)
in(Boolean[])
in(Byte[])
in(Date[])
in(Double[])
in(Float[])
in(Integer[])
in(Long[])
in(Short[])
in(String[])
in(String[], Case)
isEmpty()
isNotEmpty()
isNotNull()
isNull()
lessThan(Date)
lessThan(double)
lessThan(float)
lessThan(long)
lessThanOrEqualTo(Date)
lessThanOrEqualTo(double)
lessThanOrEqualTo(float)
lessThanOrEqualTo(long)
like(String)
max()
maximumDate()
min()
minimumDate()
notEqualTo(Boolean)
notEqualTo(Byte)
notEqualTo(byte[])
notEqualTo(Date)
notEqualTo(Double)
notEqualTo(Float)
notEqualTo(Integer)
notEqualTo(Long)
notEqualTo(Short)
notEqualTo(String)
notEqualTo(String, Case)
sum()
```

Benefit: Intent expressed in method params. All queries use the same class Downside: Ton of new methods. More chances for
people to use the "wrong" method. Today people still have to remember not to call e.g. `sum` on a String field though.


4b) Add support for a special keyword for column names. NSPredicate apparently uses `SELF`, `$` could also work.

Benefit: No need to add a lot of new methods. Easier to upgrade from existing workaround where people reference
a field inside a `RealmInt` type (search-replace-done). Downside: Would not work if people used the restricted name for
an actual field name. Syntax would also be a bit awkward and "hidden".


# Design 2: PrimitiveRealmList<Object>

In this variant, we add 3 new classes `PrimitiveRealmList`, `PrimitiveRealmQuery`, `PrimitiveRealmResults`

1a) Add new classes

```
// New classes for supporting primitive arrays
public class PrimitiveRealmList<Object> extends AbstractList<E> implements OrderedRealmCollection<E>
public class PrimitiveRealmQuery<Object>
public class RealmResults<Object> implements OrderedRealmCollection<E>

// We need to relax the Collection Interface methods unless we want to create a completely new Collection hierarchy for
// primitive types.
public interface OrderedRealmCollection<E> extends List<E>, RealmCollection<E>
public interface RealmCollection<E> extends Collection<E>
```

1b) Add new classes with `Primitive*` as an abstract super class:

Precedence: SparseIntArray, SparseBooleanArray, etc.

```
protected abstract class PrimitiveRealmQuery<Object>
public class PrimitiveIntegerRealmQuery extends PrimitiveRealmQuery<Integer>
public class PrimitiveFloatRealmQuery extends PrimitiveRealmQuery<Float>
public class PrimitiveDoubleRealmQuery extends PrimitiveRealmQuery<Double>
public class PrimitiveStringRealmQuery extends PrimitiveRealmQuery<String>
public class PrimitiveBooleanRealmQuery extends PrimitiveRealmQuery<Boolean>

protected abstract class PrimitiveRealmList<Object> extends OrderedRealmCollection<Object>
public class PrimitiveIntegerRealmList extends PrimitiveRealmList<Integer>
public class PrimitiveFloatRealmList extends PrimitiveRealmList<Float>
public class PrimitiveDoubleRealmList extends PrimitiveRealmList<Double>
public class PrimitiveStringRealmList extends PrimitiveRealmList<String>
public class PrimitiveBooleanRealmList extends PrimitiveRealmList<Boolean>

protected abstract class PrimitiveRealmResults<Object> extends OrderedRealmCollection<Object>
public class PrimitiveIntegerRealmResults extends PrimitiveRealmResults<Integer>
public class PrimitiveFloatRealmResults extends PrimitiveRealmResults<Float>
public class PrimitiveDoubleRealmResults extends PrimitiveRealmResults<Double>
public class PrimitiveStringRealmResults extends PrimitiveRealmResults<String>
public class PrimitiveBooleanRealmResults extends PrimitiveRealmResults<Boolean>
```

2) We need the same optional methods in `RealmCollection` and `OrderedRealmCollection` unless we we create an entirely
   new collection hierarchy.

3) `PrimitiveRealmQuery` will only contain the features supported by primitive arrays. If using 1b, we can even ensure
   that all methods are useful

```
beginsWith // Only String
between // Only numbers, date
contains // Only String
distinct
endsWith // Only String
equalTo
findAllSorted
findAllSortedAsync
greaterThan // Only numbers, date
greaterThanOrEqualTo // only numbers, date
in
isEmpty
isNotEmpty
isNotNull
isNull
lessThan // only numbers, date
lessThanOrEqualTo // only numbers, date
like // only String
max // only numbers, date
maximumDate // only numbers, date
min // only numbers, date
minimumDate // only numbers, date
notEqualTo
sum // only numbers // only numbers
````

# Discussion

## Design 1: Relax type requirements

**Advantages**
* No need to add new types.
* RealmQuery/RealmResults already contain methods that doesn't work in certain circumstances.
* Schemas can already be restrict using modules, making `<E extends RealmModel>` inaccurate anyway.
* RealmCollection will have to contain optional methods no matter what, so adding them to RealmQuery doesn't break
  that pattern.
* Would allow us to remove the requirement that people implements `RealmModel`, they could just use the `@RealmClass`
  annotation.

**Disadvantages**
* API gets a lot less type safe (IntelliJ plugin might help here).
* `RealmQuery` will suddenly have a ton of new methods that will only work for primitive arrays (lint check?)


## Design 2: New collection classes:

**Advantages**
* Intent is more clear in the type system.
* `RealmQuery` is not flooded with a lot of new methods.
* No changes to existing code. Primitive arrays will be a complete new addon.
* With 1b) we statically check everything using the type system on compile time.

**Disadvantages**
* Explosion in classes in the Realm API. Maintenance and test will be problematic.
* It is not clear how to handle primitive arrays in the current Collection hierarchy. It seems a bit pointless to allow
  optional methods for primitive lists in the interface if we go to great lengths in the actual class to make it type
  safe.
* Type safety is not guaranteed anyway if we allow `<Object>`` as generic.


# Work required

- [ ] Extend JSON API to support primitive lists.
- [ ] Extend RealmObjectSchema to support primitive lists
- [ ] Extend DynamicRealmObject to support primitive lists (Design required).
- [ ] Annotation processor must be updated
- [ ] Add support for primitive arrays using RealmTransformer (maybe not part of this RFC?)
- [ ] Implement new classes (depends on chosen solution).
- [ ] Refactor current Collection API's + unit tests.

# Unresolved questions

1. We need more feedback on the two proposals.

2. Unclear how we are going to support lists-of-lists, e.g. what kind of queries do you want to run on a `int[][]`
  (if any) ?

3. Should we support `int[]` using bytecode transformation? Basically replace `int[]` and friends with `RealmList<Integer/.../...>` ?


# Version history

1: Initial version.
