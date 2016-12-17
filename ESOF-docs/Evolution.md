![Realm](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/realmDark.jpg)
# Software Evolution

## Index

1. [Software Maintability](#maintability)
2. [Evolution Process](#evolution)
3. [Pull Request](#pull)
4. [Contribuition](#contribuition)

## Software Maintability <a name="maintability"></a>

![SIG Metrics](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/Metrics.png)



[![BCH compliance](https://bettercodehub.com/edge/badge/renatoabreu11/realm-java)](https://bettercodehub.com)

## Evolution Process <a name="evolution"></a>

Since Realm is a complex project and a new feature chosen by us could require an in depth analysis of Realm's core (written in C++) and a excelent knowledge of Realm's API architecture. That would be very hard to do, mainly because of our time to implement that feature is limited(two weeks). Therefore, we decided that the best step to take was search for [issues](https://github.com/realm/realm-java/issues?q=is%3Aopen+is%3Aissue+label%3AT%3AEnhancement) with Enhancement labels. By doing this, we knew that the feature was already requested and necessary to the project, and ultimately our implementation, despite of being accepted or not, it's a good effort to contribute something useful for the users. 

So, after an extensive analysis, the selected issue is [#3752](https://github.com/realm/realm-java/issues/3752). Realm is an application that replaces databases like SQLite & ORMs, so it makes sense that the queries besides being much faster also must have similiar functionalites. The LIKE query, in our perspective, is pretty important and since wildcard matches was merged to Realm Core, meaning that we could abstain from undestanding all the core details, it allowed us to add the required support to RealmQuery, as suggested in the issue. The feature that we decided to implement lets the user query a realm using the RealmQuery "like" function - RealmQuery.like(String field, String value) -, which is used to match a field against a value using wildcards ( * and ? ) and if the field expression is matched with the expression, it returns true.

## Pull Request <a name="pull"></a>

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
