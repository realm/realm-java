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

There are a Software Requirements Specification (SRS) document?
We don't have a specification to write specifications.

Mostly, we are using Github issues to specify requirements but in rare cases, we use our internal wiki (a private Github repo) or share a Google Doc/Sheet.

Needs: 
- database to run directly inside phones, tablets and wearables.
- Simple saving. Fast queries. Save weeks of development time.
- an alternative to SQLite and Core Data

Specific Requirements and Features (Functional and Non-Functional requirements):

Funcional:

- create and remove instances of your RealmObjects (database classes)
- create relationships between RealmObjects
- make queries
- perform transactions

Non-Funcional:

- support all Android versions since API Level 9 (Android 2.3 Gingerbread & above).
- Offline-first functionality
- Fast queries
- Access the same data concurrently from multiple threads, with no crashes
- Use the same database for all apps, on any major platform.
- Secure data with transparent encryption and decryption
- Reactive architecture (Connect your UI to Realm, and data changes will appear automatically)

Current Limitations

- Realm aims to strike a balance between flexibility and performance. In order to accomplish this goal, realistic limits are imposed on various aspects of storing information in a Realm like the length of names of classes.
- Sorting and querying on String.
- Although Realm files can be accessed by multiple threads concurrently, you cannot hand over Realms, Realm objects, queries, and results between threads
- Realm files cannot be accessed by concurrent processes

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
