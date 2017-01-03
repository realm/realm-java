![Realm](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/realmDark.jpg)
# Analysis of the software developing process of Realm

## Introduction

Realm is a mobile database that runs directly inside phones, tablets or wearables. However at the moment, it currently runs only on Android.

The data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance and maintenance issues. In addition, the API has very few classes so that most of our users can pick it up intuitively, which, in turn allows getting simple apps up and running in minutes.

Realm also supports easy thread-safety, relationships and encryption. Another feature is that this database has proven to be faster than even raw SQLite on common operations, while maintaining an extremely rich feature set.

## Development process

Realm Java started 5 years ago and is developed by a team within the Realm organisation (Realm Inc.). It counts on 6-7 members, all developers (who also act as release managers, supporters, and testers). The team has regular video conferences and uses closed Slack channels for communication.

As an Agile project, Realm Java embraces change by considering issues filed by users.

This project can evolve iteratively in two different way: by adding features and correcting bugs. For this reason, Realm Java is divided in two main branches, both of which originate releases:

 * The master branch is responsible for the creation of new features and major changes in the project. As a result, is where new versions are released from.

 * On the other hand, the releases branch takes care of minor changes such as bug fixes, documentation, tests and build system. Patch versions are released from this branch.

## Critics and opinions

![Commits graph](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/commits%20graph.png)

After analysing this graphs, we can conclude that the development process is continuous with the main team supervising the pull requests, trying to maintain code standards and design. To ensure this, good contributing guidelines are available in [Contributing.md] (https://github.com/realm/realm-java/blob/master/CONTRIBUTING.md). In addition, it is very easy for new contributors and users to understand this application as it's [API](https://realm.io/docs/java/latest/) is well documented.

However, there are some things that can be improved:
* Coding guidelines should be more specific
* Automatize some pull requests analysis in order to avoid bottleneck effect: it is useless to have a lot of contributers when the amount of code checked is always the same. Besides, when the latest contributions are finally checked, there is the chance that they are already outdated.
* A roadmap could be helpful so that contributors know what features to implement

To sum up, in our opinion this project follows a good process model which allows a continuous evolution through times.

## Contribuitions
Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
