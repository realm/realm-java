![Realm](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/realmDark.jpg)
# Software Evolution

## Index

1. [Software Maintability](#maintability)
2. [Evolution Process](#evolution)
3. [Pull Request](#pull)
4. [Contribuition](#contribuition)

## Software Maintability <a name="maintability"></a>

![SIG Metrics](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/Metrics.png)

As we can see from Realm Metrics, it fails on "Write Short Units of Code" but passes on "Write Simples Units of Code". In fact, some functions are very large. Still, realm uses many interfaces to take advantage of polymorphism, and therefore, avoiding conditional logic.

Relatively to the third metric, Realm fails but looking at the refactoring candidates we conclude that is understandable since Realm
needs to have methods (getters and setters) and value definitions for each type of data (Bool, Byte, Long, String, Short), and as such
mostly candidates are example of those situations.

The metric "Keep Unit Interfaces Small" also fails, however it's pertinent to state that all units which violate this metric have to
query and manipulate the realm tables, where it's always necessary more than 4 parameters. Besides that, the methods to query a realm
table vary significantly and as such it isn't rewarding to group those parameters.

Regarding "Separate Concerns in Modules", after working closely with this project we definitely agree that Realm modules could be better
organized. Each Realm module is extensive and has a lot of functionalities which consequently affects the maintability. 

The "Couple Architecture Components Loosely" metric is successful. In fact, realm hides implementation details on many interfaces,
reducing dependencies between components.

The seven metric, in this project, isn't reliable at all because Realm architecture components are inside "realm" folder. This metric
analysed the root folder, so we cannot conclude anything relatively to the result.

Although the realm is a complex project bringing many functionality to those who use it, they can comply with the "Keeping Your Codebase Small" parameter. This has certainly been achieved by several refactoring sessions over time.

In terms of clean code, we can see that the main developers try and encourage all the contributors to use the best practices. So, the
contribution guidelines are well explained and strictly followed by all users.

The "Automate Tests" metric fails completely. Realm is a huge and complex project but regarding tests it has as lot of flaws. 

To sum up, after this analysis, we cannot judge a project maintability basely on a compliance. It is extremely important to understand
why the project fails on that specific metric because sometimes it is not worth refactoring code in order to comply to that metric.
Every project architecture is different and there is a reason why it was built that way, so to analyse the maintability of a open source
project, Better Code Hub is very useful but a good knowledge of the architecute is also necessary. In our opinion, those metrics are
beyound a doubt a excelent starting point to build a complex and maintaible software. 

[![BCH compliance](https://bettercodehub.com/edge/badge/renatoabreu11/realm-java)](https://bettercodehub.com)

## Evolution Process <a name="evolution"></a>

Since Realm is a complex project and a new feature chosen by us could require an in depth analysis of Realm's core (written in C++) and
a excelent knowledge of Realm's API architecture. That would be very hard to do, mainly because of our time to implement that feature is
limited(two weeks). Therefore, we decided that the best step to take was search for [issues](https://github.com/realm/realm-java/issues?
q=is%3Aopen+is%3Aissue+label%3AT%3AEnhancement) with Enhancement labels. By doing this, we knew that the feature was already requested
and necessary to the project, and ultimately our implementation, despite of being accepted or not, it's a good effort to contribute
something useful for the users. 

So, after an extensive analysis, the selected issue is [#3752](https://github.com/realm/realm-java/issues/3752). Realm is an application
that replaces databases like SQLite & ORMs, so it makes sense that the queries besides being much faster also must have similiar
functionalites. The LIKE query, in our perspective, is pretty important and since wildcard matches was merged to Realm Core, meaning
that we could abstain from undestanding all the core details, it allowed us to add the required support to RealmQuery, as suggested in
the issue. The feature that we decided to implement lets the user query a realm using the RealmQuery "like" function -
RealmQuery.like(String field, String value) -, which is used to match a field against a value using wildcards (* and ?).

The feature will be part of a group of conditions that RealmQuery already supports (between, contains, lessThan, etc) and as such the best way to implement this feature is to check where those supported conditions are implemented. The file identified was [RealmQuery.java](https://github.com/realm/realm-java/blob/master/realm/realm-library/src/main/java/io/realm/RealmQuery.java).

Looking at this file, we first tried to find out if there were some functions that operate on Strings that are similar to the function that was going to be implemented. The most similar function found was the function equalTo(). 
We try to see how this function was implemented, more concretely, the functions called in [TableQuery.java](https://github.com/realm/realm-java/blob/master/realm/realm-library/src/main/java/io/realm/internal/TableQuery.java). The last function called was a native function implemented in C ++, TableQuery_StringPredicate(), present in file [io_realm_internal_TableQuery.java](https://github.com/realm/realm-java/blob/master/realm/realm-library/src/main/cpp/io_realm_internal_TableQuery.cpp). 
This function is not only called for the function equalTo(), but for all querys that operate on strings. We just had to add a case to the switch there, and then call a newly deployed function in the realm core.

As requested in the issue and to test our implementation, we had to create test cases to check if our feature was working properly, so a new test was created in [RealmQueryTests.java](https://github.com/realm/realm-java/blob/master/realm/realm-library/src/androidTest/java/io/realm/RealmQueryTests.java), since all queries and conditions are tested there.

## Pull Request <a name="pull"></a>

The link to pull request is [here](https://github.com/realm/realm-java/pull/3922#pullrequestreview-13474336).

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
