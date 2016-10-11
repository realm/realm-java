# Analysis of the software developing process of Realm

## Introduction

Realm is a mobile database that runs directly inside phones, tablets or wearables. However at the moment, it currently runs only on Android.

The data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance and maintenance issues. In addition, the API has very few classes so that most of our users can pick it up intuitively, which, in turn allows getting simple apps up and running in minutes.

Realm also supports easy thread-safety, relationships and encryption. Another feature is that this database has proven to be faster than even raw SQLite on common operations, while maintaining an extremely rich feature set.

## Development process

As an Agile project, Realm Java embraces change by considering issues filed by users.

This project can evolve iteratively in two different way: by adding features and correcting bugs. For this reason, Realm Java is divided in two main branches, both of which originate releases:

 *The master branch is responsible for the creation of new features and major changes in the project. As a result, is where new versions are released from.

 *On the other hand, the releases branch takes care of minor changes such as bug fixes, documentation, tests and build system. Patch versions are released from this branch.

Realm Java started 5 years ago and is developed by a team within the Realm organisation (Realm Inc.). It counts on 6-7 members, all developers (who also act as release managers, supporters, and testers). The team has regular video conferences and uses closed Slack channels for communication.


## Critics and opinions

