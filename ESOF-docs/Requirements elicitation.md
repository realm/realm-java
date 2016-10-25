# Requirements elicitation

## Index

1. [Requirements: Introduction, Scope, and Description](#requirements)
2. [Specific Requirements and Features](#features)
3. [Use Cases](#cases)
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

An alternative to SQLite and Core Data was needed so that databases could run directly inside phones, tablets and wearables. Also, this alternative must be capable of a simple saving and fast queries, in order to save weeks of develpment time.

As such, Realm funcional requirements are:

- creation and removal database classes and their instances
- classes should be able to have relationships with one another
- perform database queries and access the results
- insure the implementation and integrity of transactions

However, there are some factors that constrict the solution used by Realm, such as:

- all Android versions since API Level 9 (Android 2.3 Gingerbread) have to be supported.
- funcionality must be offline-first
- queries need to be fast
- access the same data concurrently from multiple threads should be possible and originate no crashes
- on any major platform, the same database must be used for all apps
- data should be secure with transparent encryption and decryption
- data changes must appear automatically when UI connects to Realm

Furthermore, at the moment, the solution implemented has limitations. In order to strike a balance between flexibility and performance,  realistic limits are imposed on various aspects of information storage in a Realm like the length of names of classes. In addition,
sorting and case insensitive string matches in queries are only supported for some character sets and the case insensitive flag used in string comparison only works on characters from the English locale. Thirdly, although Realm files can be accessed by multiple threads concurrently, handing over Realms, Realm objects, queries, and results between threads is not possible. Finally, Realm files cannot be accessed by concurrent processes.

## Use Cases <a name="cases"></a>

## Domain Model <a name="domain"></a>

This project can be conceptually represented by six classes and the relations between them, as shown in the diagram bellow.

![Domain model](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/domain%20model.png)

This representation can bridge the gap between Use-Case Model and Software Design Models. Therefore, allowing everyone on the team to 
have a understanding of the structure of Realm and minimizing miscommunication and misunderstanding of the solution that will be used.

## Critics and opinions <a name="conclusion"></a>

## Contribuition <a name="contribuition"></a>
Carolina Centeio:

Inês Proença:

Hélder Antunes: 1%

Renato Abreu: 2%
