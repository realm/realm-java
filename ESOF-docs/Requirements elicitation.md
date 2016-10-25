# Requirements elicitation

## Index

1. [Requirements: Introduction, Scope, and Description](#requirements)
2. [Specific Requirements and Features](#features)
3. [Use Cases](#use-cases-)
3. [Domain Model](#domain)
4. [Critics and opinions](#conclusion)
5. [Contribuition](#contribuition)


## Requirements: Introduction, Scope, and Description <a name="requirements"></a>

In the last two-three decades, the software development process has changed a lot. Back then, the waterfall model was the thing. Open source changed that very much. In general, Realm isn't govern by strict procedures, and we gradually change how we do things depending on who we hire (a new hire might have brilliant idea or know a great tool) and what we did wrong in the last major release.

How does the team decides on whether to implement a new feature?
We are very much driven by what make sense for our users. Since our users are developers like us, we believe we have a pretty good idea of what makes sense. Of course, we listen - and therefore we enjoy attending meetups and conferences.

Does someone is responsible to decide which functionality will be implemented?
Decisions are done as a team. Of course, some team members have stronger opinion on some topics than others. In your area of expertise, your opinion weight more.

What is the process to analyse and accept a pull request or an issue?
There is no formal process but at least two developers who have not written any code, must approve the PR (yes, for larger PRs, we are often two working together). For very large or complex PRs, we might schedule a meeting to highlight the hot spots. We prefer not do to that as such meetings are not done in public.

There are a Software Requirements Specification (SRS) document?
We don't have a specification to write specifications.

Mostly, we are using Github issues to specify requirements but in rare cases, we use our internal wiki (a private Github repo) or share a Google Doc/Sheet.

## Specific Requirements and Features <a name="features"></a>

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

## Critics and opinions <a name="conclusion"></a>

## Contribuition <a name="contribuition"></a>
Carolina Centeio:

Inês Proença:

Hélder Antunes: 1%

Renato Abreu: 2%
