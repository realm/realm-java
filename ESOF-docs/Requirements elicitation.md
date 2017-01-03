# Requirements elicitation

## Index

1. [Requirements: Introduction, Scope, and Description](#requirements)
2. [Specific Requirements and Features](#features)
3. [Use Cases](#cases)
4. [Domain Model](#domain)
5. [Contribuition](#contribuition)

## Requirements: Introduction, Scope, and Description <a name="requirements"></a>

Realm, as an open source project, is not governed by strict procedures. This gradually changes according to the people hired and to what was previously done wrong in major releases.

Regarding the requirements scope, the team has their focus on what makes sense for the users. Since the developers/contributors are users as well, they have a good notion of what is needed to improve or implement. Also, the main team is quite available to attend and participate on meetups or conferences. By doing so, innovative ideas and constructive critics from general community are extremely well received and encouraged.

Realm's main team is responsible to define priorities and choose what will be implented or developed. However, some team members have more influence according to their area of expertise.

In order to develop new features or improve the ones already implemented, the contributors can use Issues or Pull requests:

Issues are used to keep track of tasks and bugs within the software. Also, it is possible to talk about the respective issue with other contributors.

Furthermore, if someone patches a bug or adds a new feature, a new PR is open and a lot of discussion revolves around that feature.
For general PR, at least two developers (often working together) that have not written any code, must approve the PR. For very large or complex PR, a private meeting might be scheduled in order to highlight the most important points of that PR. Ultimately the decision to merge those changes is made by the main developers.

An example of a discussion around a PR is shown [here](https://github.com/realm/realm-java/pull/3285).

So, issues, PR and the discussion on those represents the main core of requirements elicitation and validation.

We contacted [Kenneth Geisshirt](https://github.com/kneth) to know more about requirements elicitation. 

## Specific Requirements and Features <a name="features"></a>

An alternative to SQLite and Core Data was needed so that databases could run directly inside phones, tablets and wearables. Also, this alternative must be capable of a simple saving and fast queries, to save weeks of development time. In order to specify the project's requirements, the team chose GitHub issues as the main mean of communication, but the internal wiki or Google Doc/Sheet can also be used occasionally.

>  We don't have a specification to write specifications. Mostly, we are using Github issues to specify requirements but in rare cases, we use our internal wiki (a private Github repo) or share a Google Doc/Sheet.

Then, Realm functional requirements are:

- creation and removal of database classes and their instances
- abitily of creating relationships between classes
- performing database queries and accessing the results
- insuring the implementation and integrity of transactions

However, there are some factors that constrain the solution (non-functional requirements) used by Realm, such as:

- all Android versions since API Level 9 (Android 2.3 Gingerbread) should be supported.
- functionality must be offline-first
- queries need to be fast
- access the same data concurrently from multiple threads should be possible and originate no crashes
- on any major platform, the same database must be used for all apps
- data should be secure with transparent encryption and decryption
- data changes must appear automatically when UI connects to Realm

The present solution implemented has limitations. In order to strike a balance between flexibility and performance, realistic limits are imposed on various aspects of information storage in a Realm, like the length of names of classes. In addition, sorting and case insensitive string matches in queries are only supported for some character sets and the case insensitive flag used in string comparison only works on characters from the English locale. Thirdly, although Realm files can be accessed by multiple threads concurrently, handing over Realms, Realm objects, queries, and results between threads is not possible. Finally, Realm files cannot be accessed by concurrent processes.

## Use Cases <a name="cases"></a>

As an alternative to SQLite and Core Data, Realm Mobile Database is simple saving and fast to store, organize and get your data. It offers a wide range of materials to build a database, making it easier to learn and implement.

###Use-Cases listed

<img src="https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/usecaseslist.jpg" width="500">

###Use-Cases Diagram

<img src="https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/usecases.jpg" width="500">

## Domain Model <a name="domain"></a>

This project can be conceptually represented by six classes and their relationships, as shown in the diagram bellow.

![Domain model](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/domain%20model.png)

This representation can bridge the gap between Use-Case Model and Software Design Model. Therefore, allowing everyone on the team to be on the same page regarding the structure of Realm.

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
