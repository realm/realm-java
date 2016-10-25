# Requirements elicitation

## Index

1. [Requirements: Introduction, Scope, and Description](#requirements)
2. [Specific Requirements and Features](#features)
3. [Use Cases](#cases)
3. [Domain Model](#domain)
4. [Critics and opinions](#conclusion)
5. [Contribuition](#contribuition)


## Requirements: Introduction, Scope, and Description <a name="requirements"></a>

## Specific Requirements and Features <a name="features"></a>

## Use Cases <a name="cases"></a>

## Domain Model <a name="domain"></a>

## Critics and opinions <a name="conclusion"></a>

## Contribuition <a name="contribuition"></a>
Carolina Centeio:

Inês Proença:

Hélder Antunes: 110%

Renato Abreu: -10%

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
