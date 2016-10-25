# Requirements elicitation

## Index

1. [Requirements: Introduction, Scope, and Description](#requirements)
2. [Specific Requirements and Features](#features)
3. [Use Cases](#cases)
3. [Domain Model](#domain)
4. [Critics and opinions](#conclusion)
5. [Contribuition](#contribuition)


## Requirements: Introduction, Scope, and Description <a name="requirements"></a>

Realm, as open source project, isn't govern by strict procedures, and gradually change how do things depending on who they hire (a new hire might have brilliant idea or know a great tool) or what they did wrong in the last major release.

The team have a good notion of what is needed. However, the team is very receptive to new ideas from general community.

Decisions and prioritys are discussed in team. Altough, some team members have stronger opinion on some topics that are their area of expertise.

For general pull-request, at least two developers (often working together) that have not written any code, must approve the pull-request. For very large or complex pull-request, they might a meeting to highlight the hot spots.

## Specific Requirements and Features <a name="features"></a>

An alternative to SQLite and Core Data was needed so that databases could run directly inside phones, tablets and wearables. Also, this alternative must be capable of a simple saving and fast queries, to save weeks of development time. In order to specify the requirements, the main communication is through GitHub issues, but the internal wiki or Google Doc/Sheet can also be used occasionally.

>  We don't have a specification to write specifications. Mostly, we are using Github issues to specify requirements but in rare cases, we use our internal wiki (a private Github repo) or share a Google Doc/Sheet.

As such, Realm functional requirements are:

- creation and removal database classes and their instances
- classes should be able to have relationships with one another
- perform database queries and access the results
- insure the implementation and integrity of transactions

However, there are some factors that constrict the solution used by Realm, such as:

- all Android versions since API Level 9 (Android 2.3 Gingerbread) should be supported.
- functionality must be offline-first
- queries need to be fast
- access the same data concurrently from multiple threads should be possible and originate no crashes
- on any major platform, the same database must be used for all apps
- data should be secure with transparent encryption and decryption
- data changes must appear automatically when UI connects to Realm

Furthermore, at the moment, the solution implemented has limitations. In order to strike a balance between flexibility and performance, realistic limits are imposed on various aspects of information storage in a Realm like the length of names of classes. In addition, sorting and case insensitive string matches in queries are only supported for some character sets and the case insensitive flag used in string comparison only works on characters from the English locale. Thirdly, although Realm files can be accessed by multiple threads concurrently, handing over Realms, Realm objects, queries, and results between threads is not possible. Finally, Realm files cannot be accessed by concurrent processes.


## Use Cases <a name="cases"></a>

## Domain Model <a name="domain"></a>

This project can be conceptually represented by six classes and the relations between them, as shown in the diagram bellow.

![Domain model](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/domain%20model.png)

This representation can bridge the gap between Use-Case Model and Software Design Model. Therefore, allowing everyone on the team to be on the same page regarding the structure of Realm.

## Critics and opinions <a name="conclusion"></a>

## Contribuition <a name="contribuition"></a>
Carolina Centeio:

Inês Proença:

Hélder Antunes: 1%

Renato Abreu: 2%
