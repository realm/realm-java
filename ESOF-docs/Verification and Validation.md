# Verification and Validation

## Index

1. [Software Testability and Reviews](#testability)
 1. [Controllability](#controllability)
 2. [Observability](#observability)
 3. [Isolateability](#isolateability)
 4. [Separation of Concerns](#concerns)
 5. [Understandability](#understandability)
 6. [Heterogeneity](#heterogeneity)
2. [Test Statistics and Analytics](#statistics)
3. [Bug Identification](#bugs)

## Software Testability and Reviews <a name="testability"></a>

Realm is an application integrated within other applications, so all the API functionalities have to be deeply tested to achieve a good degree of reliability. 

Therefore, the developers decided to:
* Build unit and instrumental testing so that they could test functionalites in a specific case, whithout dependencies and in the context of a app that has Android dependencies.
* Create example projects that show how Realm is used, and run Monkey Tests on those examples.
* Test Realm Object Server mechanisms (Authentication and Authorization)

### Controllability <a name="controllability"></a>

Os caso de testes existentes foram desenvolvidos com vista a avaliar funcionalidades específicas e como tal as situações geradas são limitadas.

It is also possible to run monkey tests - a program that runs on the emulator or device and generates pseudo-random streams of user events such as clicks, touches, or gestures, as well as a number of system-level events - on the various examples projects. However, with this is hard to control the state of the components under test.   

### Observability <a name="observability"></a>

Neste projeto, não existe uma ferramente que permita correr automaticamente os testes unitários e analisar as estatísticas desses testes, daí que a análise tenha de ser manual e portanto mais exaustiva.

### Isolateability <a name="isolateability"></a>

Em cada classe de teste é desenvolvida uma situação especifica que permita testar uma ou várias funcionalidades. Deste modo apenas são utilizadas e testadas as funções necessárias da API, permitindo assim que haja uma certa isolabilidade entre os casos de teste, isto é, um teste falhar não implica a falha de outros. 

Contudo, também há interdependencias entre os casos de teste existentes, o que implica que o teste de uma funcionalidade mais complexa necessite de funções mais simples. 

De um modo geral, a isolabilidade do projeto é 

### Separation of Concerns <a name="concerns"></a>

In this project the main goal of using tests is to fully evaluate the API functions.
So there is a need to properly separate the responsability of all the test classes, because each one is responsible to test a different functionality present in Realm API.

### Understandability <a name="understandability"></a>

Regarding the [API](https://realm.io/docs/java/2.2.1/api/) itself, the core classes of Realm are explained in great detail. Since the project is fairly complex, extensively documenting the code is very important for new contributors to quickly understand the functionalities of each class.

In relation to the test module, the existence of documentation varies per the test. After analyzing this module, we conclude that, in general, the existing documentation for each test is insufficient to quickly realize the purpose of each one. For an open source project of this size and, more importantly, with the complexity inherent to Realm, the understandability is low.

### Heterogeneity <a name="heterogeneity"></a>

## Test Statistics and Analytics <a name="statistics"></a>

## Bug Identification <a name="bugs"></a>

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
