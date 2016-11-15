# Software Design

## Index

1. [Introduction](#introduction)
2. [Logical View](#logical)
3. [Development View](#development)
4. [Deployment View](#deployment)
5. [Process View](#process)
6. [Contribuition](#contribuition)

## Introduction <a name="introduction"></a>

## Logical View <a name="logical"></a>

This diagram shows the main structure of the Realm Java project (its grouping mechanisms and relations), starting at "io" package. Although this project also uses many java and android packages, we considered more important to represent it with Realm packages only.

![Logical View](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/logicview.jpg)

## Development View <a name="development"></a>

## Deployment View <a name="deployment"></a>

In order for the user to be able to use the same database for all his apps, on any major platform. Realm Java needs to connect to a server application named Realm Server Object which can, in turn, verify the user identity, grant permissions needed for reading and writing data, access and modify data mirrorded on mobile devices. The communication between devices is encrypted to ensure the security of the data.

![Deployment Diagram](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/Deployment%20Diagram.png)

This deployment diagram illustrates how the devices sync with one another without conficts.  

## Process View <a name="process"></a>

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
