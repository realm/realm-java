# Android Migrations API proposal

This document is Work-in-Progress and describes the current API proposal for the new Realm Migration API.


## Goals

- Feature compliant with Cocoa (as much as possible)
- Easy to use/read
- Avoid mentioning tables/columns, it is Core implementation details.
- Fields are Sets not lists. Order inside a class should not matter.


**Modes**

- Stepwise/Linear migrations: Specify what happens.
- "Magic mode": Try to migrate as much as possible without user involvement.


## Use cases

This is a list of use cases that should explore the breath of the problem:

**Some notation shorthand**
-> Migration

\+ Add class

\- Remove class

{x,y,z} Field names and order

{x:int,y:str,z:bool} Field names, order and type

{int, str, bool} Field names, order and type

( ... ) Describe semantics


**Class changes**

Description                             | Shorthand
--------------------------------------  | -------------------------
Class deleted                           | A-
Class added                             | A+
Class renamed                           | A -> B
Multiple classes with diff. types       | A{int,str} -> B{int,str} and C{bool} -> D{bool}
Mult. classes with same type            | A{int} -> B{int} and C{int} -> D{int}


**Field changes**

Description                             | Shorthand
--------------------------------------  | -------------------------
Rename                                  | {x} -> {y}
Delete                                  | {x} -> {}
Type switch (many variants)             | {x:int} -> {x:str}
Merge (many variants and operators)     | {x:int, y:str} -> {z:x+y}
Split (many variants and operators)     | {x:int} -> {y:int, z:int} (x -> y = x/2, z = x/3, x-)
Rename mult., diff. types               | {x:int, y:str} -> {z:int,w:str}
Rename mult., same. types               | {x:int, y:int} -> {z:int,w:int}
Move to other class                     | A{x,y}, B{z} -> A{y}, B{x,z}
Delete + rename to same name            | {x,y} -> {x} (x-, y->x)


**Mixing them**

BOOM! Combinatorial explosion!


## Types of migrations

### Object migrations

- **Create**: Create a new object
- **Delete**: Delete existing object
- **Rename**: Rename an object

### Field migrations

- **Add**: Add a new field
- **Delete**: Delete a existing field
- **Rename**: Rename a existing field
- **Change type**: Change the type of an existing field, ie. from int to string
- **Merge**: Merge two fields by some rules
- **Move**: Move to another class (eg. when we introduce subclasses)

### Other features

- **Enumerate existing objects together with new Schema**: Very powerful feature allowing almost any kind of data manipulation
- **Able to create new objects during migration**


## Challenges

- Wording: Should we use Spec or Schema, eg. Spec is closer to class defintions and eg. JavaWriter uses that, Cocoa uses Schema that hints at SQL databases, do we want that?
- Rules for a magic "magic mode" migration needs to be simple and easy to understand. To much magic makes it hard to
  reason about behavior
- How to expose both old and new object model, since the old one no longer exists except as core tables?
- How much should Magic mode handle?
- Should "magic mode" and custom migration be mixed, gut feeling says not. It will be too hard for user to reason about
  their code.
- Most migration logic should be moved to C++ in a ObjectStoreHelper.cpp or similar to avoid code duplication with Cocoa.
  Ideally this abstraction layer should have its own independent release cycle from core, ie. Bindings depend on that
  instead of directly on core.

## Magic mode migration

Magic mode automatically handles the following cases

*Easy to detect*

- New Object
- Delete object
- New field
- Delete field

*Harder*

- Rename object: Easy for one object, else compare fields, else fail?
- Rename field: Easy if different types/1 field, impossible if multiple fields of same type changes

Any renaming will break magic mode. Can we even detect that?
Perhaps magic mode has to be really explicit?

## API

**Overall thoughts**
- As typesafe as possible
- Introduce RealmSpec and RealmObjectSpec classes that are wrappers around the dynamic API. Could possible
  replace Row and TableSpec classes.
- Builder patterns to ease constructing.


Annotations

1) Add new annotation @RealmField(name = "otherName"). This allow you to refactor field name without causing migrations. Also useful when dealing with multiplatform Realms.

2) Expand @RealmClass so it accepts @RealmClass(name = "otherName"). Same reason as above.


Realm method:

Realm.migrateRealm(getContext(), new MyMigration());


Interface for migrations

```
public class MyMigration implements RealmMigration {

	// Only one method called migrate. Return value is new schema version
	public int migrate(RealmSpec oldRealm, int oldVersion) {

		// Migrate to version 1
		if (oldVersion < 1) {
            // Do stuff
		}

		return 1;
	}
}
```

API examples are in the io.realm.examples.MigrationAPIExamples

## Additional notes

ALl the major ORMs: OrmLite, GreenDao, SugarORM have no support for migrations. You have to manually execute SQL
commands to migrate.





