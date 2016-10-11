# Analysis of the software developing process of Realm

## Introduction

Realm is a mobile database that runs directly inside phones, tablets or wearables. However at the moment, it currently runs only on Android.

The data is directly exposed as objects and queryable by code, removing the need for ORM's riddled with performance & maintenance issues. In addition, the API has very few classes so that most of our users can pick it up intuitively, which, in turn allows getting simple apps up and running in minutes.

Realm also supports easy thread-safety, relationships and encryption. Another feature is that this database has proven to be faster than even raw SQLite on common operations, while maintaining an extremely rich feature set.

## Development process

### Specification
You can declare bugs, declaring an issue, which you should have the following information:
- Goals
- Expected results
- Current results
- Steps to reproduce
- Code sample that highlights the issue

You can work at some improvements that you think is useful, and send an issue to receive someone's feedback.
Also, it is possible to work on an idea proposed by other contributors that you find interesting.

### Design and implementation
Since Realm is a large open source project with a lot of developers, the project development and implementation follow a set of rules defined by the main contributors. 
So, first is necessary to accept a Contributor License Agreement. Then, it is possible to contribute code, documentation, or any other improvements. However, every pull request must obey this guidelines:
- Code Style
- Unit Tests
- Javadoc
- Branch Strategy

These changes are then verified by the admnisters, and if accepted merged to the main branch of the project.

### Validation
All PR's must be accompanied by related unit tests. All bug fixes must have a unit test proving that the bug is fixed. You can use ./realm/gradlew connectedCheck createDebugCoverageReport to generate a coverage report to check for missing unit test coverage. The aim is 100% code coverage.

### Evolution
As a Agile project, Realm Java embraces change by considering issues filed by users.

This project can evolve iteratively in two different ways, by adding features or correcting bugs. For this reason, Realm Java is divided in two main branches, both of which originate releases.

The master branch is responsible for the creation of new features and major changes in the project. As a result is where new versions are released from.

On the other hand, the releases branch takes care of minor changes such as bug fixes, documentation, tests and build system. Patch versions are released from this branch.

## Software process model

## Critics and opinions
