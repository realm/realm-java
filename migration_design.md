# Android Migrations API proposal

This document is Work-in-Progress and describes the current API proposal for the new Realm Migration API.


## Goals

- Feature compliant with Cocoa (as much as possible)
- Easy to use/read
- Avoid mentioning tables/columns, it is Core implementation details.
- Fields are Sets not lists. Order inside a class should not matter.


## Other problems

Working with migrations has showcased some other problems related to this that this spec will also
try to address. Those aspects might not be implemented as part of this, but the general direction is worth
discussing to avoid unnessary rewrites going forward.

- Apps using Realm should be able to use RealmObjects from library projects
- It should be possible to ProGuard model classes if possible.
- Getting instances of Realm are getting more complicated, and managing multiple Realms is not trivial
  when taking into account features like encrypted Realms and ReadOnly mode.
- Android code must be seperated as much as possible for compabitility with Java
- Any Android code using high level API's must be seperated. See this bug: https://github.com/realm/realm-java/issues/870


## Migration Use cases

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

**Realm libraries**

We also has to consider that people want to use Realm in libraries. For this there is two cases

- App code should not be aware that library is using Realm
- App code should be able to reuse model classes from the library

Both of these effect how to approach migrations.

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

##Modes

We have to modes of operation to choose between:

- Stepwise/Linear migrations: Specify what happens at each version.
- Direct migration: We jump directly to the newest spec, adjust as needed.


### Stepwise migration

For each version you specify the changes compared to the previous version.

Eg.

V1: Initial  version
V2: Add Class Foo with fields Foo.fieldBar and Foo.fieldBaz
V3: Remove field Foo.fieldBar
V4: Add 1 to all Foo.fieldBaz

Advantages:
- The model is very easy to understand.
- People know this model.
- You are only concerned with the diff between vA and vB
- The migration code is very localized

Disadvantages:
- All changes has to be described, also adding completely new classes incl. all their fields
- All changes between versions has to be applied, which can potentially be many
- We need to introduce mutator methods to the tables for all possible use cases.


### Direct migration

The DB is automatically converted to newest version and use will have to specify
changes to data for each version.

Eg.

V1: Initial  version
V2: Do nothing
V3: Do nothing
V4: Add 1 to all Foo.fieldBaz

Advantages:
- Adding and removing classes/fields are handled automatically.
- Less code is needed, only changes to existing data has to described

Disadvantages:
- People are less familiar with this model.
- Writing migration code from B to C will also has to consider what happened in A.
- Schema history cannot be infered from the migration code.


## Challenges

- Wording: Should we use Spec or Schema, eg. Spec is closer to class defintions and eg. JavaWriter uses that, Cocoa uses Schema that hints at SQL databases, do we want that?
- Rules for a magic "magic mode" migration needs to be simple and easy to understand. To much magic makes it hard to
  reason about behavior
- How to expose both old and new object model, since the old one only exists as core tables?
- Most migration logic should be moved to C++ in a ObjectStoreHelper.cpp or similar to avoid code duplication with Cocoa.
  Ideally this abstraction layer should have its own independent release cycle from core, ie. Bindings depend on that
  instead of directly on core.
- Running migrations on the main thread is most likely a bad idea. We should make it easy to migrate on a worker thread.

## API

This spec proposes that we take the same approach as Cococa, both due to knowledge transfer, making it simpler to merge
codebase in C++ later. But also because doing it like this will make simple migrations trivial for the user compared
to the alternative. Simplicity is King!

After discussion with Cocoa we have decided *not* to merge code in C++ at this stage, as they do not have the time and we
would gain rather get experience with that on a less complicated feature.


The spec will be split in two: 1) Changes to creating Realms 2) Migration API


1) Can be found in CreatingRealmExamples.java

2) Can be found in DirectMigrationExamples.java


**Overall thoughts**
- As typesafe as possible
- Introduce RealmSpec and RealmObjectSpec classes that are wrappers around the dynamic API. Could possible
  replace Row and TableSpec classes.
- Builder patterns to ease constructing.


**Additional annotations introduced**

1) Add new annotation @RealmField(name = "otherName"). This allow you to refactor field name without causing migrations. Also useful when dealing with multiplatform Realms.
2) Expand @RealmClass so it accepts @RealmClass(name = "otherName"). Same reason as above.



## Additional notes

ALl the major ORMs: OrmLite, GreenDao, SugarORM have no support for migrations. You have to manually execute SQL
commands to migrate.





