Realm for Realm pre-alpha 1
===========================

Setting up a new Android Studio project
---------------------------------------

Prerequisites:
 * Android Studio verions >= 0.8.6
 * A recent Android SDK

Setup:
 1) Create a new project with Android Studio
 2) Copy the 'realm' folder into the root folder of the new project (beside 'app', not inside it)
 3) Copy the 'realm-annotations-processor-0.80.jar' file to into 'app/libs'
 4) Add the following line to the 'settins.gradle' file:

    include ':realm'

 5) Add the following buildscript dependency to the 'build.gradle' file in the root folder of the project

    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.3'

 6) Add the following plugin inclusion in the top of the 'app/build.gradle' file

 	apply plugin: 'android-apt'

 7) Replace the 'dependencies' section of the 'app/build.gradle' file with:

	dependencies {
		apt fileTree(dir: 'libs', include: ['*.jar'])
		compile project(':realm')
	}

 8) In the Android Studio menu: Tools->Android->Sync Project with Gradle Files
