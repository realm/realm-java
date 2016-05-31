- Feature Name: Inverse relationship syntax
- Start Date: 2016-05-30
- RFC PR: 
- Version: 3

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

Any design should ideally solve all these constraints:

- Should support a list of backlinks.

- Should support a single backlink, enforcing the constraint that only 1 object 
  can point to it. This is not supported by Core, but it is a natural extension.

- Should support backlinks from multiple fields.

- Should be usable from the Dynamic API.

- Should either support polymorphic objects or not prevent them from being 
  implemented.

- Ideally it should work for both a pure text-based variant as well as the 
  current mixed style found in Realm Java.
  

# Detailed design

**Example model classes:**

```
public class Person extends RealmObject {
	private Dog favoriteDog;
	private RealmList<Dog> dogs;
}

public class Dog extends RealmObject {
	private String name

	// Named backlinks below
	@Backlink({"favoriteDog"})
	private RealmResults<Person> favoriteOwner;

	@Backlink({"favoriteDog", "dogs"})
	private RealmResults<Person> allOwners;
}
```

**Introduce the following query extension**

- Use `"y[x.z]"` to search field Y on type X where X is a backlink through 
  field Z.
- Searching multiple fields should be done in individual query methods:
  `equalTo("y[x.z]", "foo").equalTo("y[x.z1]", "foo")`
- This is a supplement to defining the named backlinks.
- No changes to the migration API.
- No changes to the schema definition.

Example:

```
// Named backlink queries
realm.where(Dog.class).equalTo("allOwners.name", "John").findAll();
realm.where(Dog.class).equalTo("favoriteOwner.name", "John").findAll();

// Unnamed backlink queries
realm.where(Dog.class).equalTo("name[Person.favoriteDog]", "John").findAll();
realm.where(Dog.class)
  .equalTo("name[Person.favoriteDog]", "John")
  .equalTo("name[Person.dogs]", "John")
  .findAll();

// Example in a purely text based variant
"name[Person.favoriteDog] = 'John'"
```

**Implementation**
Implementation is straight forward as we have all schema information available 
when parsing queries. So simple String manipulation will make it possible to 
split a field name into a name and a type part.

Validating that the type is an actual backlink with the given field does require 
an `O(n)` lookup where `m: number of fields in Person`. Since number of fields 
are generally low, this shouldn't be a problem in practise.

**Advantages**
- Using "[]" is not an allowed field characters in any of our supported 
  languages as far as I know. So parsing the above expressions should be 
  possible with no ambiguity.

- The default case is easy to describe.

- Using [] as a modifier already has precedent in NSPredicate.


**Disadvantages**

- Unknown syntax. This concept is not used nor supported in neither NSPredicate, 
  LINQ or SQL. It will be a "power feature", and might make the language feel 
  complicated and hard to use.

- Moves the Java query language further away from being type safe.

- Will introduce two ways of querying backlinks.

- Without wildcards for unnamed backlinks queries risk getting extremely verbose.


## Alternative syntax suggestions

As an alternative to the above, I have listed a some of variants that were 
considered:

```
1) Type in front

- "[Person]name = 'John'"
- "[Person.favoriteDog]name = 'John'"


2) All fields explicit. Group fields using ()

- "name[Person.(favoriteDog, dogs)]  = 'John'"


3) Other modifiers (^ up) , (<- back)

- "^Person(favoriteDog, dogs).name"
- "Person(favoriteDog, dogs)<-.name"


4) Special grouping operator

```
realm.where(Dog.class)
   .backlinks("Dogs", "dogs")
      .equalTo("state", "CA")
   .backlinksEnd()
   .equalTo("name", "Fido")
   .findAll()
```


```

## Possible extensions

The proposed query syntax can also be extended to support the following features:

```
// Wild cards
- "name[*] = 'John' " // All backlink types
- "name[Person.*] = 'John' " // All fields are included from the backlink type.
- "name[* extends Person] = 'John' " // Polymorphic backlinks
- *[*] = 'John' // Any string based field on all types of backlinks

However this is not part of this RFC.

```


# Drawbacks

It is not clear that his approach is the correct way forward. I am not sure what 
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

1: Initial version
2: Backlink fields must now be enumerated. Removed "[Person]" as a shortcut
   for enumerating all fields on Person. Added suggestion that introduces
   a special `backlinks()/backlinksEnd()` grouping method.
3: Fixed Typos and mistakes.