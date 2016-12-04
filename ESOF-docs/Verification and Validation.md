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
* Build unit and instrumental testing so that they could test functionalites in a specific case, whithout dependencies and in the context of an app that has Android dependencies.
* Create example projects that show how Realm is used, and run Monkey Tests on those examples.
* Test Realm Object Server mechanisms (Authentication and Authorization)

### Controllability <a name="controllability"></a>

Since the unit test cases were developed to evaluate very specific features, it is easy to provide the program with new inputs and affect the software behaviour. So, the state of the component under test is easily modified by the inputs given.

Regarding the monkey tests - a program that runs on the emulator or device and generates pseudo-random streams of user events such as clicks, touches, or gestures, as well as a number of system-level events - on the various examples projects, the controllability is very low because it's hard to control the inputs given by the program and therefore the software behaviour is unknown and hard to replicate (unknown state of components). 

### Observability <a name="observability"></a>

Realm uses android instrumented tests for the purpose of testing on a mobile device. To this is added the use of the framework JUnit. 
With this it is possible to observe the tests that have failed or passed, as well as the code that was executed in each test. 
Monkey tests are also used, which basically generates pseudo-random streams of user events such as clicks, touches, or gestures. With this it is possible to observe possible internal errors of the application in situations of stress.

![instrumentedTests](https://github.com/renatoabreu11/realm-java/blob/master/ESOF-docs/Resources/test-types_2x.png)

### Isolateability <a name="isolateability"></a>

In each class of android tests, a specific situation is developed allowing to test at least one feature. Therefore, only API functions are used and tested. This makes isolateability higher among test cases, because the failure of one test does not imply the failure of another. So, in general, a component (class) under test can be sucessful tested in isolation.

However, in the example projects where it shown how to use Realm, all the API functions (or mostly of them) are used and consequently tested, so we can assume that in those cases the isolateability is always comparatively lower to the unit tests.

### Separation of Concerns <a name="concerns"></a>

In this project the main goal of using tests is to fully evaluate the API functions.
So there is a need to properly separate the responsability of all the test classes, because each one is responsible to test a different functionality present in Realm API.

### Understandability <a name="understandability"></a>

Regarding the [API](https://realm.io/docs/java/2.2.1/api/) itself, the core classes of Realm are explained in great detail. Since the project is fairly complex, extensively documenting the code is very important for new contributors to quickly understand the functionalities of each class.

In relation to the test module, the existence of documentation varies per the test. After analyzing this module, we conclude that, in general, the existing documentation for each test is insufficient to quickly realize the purpose of each one. For an open source project of this size and, more importantly, with the complexity inherent to Realm, the understandability is low.

### Heterogeneity <a name="heterogeneity"></a>

Heterogeneity is the degree to which the use of diverse technologies requires to use diverse test methods and tools in parallel.

Realm does not use many technologies. In fact, the only external libraries used are Android SDK to let this application run on Android, Android NDK and CMake to compile C++ code. On contrary, it aims to be adaptable enough to be used by other applications. 

As such, it uses both android and monkey tests to assure the reliabity of the realm code, it's integration in example applications and server funcionalities (Authentication and Authorization). In addition, Jenkins is used to run and store the results of unit tests, run intrumented tests, analyse static code and collect metrics.

## Test Statistics and Analytics <a name="statistics"></a>

We were unable to run the tests in the project. First we tried build the project in Windows and found that no one in the community had done it successfully. Then, we tried in Ubuntu, and we were able to compile successfully, although it took a long time due to the delays of the administrators' responses. 
Currently we are trying to run the project's tests without success, because more configuration is needed.

We created an [issue](https://github.com/realm/realm-java/issues/3845) to get help from the administrators of the project. 

## Bug Identification <a name="bugs"></a>

For the reasons stated above, we were unable to find and solve a bug.

## Contribuition <a name="contribuition"></a>

Carolina Centeio: 25%

Inês Proença: 25%

Hélder Antunes: 25%

Renato Abreu: 25%
