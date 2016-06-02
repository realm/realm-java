- Feature Name: Inverse relationship syntax
- Start Date: 2016-05-30
- RFC PR: 
- Version: 5

# Summary

It should be possible to define and query backlinks in both `Realm` and 
`DynamicRealm`. As a `DynamicRealm` can work without _any_ Realm model 
classes, it should be possible to query backlinks without them being defined there.

# Motivation

Backlinks are special in Realm. They are not defined explicitly in the schema,
but are automatically created when defining Links and LinkLists (forward links).

This creates some conflicts with the current query system as it rely on all 
fields being defined by the schema. This is called *named backlinks*.

This proposal introduces a new syntax for queries so it is possible to query
across a backlink even though it hasn't been defined by a model class. This is
called *unnamed backlinks*.

Realm Objective C/Swift shipped with only support for named backlinks. This was mostly
prompted by restrictions in NSPredicate, which do not have a syntax for unnamed backlinks.

Realm Java does not have this constraint and at the same time the Java Migration/Dynamic API
is more full-fledged and expected to have the same feature set as the static API due to how the
query api works.

Adding support for the unnamed variant now will also most likely save us time documenting the
difference as well as reduce the amount of support we have to do.

Any design should ideally solve all these constraints:

- Should support a list of backlink objects.

- Should support a single backlink object, enforcing the constraint that only 1 object
  can point to it. This is not supported by Core, but it is a natural extension.

- Should be usable from the Dynamic API.

- Should either support polymorphic objects or not prevent them from being 
  implemented.

- Ideally it should work for both the pure text-based variant of the query language in the
  Object Store as well as the current mixed style found in Realm Java.
  

# Detailed design

**Example model classes:**

```
public class Person extends RealmObject {
	private String name
	private Dog favoriteDog;
	private RealmList<Dog> dogs;
}

public class Dog extends RealmObject {
	// Named backlinks
	@LinkingObjects("favoriteDog")
	private RealmResults<Person> favoriteOwner;
}
```

**Introduce the following query extension**

- Use `linkingObjects(x.z).y` to search field Y on type X where X is a backlink object through
  field Z. This is a supplement to defining the named backlinks, which will continue to work as normal
  link queries: `favoriteOwner.name`
- No changes to the migration API.
- No changes to the schema definition.

Example:

```
// Named backlink queries
RealmResults<Dog> dogs = realm.where(Dog.class).equalTo("favoriteOwner.name", "John").findAll();

// Unnamed backlink queries
RealmResults<Dog> dogs = realm.where(Dog.class).equalTo("linkingObjects(Person.dogs).name", "John").findAll();
RealmResults<DynamicRealmObjcet> dogs = dynamicRealm.where("Dog").equalTo("linkingObjects(Person.dogs).name", "John").findAll();

// Example in a purely text based variant
"ANY linkingObjects(Person.dogs).name = 'John'"
```

**Implementation**
Implementation is straight forward as we have all schema information available 
when parsing queries. So simple String manipulation will make it possible to 
split a field name into a name and a type part.

Validating that the type is an actual backlink with the given field does require 
an `O(n)` lookup where `m: number of fields in Person`. Since number of fields 
are generally low, this should not be a problem in practise.

**Advantages**
- Using "()" is not allowed in field names in any of our supported
  languages as far as I know. So parsing the above expressions should be 
  possible with no ambiguity.

- Using a method invocation syntax make it descriptive what is happening instead of relying
  on people understanding some unfamiliar syntax.

- Using a method invocation syntax make it easy to add more functionality to the query
  language.

- Will feel like a natural way of doing it if coming from NSPredicate.

**Disadvantages**

- Unknown syntax, with no help from the type system. The concept of unnamed backlinks is not used
  nor supported in neither NSPredicate, LINQ or SQL. It will be a "power feature", and might make
  the language feel complicated and hard to use.

- Moves the Java query language further away from being type safe.

- Nesting text-based method calls inside other Java method calls feels and reads a bit strange:
  `equalTo("linkingObjects(foo.bar).baz", "42")`.

- Will introduce two ways of querying backlinks.


## Alternative annotation name suggestions

The name of the annotation should reflect how we otherwise document and talk about the feature.

Possible names for the annotation so far:

- `@Backlink` (Internal name)
- `@InverseRelationship` (Name used on Cocoa/CoreData docs)
- `@LinkingObjects` (API name used by Cocoa)


## Alternative syntax suggestions

As an alternative to the above, I have listed a some of variants that were 
considered:

```
1) Use [] as seperator

- "name[Person.dogs] = 'John'"

2) Type in front

- "[Person.dogs]name = 'John'"

3) Other modifiers (^ up) , (<- back)

- "^[Person.dogs].name"
- "[Person.dogs]<-.name"

4) Special grouping operator

realm.where(Dog.class)
   .backlinks("Dogs", "dogs")
      .equalTo("state", "CA")
   .backlinksEnd()
   .equalTo("name", "Fido")
   .findAll()
```

The primary argument against 1-3 is that it will introduce a completely new syntax that is unknown
to users, also the use of special chars will make it harder to google for help.

The primary argument against 4 is that we should try to avoid adding query methods only needed by
the Dynamic API.


## Possible extensions

The proposed query syntax can also be extended to support the following features:

```
// Wild cards
- "linkingObjects(Person.*).name = 'John' // search all backlink fields on Person
- "linkingObjects(*.*).name  = 'John' // Include all backlink types
- "linkingObjects(Person.dogs, Person.favoriteDog).name = 'John' // Multiple fields at the same time.

// Backlinks multiple levels back (City -> Person -> Dog)
// Find all dogs, that has a owner that lives in Copenhagen
- "linkingObjects(City.citizens.dog).name = 'Copenhagen'"
```

However this is not part of this RFC.

# Drawbacks

It is not clear that this approach is the correct way forward. I am not sure what
can be done to gain more confidence in this, expect asking for more input. Please 
see the list of advantages and disadvantages in the last section as well as the 
proposed alternatives.


# Alternatives

A number of alternatives exists:

## 1. Only named backlinks

Objective C / Swift only allow named backlinks that are defined as part
of the Realm model class.

**Advantages:**
- Minimal effort required to implement backlink queries. Current query system is 
  enough.
- Already supported by Objective C / Swift

**Disadvantages:**
- Does not work _at all_ in the dynamic API.
- User ambiguity regarding manual migrations: Some fields must be added in a 
  migration, others not needed. This is already happening with ignored fields
  though.

## 2. Backlinks as first class citizens

An extended version of #1, but require backlinks to be defined as part of the 
schema.

**Advantages:**
- Backlinks are treated just like all other fields for the purpose of migrations.
- Minimal effort required to implement backlink queries. Current query system is 
  enough.

**Disadvantages:**
- Forcing users to do work that is really not needed. Document that "computed" 
  fields do not require migrations might be enough though.
- Has to be implemented in the Object Store.
- Require changes to the manual Migration API.


# Unresolved questions

None.

# Version history

1: Initial version.
2: Backlink fields must now be enumerated. Removed "[Person]" as a shortcut for enumerating all
   fields on Person. Added suggestion that introduces a special `backlinks()/backlinksEnd()`
   grouping method.
3: Fixed typos and mistakes.
4: Annotation renamed to `@LinkingObjects`. Syntax is now`linkingObjects(x.y).z`. Added more details
   to the motivation section. Only one field is now allowed in the annotation, multiple backlink
   fields must be added or queried separately.
5: Added backlinks reaching back multiple levels as a possible extension.