# Software Design

## Index

1. [Introduction](#introduction)
2. [Logical View](#logical)
3. [Development View](#development)
4. [Deployment View](#deployment)
5. [Process View](#process)
6. [Contribuition](#contribuition)

## Introduction <a name="introduction"></a>

Software architecture is described as the organization of a system, where the system represents a set of components and the relationships between them that accomplish the requirements.

4+1 Architectural View Model is a view model used to describe the system from the viewpoint of different stakeholders, such as end-users, developers and project managers. The four views of the model (Logical, development, deployment and process) are presented in this report. Use cases or scenarios (described in previous report) are used to illustrate the architecture serving as the 'plus one' view. 

Realm is a database intended to be part of larger systems. Therefore, Realm doesn't follow any architectural patterns, just try to be usable by them. For instance, most of the mobile apps that use Realm, allowing communication between a server and multiple devices, are following the client-server architectural pattern.

## Logical View <a name="logical"></a>

This diagram shows the main structure of the Realm Java project (its grouping mechanisms and relations), starting at "io" package. Although this project also uses many java and android packages, we considered more important to represent it with Realm packages only.

![Logical View](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/logicview.jpg)

## Development View <a name="development"></a>

Realm has a system that can be easily integrated with other applications, so it follows a basic architecture that also allows an optimal development process.
The system runs trough the created API and uses processor-annotations(ReamProcessor) made specifically for that API, where a bridge is created between both. The transformer component, throughout the application building process, is useful to send data to Realm.
The following diagram contains Realm software components, the dependencies between them and the main functionaly of each one.
![Development View](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/DevelopmentView.png)

## Deployment View <a name="deployment"></a>

In order for the user to be able to use the same database for all his apps, on any major platform. Realm Java needs to connect to a server application named Realm Server Object which can, in turn, verify the user identity, grant permissions needed for reading and writing data, access and modify data mirrorded on mobile devices. The communication between devices is encrypted to ensure the security of the data.

![Deployment Diagram](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/Deployment%20Diagram.png)

This deployment diagram illustrates how the devices sync with one another without conficts. By operating like this, Realm works smoothly as a distributed system and is able become part of larger systems.

## Process View <a name="process"></a>

![Process View Diagram](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/process%20view%20graph.png)

Any mobile app can use Realm. An application start a transaction before add or modify data. When a transaction ends, other application functions can start. The Realm database is based on Realm objects, and to add information in database it's necessary create a Realm object. To search for data it's needed create a query, which returns the data found in the form of Realm objects. 

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
