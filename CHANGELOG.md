## 5.6.0 (YYYY-MM-DD)

### Enhancements

* `@RealmClass("name")` and `@RealmField("name")` can now be used as a shorthand for defining custom name mappings (#6145).


## 5.5.0 (2018-08-31)

### Enhancements

* [ObjectServer] Added `ConnectionState` enum describing the states a connection can be in.
* [ObjectServer] Added `SyncSession.isConnected()` and `SyncSession.getConnectionState()`.
* [ObjectServer] Added support for observing connection changes for a session using `SyncSession.addConnectionChangeListener()` and `SyncSession.removeConnectionChangeListener()`.
* [ObjectServer] Added Kotlin extension property `Realm.syncSession` for synchronized Realms.
* [ObjectServer] Added Kotlin extension method `Realm.classPermissions<RealmModel>()`.
* [ObjectServer] Added support for starting and stopping synchronization using `SyncSession.start()` and `SyncSession.stop()` (#6135).
* [ObjectServer] Added API's for making it easier to work with network proxies (#6163): 
  * `SyncManager.setAuthorizationHeaderName(String headerName)`
  * `SyncManager.setAuthorizationHeaderName(String headerName, String host)`
  * `SyncManager.addCustomRequestHeader(String headerName, String headerValue)`
  * `SyncManager.addCustomRequestHeader(String headerName, String headerValue, String host)`
  * `SyncManager.addCustomRequestHeaders(Map<String, String> headers)`
  * `SyncManager.addCustomRequestHeaders(Map<String, String> headers, String host)`
  * `SyncConfiguration.Builder.urlPrefix(String prefix)`
 
### Bug Fixes

* Methods and classes requiring synchronized Realms have been removed from the standard AAR package. They are now only visible when enabling synchronized Realms in Gradle. The methods and classes will still be visible in the source files and docs, but annotated with `@ObjectServer` (#5799).

### Internal

* Updated to Realm Sync 3.9.4
* Updated to Realm Core 5.8.0
* Updated to Object Store commit: b0fc2814d9e6061ce5ba1da887aab6cfba4755ca

### Credits

* Thanks to @lucasdornelasv for improving the performance of `Realm.copyToRealm()`, `Realm.copyToRealmOrUpdate()` and `Realm.copyFromRealm()` #(6124). 


## 5.4.3 (YYYY-MM-DD)

### Bug Fixes

* [ObjectServer] ProGuard was not configured correctly when working with Subscriptions for Query-based Realms.


## 5.4.2 (2018-08-09)

### Bug Fixes

* [ObjectServer] Fixed bugs in the Sync Client that could lead to memory corruption and crashes.

### Internal

* Upgraded to Realm Sync 3.8.8


## 5.4.1 (2018-08-03)

### Bug Fixes

* Compile time crash if no `targetSdk` was defined in Gradle. This was introduced in 5.4.0 (#6082).
* Fix Realm Gradle Plugin adding dependencies in a way incompatible with Kotlin Android Extensions. This was introduced in Realm Java 5.4.0 (#6080).


## 5.4.0 (2018-07-22)

### Enhancements

* Removing a ChangeListener on invalid objects or `RealmResults` should warn instead of throwing (fixes #5855).

### Bug Fixes

* [ObjectServer] Using Android Network Security Configuration is necessary to install the custom root CA for tests (API >= 24) (#5970).
* Fixes issue with the incremental build causing direct access to model without accessor to fail (#6056).
* `RealmQuery.distinct()` is now correctly applied when calling `RealmQuery.count()` (#5958).

### Internal

* Upgraded to Realm Core 5.7.2
* Upgraded to Realm Sync 3.8.1
* [ObjectServer] Improved performance when integrating changes from the server.
* Added extra information about the state of the Realm file if an exception is thrown due to Realm not being able to open it.
* Removed internal dependency on Groovy in the Realm Transformer (#3971).

### Credits

* Thanks to @kageiit for removing Groovy from the Realm Transformer (#3971).


## 5.3.1 (2018-06-19)

### Bug Fixes

* [ObjectServer] Fixed a bug which could potentially flood Realm Object Server with PING messages.
* Calling `Realm.deleteAll()` on a Realm file that contains more classes than in the schema throws exception (#5745).
* `Realm.isEmpty()` returning false in some cases, even if all tables part of the schema are empty (#5745).
* Fixed rare native crash materializing as `Assertion failed: ref + size <= after_ref with (ref, size, after_ref, ndx, m_free_positions.size())` (#5300).

### Internal

* Upgraded to Realm Core 5.6.2
* Upgraded to Realm Sync 3.5.6
* Upgraded to Object Store commit `0bcb9643b8fb14323df697999b79c4a5341a8a21`


## 5.3.0 (2018-06-12)

### Enhancements

* [ObjectServer] `Realm.compactRealm(config)` now works on synchronized Realms (#5937).
* [ObjectServer] `SyncConfiguration.compactOnLaunch()` and `SyncConfiguration.compactOnLaunch(callback)` has been added (#5937).
* Added `RealmQuery.getRealm()`, `RealmResults.getRealm()`, `RealmList.getRealm()` and `OrderedRealmCollectionSnapshot.getRealm()` (#5997).
* Removing a ChangeListener on invalid objects or `RealmResults` should warn instead of throwing (fixes #5855).


### Internal

* Upgraded to Realm Core 5.6.0
* Upgraded to Realm Sync 3.5.2


## 5.2.0 (2018-06-06)

The feature previously named Partial Sync is now called Query-Based Sync and is now the default mode when synchronizing Realms.
This has impacted a number of API's. See below for the details.

### Deprecated

* [ObjectServer] `SyncConfiguration.automatic()` has been deprecated in favour of `SyncUser.getDefaultConfiguration()`.
* [ObjectServer] `new SyncConfiguration.Builder(user, url)` has been deprecated in favour of `SyncUser.createConfiguration(url)`. NOTE: Creating configurations using `SyncUser` will default to using query-based Realms, while creating them using `new SyncConfiguration.Builder(user, url)` will default to fully synchronized Realms.
* [ObjectServer] With query-based sync being the default `SyncConfiguration.Builder.partialRealm()` has been deprecated. Use ``SyncConfiguration.Builder.fullSynchronization()` if you want full synchronisation instead.

### Enhancements

* [ObjectServer] Added `SyncUser.createConfiguration(url)`. Realms created this way are query-based Realms by default.
* [ObjectServer] Added `SyncUser.getDefaultConfiguration()`.
* The Realm bytecode transformer now supports incremental builds (#3034).
* Improved speed and allocations when parsing field descriptions in queries (#5547).

### Bug Fixes

* Having files that ends with `RealmProxy` will no longer break the Realm Transformer (#3709).

### Internal

* Module mediator classes being generated now produces a stable output enabling better support for incremental builds (#3034).


## 5.1.0 (2018-04-25)

### Enhancements

* [ObjectServer] Added support for `SyncUser.requestPasswordReset()`, `SyncUser.completePasswordReset()`
  and their async variants. This makes it possible to reset the password for users created using
  `Credentials.usernamePassword()` where they used their email as username (#5821).
* [ObjectServer] Added support for `SyncUser.requestEmailConfirmation()`, `SyncUser.confirmEmail()`
  and their async variants. This makes it possible to ask users to confirm their email. This is only
  supported for users created using `Credentials.usernamePassword()` who have used an email as their
  username (#5821).
* `RealmQuery.in()` now support `null` which will always return no matches (#4011).
* Added support for `RealmQuery.alwaysTrue()` and `RealmQuery.alwaysFalse()`.

### Bug Fixes

* Changing a primary key from being nullable to being required could result in objects being deleted (##5899).


## 5.0.1 (2018-04-09)

### Enhancements

* [ObjectServer] `SyncConfiguration.automatic()` will make use of the host port to work out the default Realm URL.
* [ObjectServer] A role is now automatically created for each user with that user as its only member. This simplifies the common use case of restricting access to specific objects to a single user. This role can be accessed at `PermissionUser.getRole()`.
* [ObjectServer] Expose `Role.getMembers()` to access the list of associated `UserPermission`.

### Bug Fixes

* `RealmList.move()` did not move items correctly for unmanaged lists (#5860).
* `RealmObject.isValid()` not correctly returns `false` if `null` is provided as an argument (#5865).
* `RealmQuery.findFirst()` and `RealmQuery.findFirstAsync()` not working correctly with sorting (#5714).
* Permission `noPrivileges` and `allPrivileges` were returning opposite privileges.
* Fixes an issue caused by JNI local table reference overflow (#5880).

### Internal

* Upgraded to Realm Sync 3.0.1
* Upgraded to Realm Core 5.4.2

## 5.0.0 (2018-03-15)

This release is compatible with the Realm Object Server 3.0.0-beta.3 or later.

### Known Bugs

* API's marked @ObjectServer are shipped as part of the base binary, they should only be available when enabling synchronized Realms.

### Breaking Changes

* [ObjectServer] Renamed `SyncUser.currentUser()` to `SyncUser.current()`.
* [ObjectServer] Renamed `SyncUser.login(...)` and `SyncUser.loginAsync(...)` to `SyncUser.logIn(...)` and `SyncUser.logInAsync(...)`.
* [ObjectServer] Renamed `SyncUser.logout()` to `SyncUser.logOut()`.
* The `OrderedCollectionChangeSet` parameter in `OrderedRealmCollectionChangeListener.onChange()` is no longer nullable. Use `changeSet.getState()` instead (#5619).
* `realm.subscribeForObjects()` have been removed. Use `RealmQuery.findAllAsync(String subscriptionName)` and `RealmQuery.findAllAsync()` instead.
* Removed previously deprecated `RealmQuery.findAllSorted()`, `RealmQuery.findAllSortedAsync()` `RealmQuery.distinct()` and `RealmQuery.distinctAsync()`.
* Renamed `RealmQuery.distinctValues()` to `RealmQuery.distinct()`

### Enhancements

* [ObjectServer] Added support for partial Realms. Read [here](https://realm.io/docs/java/latest/#partial-realms) for more information.
* [ObjectServer] Added support for Object Level Permissions (requires partial synchronized Realms). Read [here](https://realm.io/docs/java/latest/#partial-realms) for more information.
* [ObjectServer] Added `SyncConfiguration.automatic()` and `SyncConfiguration.automatic(SyncUser user)` (#5806).
* Added two new methods to `OrderedCollectionChangeSet`: `getState()` and `getError()` (#5619).

## Bug Fixes

* Better exception message if a non model class is provided to methods only accepting those (#5779).

### Internal

* Upgraded to Realm Sync 3.0.0
* Upgraded to Realm Core 5.3.0


## 4.4.0 (2018-03-13)

### Enhancements

* Added support for mapping between a Java name and the underlying name in the Realm file using `@RealmModule`, `@RealmClass` and `@RealmField` annotations (#5280).

## Bug Fixes

* [ObjectServer] Fixed an issue where login after a logout will not resume Syncing (https://github.com/realm/my-first-realm-app/issues/22).


## 4.3.4 (2018-02-06)

## Bug Fixes

* Added missing `RealmQuery.oneOf()` for Kotlin that accepts non-nullable types (#5717).
* [ObjectServer] Fixed an issue preventing sync to resume when the network is back (#5677).

## 4.3.3 (2018-01-19)

### Internal

* Downgrade JavaAssist to 3.21.0-GA to fix an issue with a `ClassNotFoundException` at runtime (#5641).


## 4.3.2 (2018-01-17)

### Bug Fixes

* Throws a better exception message when calling `RealmObjectSchema.addField()` with a `RealmModel` class (#3388).
* Use https for Realm version checker (#4043).
* Prevent Realms Gradle plugin from transitively forcing specific versions of Google Build Tools onto downstream projects (#5640).
* [ObjectServer] logging a warning message instead of throwing an exception, when sync report an unknown error code (#5403).

### Enhancements

* [ObjectServer] added support for both Anonymous and Nickname authentication.


### Internal

* Upgraded to Realm Sync 2.2.9
* Upgraded to Realm Core 5.1.2

## 4.3.1 (2017-12-06)

### Bug Fixes

* Fixed kotlin standard library being added to both Java and Kotlin projects (#5587).


## 4.3.0 (2017-12-05)

### Deprecated

* Support for mips devices are deprecated.
* `RealmQuery.findAllSorted()` and `RealmQuery.findAllSortedAsync()` variants in favor of predicate `RealmQuery.sort().findAll()`.
* `RealmQuery.distinct()` and `RealmQuery.distinctAsync()` variants in favor of predicate `RealmQuery.distinctValues().findAll()`

### Enhancements

* [ObjectServer] Added explicit support for JSON Web Tokens (JWT) using `SyncCredentials.jwt(String token)`. It requires Object Server 2.0.23+ (#5580).
* Projects using Kotlin now include additional extension functions that make working with Kotlin easier. See [docs](https://realm.io/docs/java/latest/#kotlin) for more info (#4684).
* New query predicate: `sort()`.
* New query predicate: `distinctValues()`. Will be renamed to `distinct` in next major version.
* The Realm annotation processor now has a stable output when there are no changes to model classes, improving support for incremental compilers (#5567).

### Bug Fixes

* Added missing `toString()` for the implementation of `OrderedCollectionChangeSet`.
* Sync queries are evaluated immediately to solve the performance issue when the query results are huge, `RealmResults.size()` takes too long time (#5387).
* Correctly close the Realm instance if an exception was thrown while opening it. This avoids `IllegalStateException` when deleting the Realm in the catch block (#5570).
* Fixed the listener on `RealmList` not being called when removing the listener then adding it again (#5507). Please notice that a similar issue still exists for `RealmResults`.

### Internal

* Use `OsList` instead of `OsResults` to add notification token on for `RealmList<RealmModel>`.
* Updated Gradle and plugins to support Android Studio `3.0.0` (#5472).
* Upgraded to Realm Sync 2.1.8.
* Upgraded to Realm Core 4.0.4.

### Credits

* Thanks to @tbsandee for fixing a typo (#5548).
* Thanks to @vivekkiran for updating Gradle and plugins to support Android Studio `3.0.0` (#5472).
* Thanks to @madisp for adding better support for incremental compilers (#5567).


## 4.2.0 (2017-11-17)

### Enhancements

* Added support for using non-encrypted Realms in multiple processes. Some caveats apply. Read [doc](https://realm.io/docs/java/latest/#multiprocess) for more info (#1091).
* Added support for importing primitive lists from JSON (#5362).
* [ObjectServer] Support SSL validation using Android TrustManager (no need to specify `trustedRootCA` in `SynConfiguration` if the certificate is installed on the device), fixes (#4759).
* Added the and() function to `RealmQuery` in order to improve readability.

### Bug Fixes

* Leaked file handler in the Realm Transformer (#5521).
* Potential fix for "RealmError: Incompatible lock file" crash (#2459).

### Internal

* Updated JavaAssist to 3.22.0-GA.
* Upgraded to Realm Sync 2.1.4.
* Upgraded to Realm Core 4.0.3.

### Credits

* Thanks to @rakshithravi1997 for adding `RealmQuery.and()` (#5520).


## 4.1.1 (2017-10-27)

### Bug Fixes

* Fixed the compile warnings of using deprecated method `RealmProxyMediator.getTableName()` in generated mediator classes (#5455).
* [ObjectServer] now retrying network query when encountering any `IOException` (#5453).
* Fixed a `NoClassDefFoundError` due to using `@SafeVarargs` below API 19 (#5463).

### Internal

* Updated Realm Sync to 2.1.0.


## 4.1.0 (2017-10-20)

### Enhancements

* `Realm.deleteRealm()` and `RealmConfiguration.assetFile()` are multi-processes safe now.

### Bug Fixes

* Fix some potential database corruption caused by deleting the Realm file while a Realm instance are still opened in another process or the sync client thread.
* Added `realm.ignoreKotlinNullability` as a kapt argument to disable treating kotlin non-null types as `@Required` (#5412) (introduced in `v3.6.0`).
* Increased http connect/write timeout for low bandwidth network.


## 4.0.0 (2017-10-16)

### Breaking Changes

The internal file format has been upgraded. Opening an older Realm will upgrade the file automatically, but older versions of Realm will no longer be able to read the file.

* [ObjectServer] Updated protocol version to 22 which is only compatible with Realm Object Server >= 2.0.0.
* [ObjectServer] Removed deprecated APIs `SyncUser.retrieveUser()` and `SyncUser.retrieveUserAsync()`. Use `SyncUser.retrieveInfoForUser()` and `retrieveInfoForUserAsync()` instead.
* [ObjectServer] `SyncUser.Callback` now accepts a generic parameter indicating type of object returned when `onSuccess` is called.
* [ObjectServer] Renamed `SyncUser.getAccessToken` to `SyncUser.getRefreshToken`.
* [ObjectServer] Removed deprecated API `SyncUser.getManagementRealm()`.
* Calling `distinct()` on a sorted `RealmResults` no longer clears any sorting defined (#3503).
* Relaxed upper bound of type parameter of `RealmList`, `RealmQuery`, `RealmResults`, `RealmCollection`, `OrderedRealmCollection` and `OrderedRealmCollectionSnapshot`.
* Realm has upgraded its RxJava1 support to RxJava2 (#3497)
  * `Realm.asObservable()` has been renamed to `Realm.asFlowable()`.
  * `RealmList.asObservable()` has been renamed to `RealmList.asFlowable()`.
  * `RealmResults.asObservable()` has been renamed to `RealmResults.asFlowable()`.
  * `RealmObject.asObservable()` has been renamed to `RealmObject.asFlowable()`.
  * `RxObservableFactory` now return RxJava2 types instead of RxJava1 types.
* Removed deprecated APIs `RealmSchema.close()` and `RealmObjectSchema.close()`. Those don't have to be called anymore.
* Removed deprecated API `RealmResults.removeChangeListeners()`. Use `RealmResults.removeAllChangeListeners()` instead.
* Removed deprecated API `RealmObject.removeChangeListeners()`. Use `RealmObject.removeAllChangeListeners()` instead.
* Removed `UNSUPPORTED_TABLE`, `UNSUPPORTED_MIXED` and `UNSUPPORTED_DATE` from `RealmFieldType`.
* Removed deprecated API `RealmResults.distinct()`/`RealmResults.distinctAsync()`. Use `RealmQuery.distinct()`/`RealmQuery.distinctAsync()` instead.
* `RealmQuery.createQuery(Realm, Class)`, `RealmQuery.createDynamicQuery(DynamicRealm, String)`, `RealmQuery.createQueryFromResult(RealmResults)` and `RealmQuery.createQueryFromList(RealmList)` have been removed. Use `Realm.where(Class)`, `DynamicRealm.where(String)`, `RealmResults.where()` and `RealmList.where()` instead.

### Enhancements

* [ObjectServer] `SyncUserInfo` now also exposes a users metadata using `SyncUserInfo.getMetadata()`
* `RealmList` can now contain `String`, `byte[]`, `Boolean`, `Long`, `Integer`, `Short`, `Byte`, `Double`, `Float` and `Date` values. [Queries](https://github.com/realm/realm-java/issues/5361) and [Importing primitive lists from JSON](https://github.com/realm/realm-java/issues/5362) are not supported yet.
* Added support for lists of primitives in `RealmObjectSchema` with `addRealmListField(String fieldName, Class<?> primitiveType)`
* Added support for lists of primitives in `DynamicRealmObject` with `setList(String fieldName, RealmList<?> list)` and `getList(String fieldName, Class<?> primitiveType)`.
* Minor performance improvement when copy/insert objects into Realm.
* Added `static RealmObject.getRealm(RealmModel)`, `RealmObject.getRealm()` and `DynamicRealmObject.getDynamicRealm()` (#4720).
* Added `RealmResults.asChangesetObservable()` that emits the pair `(results, changeset)` (#4277).
* Added `RealmList.asChangesetObservable()` that emits the pair `(list, changeset)` (#4277).
* Added `RealmObject.asChangesetObservable()` that emits the pair `(object, changeset)` (#4277).
* All Realm annotations are now kept at runtime, allowing runtime tools access to them (#5344).
* Speedup schema initialization when a Realm file is first accessed (#5391).

### Bug Fixes

* [ObjectServer] Exposing a `RealmConfiguration` that allows a user to open the backup Realm after the client reset (#4759/#5223).
* [ObjectServer] Realm no longer throws a native “unsupported instruction” exception in some cases when opening a synced Realm asynchronously (https://github.com/realm/realm-object-store/issues/502).
* [ObjectServer] Fixed "Cannot open the read only Realm" issue when get`PermissionManager` (#5414).
* Throw `IllegalArgumentException` instead of `IllegalStateException` when calling string/binary data setters if the data length exceeds the limit.
* Added support for ISO8601 2-digit time zone designators (#5309).
* "Bad File Header" caused by the device running out of space while compacting the Realm (#5011).
* `RealmQuery.equalTo()` failed to find null values on an indexed field if using Case.INSENSITIVE (#5299).
* Assigning a managed object's own list to itself would accidentally clear it (#5395).
* Don't try to acquire `ApplicationContext` if not available in `Realm.init(Context)` (#5389).
* Removing and re-adding a changelistener from inside a changelistener sometimes caused notifications to be missed (#5411).

### Internal

* Upgraded to Realm Sync 2.0.2.
* Upgraded to Realm Core 4.0.2.
* Upgraded to OkHttp 3.9.0.
* Upgraded to RxJava 2.1.4.
* Use Object Store to create the primary key table.

### Credits

* Thanks to @JussiPekonen for adding support for 2-digit time zone designators when importing JSON (#5309).


## 3.7.2 (2017-09-12)

### Bug Fixes

* Fixed a JNI memory issue when doing queries which might potentially cause various native crashes.
* Fixed a bug that `RealmList.deleteFromRealm(int)`, `RealmList.deleteFirstFromRealm()` and `RealmList.deleteLastFromRealm()` did not remove target objects from Realm. This bug was introduced in `3.7.1` (#5233).
* Crash with "'xxx' doesn't exist in current schema." when ProGuard is enabled (#5211).


## 3.7.1 (2017-09-07)

### Bug Fixes

* Fixed potential memory leaks of `LinkView` when calling bulk insertions APIs.
* Fixed possible assertion when using `PermissionManager` at the beginning (#5195).
* Crash caused by JNI couldn't find `SharedRealm`'s inner classes when ProGuard is enabled (#5211).

### Internal

* Replaced LinkView with Object Store's List.
* Renaming `io.realm.internal.CollectionChangeSet` to `io.realm.internal.OsCollectionChangeSet`.


## 3.7.0 (2017-09-01)

### Deprecated

* [ObjectServer] `SyncUser.getManagementRealm()`. Use `SyncUser.getPermissionManager()` instead.

### Enhancements

* [ObjectServer] `SyncUser.getPermissionManager` added as a helper API for working with permissions and permission offers.

### Internal

* [ObjectServer] Upgraded OkHttp to 3.7.0.


## 3.6.0 (2017-09-01)

### Breaking Changes

* [ObjectServer] `SyncUser.logout()` no longer throws an exception when associated Realms instances are not closed (#4962).

### Deprecated

* [ObjectServer] `SyncUser#retrieveUser` and `SyncUser#retrieveUserAsync` replaced by `SyncUser#retrieveInfoForUser`
and `SyncUser#retrieveInfoForUserAsync` which returns a `SyncUserInfo` with mode information (#5008).
* [ObjectServer] `SyncUser#Callback` replaced by the generic version `SyncUser#RequestCallback<T>`.

### Enhancements

* [ObjectServer] Added `SyncSession.uploadAllLocalChanges()`.
* [ObjectServer] APIs of `UserStore` have been changed to support same user identity but different authentication server scenario.
* [ObjectServer] Added `SyncUser.allSessions` to retrieve the all valid sessions belonging to the user (#4783).
* Added `Nullable` annotation to methods that may return `null` in order to improve Kotlin usability. This also introduced a dependency to `com.google.code.findbugs:jsr305`.
* `org.jetbrains.annotations.NotNull` is now an alias for `@Required`. This means that the Realm Schema now fully understand Kotlin non-null types.
* Added support for new data type `MutableRealmIntegers`. The new type behaves almost exactly as a reference to a Long (mutable nullable, etc) but supports `increment` and `decrement` methods, which implement a Conflict Free Replicated Data Type, whose value will converge even when changed across distributed devices with poor connections (#4266).
* Added more detailed exception message for `RealmMigrationNeeded`.
* Bumping schema version only without any actual schema changes will just succeed even when the migration block is not supplied. It threw an `RealmMigrationNeededException` before in the same case.
* Throw `IllegalStateException` when schema validation fails because of wrong declaration of `@LinkingObjects`.

### Bug Fixes

* Potential crash after using `Realm.getSchema()` to change the schema of a typed Realm. `Realm.getSchema()` now returns an immutable `RealmSchema` instance.
* `Realm.copyToRealmOrUpdate()` could cause a `RealmList` field to contain duplicated elements (#4957).
* `RealmSchema.create(String)` and `RealmObjectSchema.setClassName(String)` did not accept class name whose length was 51 to 57.
* Workaround for an Android JVM crash when using `compactOnLaunch()` (#4964).
* Class name in exception message from link query is wrong (#5096).
* The `compactOnLaunch` callback is no longer invoked if the Realm at that path is already open on other threads.

### Internal

* [ObjectServer] removed `ObjectServerUser` and its inner classes, in a step to reduce `SyncUser` complexity (#3741).
* [ObjectServer] changed the `SyncSessionStopPolicy` to `AfterChangesUploaded` to align with other binding and to prevent use cases where the Realm might be deleted before the last changes get synchronized (#5028).
* Upgraded Realm Sync to 1.10.8
* Let Object Store handle migration.


## 3.5.0 (2017-07-11)

### Enhancements

* Added `RealmConfiguration.Builder.compactOnLaunch()` to compact the file on launch (#3739).
* [ObjectServer] Adding user lookup API for administrators (#4828).
* An `IllegalStateException` will be thrown if the given `RealmModule` doesn't include all required model classes (#3398).

### Bug Fixes

* Bug in `isNull()`, `isNotNull()`, `isEmpty()`, and `isNotEmpty()` when queries involve nullable fields in link queries (#4856).
* Bug in how to resolve field names when querying `@LinkingObjects` as the last field (#4864).
* Rare crash in `RealmLog` when log level was set to `LogLevel.DEBUG`.
* Broken case insensitive query with indexed field (#4788).
* [ObjectServer] Bug related to the behaviour of `SyncUser#logout` and the use of invalid `SyncUser` with `SyncConfiguration` (#4822).
* [ObjectServer] Not all error codes from the server were recognized correctly, resulting in UNKNOWN being reported instead.
* [ObjectServer] Prevent the use of a `SyncUser` that explicitly logged out, to open a Realm (#4975).

### Internal

* Use Object Store to do table initialization.
* Removed `Table#Table()`, `Table#addEmptyRow()`, `Table#addEmptyRows()`, `Table#add(Object...)`, `Table#pivot(long,long,PivotType)` and `Table#createnative()`.
* Upgraded Realm Core to 2.8.6
* Upgraded Realm Sync to 1.10.5
* Removed `io.realm.internal.OutOfMemoryError`. `java.lang.OutOfMemoryError` will be thrown instead.


## 3.4.0 (2017-06-22)

### Breaking Changes

* [ObjectServer] Updated protocol version to 18 which is only compatible with ROS > 1.6.0.

### Deprecated

* `RealmSchema.close()` and `RealmObjectSchema.close()`. They don't need to be closed manually. They were added to the public API by mistake.

### Enhancements

* [ObjectServer] Added support for Sync Progress Notifications through `SyncSession.addDownloadProgressListener(ProgressMode, ProgressListener)` and `SyncSession.addUploadProgressListener(ProgressMode, ProgressListener)` (#4104).
* [ObjectServer] Added `SyncSession.getState()` (#4784).
* Added support for querying inverse relationships (#2904).
* Moved inverse relationships out of beta stage.
* Added `Realm.getDefaultConfiguration()` (#4725).

### Bug Fixes

* [ObjectServer] Bug which may crash when the JNI local reference limitation was reached on sync client thread.
* [ObjectServer] Retrying connections with exponential backoff, when encountering `ConnectException` (#4310).
* When converting nullable BLOB field to required, `null` values should be converted to `byte[0]` instead of `byte[1]`.
* Bug which may cause duplicated primary key values when migrating a nullable primary key field to not nullable. `RealmObjectSchema.setRequired()` and `RealmObjectSchema.setNullable()` will throw when converting a nullable primary key field with null values stored to a required primary key field.

### Internal

* Upgraded to Realm Sync 1.10.1
* Upgraded to Realm Core 2.8.4

### Credits

* Thanks to Anis Ben Nsir (@abennsir) for upgrading Roboelectric in the unitTestExample (#4698).


## 3.3.2 (2017-06-09)

### Bug Fixes

* [ObjectServer] Crash when an authentication error happens (#4726).
* [ObjectServer] Enabled encryption with Sync (#4561).
* [ObjectServer] Admin users did not connect correctly to the server (#4750).

### Internal

* Factor out internal interface ManagedObject.

## 3.3.1 (2017-05-26)

### Bug Fixes

* [ObjectServer] Accepted extra columns against synced Realm (#4706).


## 3.3.0 (2017-05-24)

### Enhancements

* [ObjectServer] Added two options to `SyncConfiguration` to provide a trusted root CA `trustedRootCA` and to disable SSL validation `disableSSLVerification` (#4371).
* [ObjectServer] Added support for changing passwords through `SyncUser.changePassword()` using an admin user (#4588).

### Bug Fixes

* Queries on proguarded Realm model classes, failed with "Table not found" (#4673).


## 3.2.1 (2017-05-19)

### Enhancements

* Not in transaction illegal state exception message changed to "Cannot modify managed objects outside of a write transaction.".

### Bug Fixes

* [ObjectServer] `schemaVersion` was mistakenly required in order to trigger migrations (#4658).
* [ObjectServer] Fields removed from model classes will now correctly be hidden instead of throwing an exception when opening the Realm (#4658).
* Random crashes which were caused by a race condition in encrypted Realm (#4343).

### Internal

* Upgraded to Realm Sync 1.8.5.
* Upgraded to Realm Core 2.8.0.

## 3.2.0 (2017-05-16)

### Enhancements

* [ObjectServer] Added support for `SyncUser.isAdmin()` (#4353).
* [ObjectServer] New set of Permission API's have been added to `SyncUser` through `SyncUser.getPermissionManager()` (#4296).
* [ObjectServer] Added support for changing passwords through `SyncUser.changePassword()` (#4423).
* [ObjectServer] Added support for `SyncConfiguration.Builder.waitForInitialRemoteData()` (#4270).
* Transient fields are now allowed in model classes, but are implicitly treated as having the `@Ignore` annotation (#4279).
* Added `Realm.refresh()` and `DynamicRealm.refresh()` (#3476).
* Added `Realm.getInstanceAsync()` and `DynamicRealm.getInstanceAsync()` (#2299).
* Added `DynamicRealmObject#linkingObjects(String,String)` to support linking objects on `DynamicRealm` (#4492).
* Added support for read only Realms using `RealmConfiguration.Builder.readOnly()` and `SyncConfiguration.Builder.readOnly()`(#1147).
* Change listeners will now auto-expand variable names to be more descriptive when using Android Studio.
* The `toString()` methods for the standard and dynamic proxies now print "proxy", or "dynamic" before the left bracket enclosing the data.

### Bug Fixes

* `@LinkingObjects` annotation now also works with Kotlin (#4611).

### Internal

* Use separated locks for different `RealmCache`s (#4551).

## 3.1.4 (2017-05-04)

## Bug fixes

* Added missing row validation check in certain cases on invalidated/deleted objects (#4540).
* Initializing Realm is now more resilient if `Context.getFilesDir()` isn't working correctly (#4493).
* `OrderedRealmCollectionSnapshot.get()` returned a wrong object (#4554).
* `onSuccess` callback got triggered infinitely if a synced transaction was committed in the async transaction's `onSuccess` callback (#4594).

## 3.1.3 (2017-04-20)

### Enhancements

* [ObjectServer] Resume synchronization as soon as the connectivity is back (#4141).

### Bug Fixes

* `equals()` and `hashCode()` of managed `RealmObject`s that come from linking objects don't work correctly (#4487).
* Field name was missing in exception message when `null` was set to required field (#4484).
* Now throws `IllegalStateException` when a getter of linking objects is called against deleted or not yet loaded `RealmObject`s (#4499).
* `NullPointerException` caused by local transaction inside the listener of `findFirstAsync()`'s results (#4495).
* Native crash when adding listeners to `RealmObject` after removing listeners from the same `RealmObject` before (#4502).
* Native crash with "Invalid argument" error happened on some Android 7.1.1 devices when opening Realm on external storage (#4461).
* `OrderedRealmCollectionChangeListener` didn't report change ranges correctly when circular link's field changed (#4474).

### Internal

* Upgraded to Realm Sync 1.6.0.
* Upgraded to Realm Core 2.6.1.

## 3.1.2 (2017-04-12)

### Bug Fixes

* Crash caused by JNI couldn't find `OsObject.notifyChangeListeners` when ProGuard is enabled (#4461).
* Incompatible return type of `RealmSchema.getAll()` and `BaseRealm.getSchema()` (#4443).
* Memory leaked when synced Realm was initialized (#4465).
* An `IllegalStateException` will be thrown when starting iterating `OrderedRealmCollection` if the Realm is closed (#4471).

## 3.1.1 (2017-04-07)

### Bug Fixes

* Crash caused by Listeners on `RealmObject` getting triggered the 2nd time with different changed field (#4437).
* Unintentionally exposing `StandardRealmSchema` (#4443).
* Workaround for crashes on specific Samsung devices which are caused by a buggy `memmove` call (#3651).

## 3.1.0 (2017-04-05)

### Breaking Changes

* Updated file format of Realm files. Existing Realm files will automatically be migrated to the new format when they are opened, but older versions of Realm cannot open these files.
* [ObjectServer] Due to file format changes, Realm Object Server 1.3.0 or later is required.

### Enhancements

* Added support for reverse relationships through the `@LinkingObjects` annotation. See `io.realm.annotations.LinkingObjects` for documentation.  
  * This feature is in `@Beta`.
  * Queries on linking objects do not work.  Queries like `where(...).equalTo("field.linkingObjects.id", 7).findAll()` are not yet supported.
  * Backlink verification is incomplete.  Evil code can cause native crashes.
* The listener on `RealmObject` will only be triggered if the object changes (#3894).
* Added `RealmObjectChangeListener` interface that provide detailed information about `RealmObject` field changes.
* Listeners on `RealmList` and `RealmResults` will be triggered immediately when the transaction is committed on the same thread (#4245).
* The real `RealmMigrationNeededException` is now thrown instead of `IllegalArgumentException` if no migration is provided for a Realm that requires it.
* `RealmQuery.distinct()` can be performed on unindexed fields (#2285).
* `targetSdkVersion` is now 25.
* [ObjectServer] In case of a Client Reset, information about the location of the backed up Realm file is now reported through the `ErrorHandler` interface (#4080).
* [ObjectServer] Authentication URLs now automatically append `/auth` if no other path segment is set (#4370).

### Bug Fixes

* Crash with `LogicError` with `Bad version number` on notifier thread (#4369).
* `Realm.migrateRealm(RealmConfiguration)` now fails correctly with an `IllegalArgumentException` if a `SyncConfiguration` is provided (#4075).
* Potential cause for Realm file corruptions (never reported).
* Add `@Override` annotation to proxy class accessors and stop using raw type in proxy classes in order to remove warnings from javac (#4329).
* `findFirstAsync()` now returns an invalid object if there is no object matches the query condition instead of running the query repeatedly until it can find one (#4352).
* [ObjectServer] Changing the log level after starting a session now works correctly (#4337).

### Internal

* Using the Object Store's Session and SyncManager.
* Upgraded to Realm Sync 1.5.0.
* Upgraded to Realm Core 2.5.1.
* Upgraded Gradle to 3.4.1

## 3.0.0 (2017-02-28)

### Breaking Changes

* `RealmResults.distinct()` returns a new `RealmResults` object instead of filtering on the original object (#2947).
* `RealmResults` is auto-updated continuously. Any transaction on the current thread which may have an impact on the order or elements of the `RealmResults` will change the `RealmResults` immediately instead of change it in the next event loop. The standard `RealmResults.iterator()` will continue to work as normal, which means that you can still delete or modify elements without impacting the iterator. The same is not true for simple for-loops. In some cases a simple for-loop will not work (https://realm.io/docs/java/3.0.0/api/io/realm/OrderedRealmCollection.html#loops), and you must use the new createSnapshot() method.
* `RealmChangeListener` on `RealmObject` will now also be triggered when the object is deleted. Use `RealmObject.isValid()` to check this state(#3138).
* `RealmObject.asObservable()` will now emit the object when it is deleted. Use `RealmObject.isValid()` to check this state (#3138).
* Removed deprecated classes `Logger` and `AndroidLogger` (#4050).

### Deprecated

* `RealmResults.removeChangeListeners()`. Use `RealmResults.removeAllChangeListeners()` instead.
* `RealmObject.removeChangeListeners()`. Use `RealmObject.removeAllChangeListeners()` instead.
* `RealmResults.distinct()` and `RealmResults.distinctAsync()`. Use `RealmQuery.distinct()` and `RealmQuery.distinctAsync()` instead.

### Enhancements

* Added support for sorting by link's field (#672).
* Added `OrderedRealmCollectionSnapshot` class and `OrderedRealmCollection.createSnapshot()` method. `OrderedRealmCollectionSnapshot` is useful when changing `RealmResults` or `RealmList` in simple loops.
* Added `OrderedRealmCollectionChangeListener` interface for supporting fine-grained collection notifications.
* Added support for ChangeListeners on `RealmList`.
* Added `RealmList.asObservable()`.

### Bug Fixes

* Element type checking in `DynamicRealmObject#setList()` (#4252).
* Now throws `IllegalStateException` instead of process crash when any of thread confined methods in `RealmQuery` is called from wrong thread (#4228).
* Now throws `IllegalStateException` when any of thread confined methods in `DynamicRealmObject` is called from wrong thread (#4258).

### Internal

* Use Object Store's `Results` as the backend for `RealmResults` (#3372).
  - Use Object Store's notification mechanism to trigger listeners.
  - Local commits triggers Realm global listener and `RealmObject` listener on current thread immediately instead of in the next event loop.


## 2.3.2 (2017-02-27)

### Bug fixes

* Log levels in JNI layer were all reported as "Error" (#4204).
* Encrypted realms can end up corrupted if many threads are reading and writing at the same time (#4128).
* "Read-only file system" exception when compacting Realm file on external storage (#4140).

### Internal

* Updated to Realm Sync v1.2.1.
* Updated to Realm Core v2.3.2.

### Enhancements

* Improved performance of getters and setters in proxy classes.


## 2.3.1 (2017-02-07)

### Enhancements

* [ObjectServer] The `serverUrl` given to `SyncConfiguration.Builder()` is now more lenient and will also accept only paths as argument (#4144).
* [ObjectServer] Add a timer to refresh periodically the access_token.

### Bug fixes

* NPE problem in SharedRealm.finalize() (#3730).
* `RealmList.contains()` and `RealmResults.contains()` now correctly use custom `equals()` method on Realm model classes.
* Build error when the project is using Kotlin (#4087).
* Bug causing classes to be replaced by classes already in Gradle's classpath (#3568).
* NullPointerException when notifying a single object that it changed (#4086).


## 2.3.0 (2017-01-19)

### Object Server API Changes

* Realm Sync v1.0.0 has been released, and Realm Mobile Platform is no longer considered in beta.
* Breaking change: Location of Realm files are now placed in `getFilesDir()/<userIdentifier>` instead of `getFilesDir()/`.
  This is done in order to support shared Realms among users, while each user retaining their own local copy.
* Breaking change: `SyncUser.all()` now returns Map instead of List.
* Breaking change: Added a default `UserStore` saving users in a Realm file (`RealmFileUserStore`).
* Breaking change: Added multi-user support to `UserStore`. Added `get(String)` and `remove(String)`, removed `remove()` and renamed `get()` to `getCurrent()`.
* Breaking change: Changed the order of arguments to `SyncCredentials.custom()` to match iOS: token, provider, userInfo.
* Added support for `PermissionOffer` and `PermissionOfferResponse` to `SyncUser.getManagementRealm()`.
* Exceptions thrown in error handlers are ignored but logged (#3559).
* Removed unused public constants in `SyncConfiguration` (#4047).
* Fixed bug, preventing Sync client to renew the access token (#4038) (#4039).
* Now `SyncUser.logout()` properly revokes tokens (#3639).

### Bug fixes

* Fixed native memory leak setting the value of a primary key (#3993).
* Activated Realm's annotation processor on connectedTest when the project is using kapt (#4008).
* Fixed "too many open files" issue (#4002).
* Added temporary work-around for bug crashing Samsung Tab 3 devices on startup (#3651).

### Enhancements

* Added `like` predicate for String fields (#3752).

### Internal

* Updated to Realm Sync v1.0.0.
* Added a Realm backup when receiving a Sync client reset message from the server.

## 2.2.2 (2017-01-16)

### Object Server API Changes (In Beta)

* Disabled `Realm.compactRealm()` when sync is enabled as it might corrupt the Realm (https://github.com/realm/realm-core/issues/2345).

### Bug fixes

* "operation not permitted" issue when creating Realm file on some devices' external storage (#3629).
* Crash on API 10 devices (#3726).
* `UnsatisfiedLinkError` caused by `pipe2` (#3945).
* Unrecoverable error with message "Try again" when the notification fifo is full (#3964).
* Realm migration wasn't triggered when the primary key definition was altered (#3966).
* Use phantom reference to solve the finalize time out issue (#2496).

### Enhancements

* All major public classes are now non-final. This is mostly a compromise to support Mockito. All protected fields/methods are still not considered part of the public API and can change without notice (#3869).
* All Realm instances share a single notification daemon thread.
* Fixed Java lint warnings with generated proxy classes (#2929).

### Internal

* Upgraded Realm Core to 2.3.0.
* Upgraded Realm Sync to 1.0.0-BETA-6.5.

## 2.2.1 (2016-11-12)

### Object Server API Changes (In Beta)

* Fixed `SyncConfiguration.toString()` so it now outputs a correct description instead of an empty string (#3787).

### Bug fixes

* Added version number to the native library, preventing ReLinker from accidentally loading old code (#3775).
* `Realm.getLocalInstanceCount(config)` throwing NullPointerException if called after all Realms have been closed (#3791).

## 2.2.0 (2016-11-12)

### Object Server API Changes (In Beta)

* Added support for `SyncUser.getManagementRealm()` and permission changes.

### Bug fixes

* Kotlin projects no longer create the `RealmDefaultModule` if no Realm model classes are present (#3746).
* Remove `includedescriptorclasses` option from ProGuard rule file in order to support built-in shrinker of Android Gradle Plugin (#3714).
* Unexpected `RealmMigrationNeededException` was thrown when a field was added to synced Realm.

### Enhancements

* Added support for the `annotationProcessor` configuration provided by Android Gradle Plugin 2.2.0 or later. Realm plugin adds its annotation processor to the `annotationProcessor` configuration instead of `apt` configuration if it is available and the `com.neenbedankt.android-apt` plugin is not used. In Kotlin projects, `kapt` is used instead of the `annotationProcessor` configuration (#3026).

## 2.1.1 (2016-10-27)

### Bug fixes

* Fixed a bug in `Realm.insert` and `Realm.insertOrUpdate` methods causing a `StackOverFlow` when you try to insert a cyclic graph of objects between Realms (#3732).

### Object Server API Changes (In Beta)

* Set default RxFactory to `SyncConfiguration`.

### Bug fixes

* ProGuard configuration introduced in 2.1.0 unexpectedly kept classes that did not have the @KeepMember annotation (#3689).

## 2.1.0 (2016-10-25)

### Breaking changes

* * `SecureUserStore` has been moved to its own GitHub repository: https://github.com/realm/realm-android-user-store
  See https://github.com/realm/realm-android-user-store/blob/master/README.md for further info on how to include it.


### Object Server API Changes (In Beta)

* Renamed `User` to `SyncUser`, `Credentials` to `SyncCredentials` and `Session` to `SyncSession` to align names with Cocoa.
* Removed `SyncManager.setLogLevel()`. Use `RealmLog.setLevel()` instead.
* `SyncUser.logout()` now correctly clears `SyncUser.currentUser()` (#3638).
* Missing ProGuard configuration for libraries used by Sync extension (#3596).
* Error handler was not called when sync session failed (#3597).
* Added `User.all()` that returns all known Realm Object Server users.
* Upgraded Realm Sync to 1.0.0-BETA-3.2

### Deprecated

* `Logger`. Use `RealmLogger` instead.
* `AndroidLogger`. The logger for Android is implemented in native code instead.

### Bug fixes

* The following were not kept by ProGuard: names of native methods not in the `io.realm.internal` package, names of classes used in method signature (#3596).
* Permission error when a database file was located on external storage (#3140).
* Memory leak when unsubscribing from a RealmResults/RealmObject RxJava Observable (#3552).

### Enhancements

* `Realm.compactRealm()` now works for encrypted Realms.
* Added `first(E defaultValue)` and `last(E defaultValue)` methods to `RealmList` and `RealmResult`. These methods will return the provided object instead of throwing an `IndexOutOfBoundsException` if the list is empty.
* Reduce transformer logger verbosity (#3608).
* `RealmLog.setLevel(int)` for setting the log level across all loggers.

### Internal

* Upgraded Realm Core to 2.1.3

### Credits

* Thanks to Max Furman (@maxfurman) for adding support for `first()` and `last()` default values.

## 2.0.2 (2016-10-06)

This release is not protocol-compatible with previous versions of the Realm Mobile Platform. The base library is still fully compatible.

### Bug fixes

* Build error when using Java 7 (#3563).

### Internal

* Upgraded Realm Core to 2.1.0
* Upgraded Realm Sync to 1.0.0-BETA-2.0.

## 2.0.1 (2016-10-05)

### Bug fixes

* `android.net.conn.CONNECTIVITY_CHANGE` broadcast caused `RuntimeException` if sync extension was disabled (#3505).
* `android.net.conn.CONNECTIVITY_CHANGE` was not delivered on Android 7 devices.
* `distinctAsync` did not respect other query parameters (#3537).
* `ConcurrentModificationException` from Gradle when building an application (#3501).

### Internal

* Upgraded to Realm Core 2.0.1 / Realm Sync 1.3-BETA

## 2.0.0 (2016-09-27)

This release introduces support for the Realm Mobile Platform!
See <https://realm.io/news/introducing-realm-mobile-platform/> for an overview of these great new features.

### Breaking Changes

* Files written by Realm 2.0 cannot be read by 1.x or earlier versions. Old files can still be opened.
* It is now required to call `Realm.init(Context)` before calling any other Realm API.
* Removed `RealmConfiguration.Builder(Context)`, `RealmConfiguration.Builder(Context, File)` and `RealmConfiguration.Builder(File)` constructors.
* `isValid()` now always returns `true` instead of `false` for unmanaged `RealmObject` and `RealmList`. This puts it in line with the behaviour of the Cocoa and .NET API's (#3101).
* armeabi is not supported anymore.
* Added new `RealmFileException`.
  - `IncompatibleLockFileException` has been removed and replaced by `RealmFileException` with kind `INCOMPATIBLE_LOCK_FILE`.
  - `RealmIOExcpetion` has been removed and replaced by `RealmFileException`.
* `RealmConfiguration.Builder.assetFile(Context, String)` has been renamed to `RealmConfiguration.Builder.assetFile(String)`.
* Object with primary key is now required to define it when the object is created. This means that `Realm.createObject(Class<E>)` and `DynamicRealm.createObject(String)` now throws `RealmException` if they are used to create an object with a primary key field. Use `Realm.createObject(Class<E>, Object)` or `DynamicRealm.createObject(String, Object)` instead.
* Importing from JSON without the primary key field defined in the JSON object now throws `IllegalArgumentException`.
* Now `Realm.beginTransaction()`, `Realm.executeTransaction()` and `Realm.waitForChange()` throw `RealmMigrationNeededException` if a remote process introduces incompatible schema changes (#3409).
* The primary key value of an object can no longer be changed after the object was created. Instead a new object must be created and all fields copied over.
* Now `Realm.createObject(Class)` and `Realm.createObject(Class,Object)` take the values from the model's fields and default constructor. Creating objects through the `DynamicRealm` does not use these values (#777).
* When `Realm.create*FromJson()`s create a new `RealmObject`, now they take the default values defined by the field itself and its default constructor for those fields that are not defined in the JSON object.

### Enhancements

* Added `realmObject.isManaged()`, `RealmObject.isManaged(obj)` and `RealmCollection.isManaged()` (#3101).
* Added `RealmConfiguration.Builder.directory(File)`.
* `RealmLog` has been moved to the public API. It is now possible to control which events Realm emit to Logcat. See the `RealmLog` class for more details.
* Typed `RealmObject`s can now continue to access their fields properly even though the schema was changed while the Realm was open (#3409).
* A `RealmMigrationNeededException` will be thrown with a cause to show the detailed message when a migration is needed and the migration block is not in the `RealmConfiguration`.


### Bug fixes

* Fixed a lint error in proxy classes when the 'minSdkVersion' of user's project is smaller than 11 (#3356).
* Fixed a potential crash when there were lots of async queries waiting in the queue.
* Fixed a bug causing the Realm Transformer to not transform field access in the model's constructors (#3361).
* Fixed a bug causing a build failure when the Realm Transformer adds accessors to a model class that was already transformed in other project (#3469).
* Fixed a bug causing the `NullPointerException` when calling getters/setters in the model's constructors (#2536).

### Internal

* Moved JNI build to CMake.
* Updated Realm Core to 2.0.0.
* Updated ReLinker to 1.2.2.

## 1.2.0 (2016-08-19)

### Bug fixes

* Throw a proper exception when operating on a non-existing field with the dynamic API (#3292).
* `DynamicRealmObject.setList` should only accept `RealmList<DynamicRealmObject>` (#3280).
* `DynamicRealmObject.getX(fieldName)` now throws a proper exception instead of a native crash when called with a field name of the wrong type (#3294).
* Fixed a concurrency crash which might happen when `Realm.executeTransactionAsync()` tried to call `onSucess` after the Realm was closed.

### Enhancements

* Added `RealmQuery.in()` for a comparison against multiple values.
* Added byte array (`byte[]`) support to `RealmQuery`'s `equalTo` and `notEqualTo` methods.
* Optimized internal caching of schema classes (#3315).

### Internal

* Updated Realm Core to 1.5.1.
* Improved sorting speed.
* Completely removed the `OptionalAPITransformer`.

### Credits

* Thanks to Brenden Kromhout (@bkromhout) for adding binary array support to `equalTo` and `notEqualTo`.

## 1.1.1 (2016-07-01)

### Bug fixes

* Fixed a wrong JNI method declaration which might cause "method not found" crash on some devices.
* Fixed a bug that `Error` in the background async thread is not forwarded to the caller thread.
* Fixed a crash when an empty `Collection` is passed to `insert()`/`insertOrUpdate()` (#3103).
* Fixed a bug that does not transfer the primary key when `RealmSchemaObject.setClassName()` is called to rename a class (#3118).
* Fixed bug in `Realm.insert` and `Realm.insertOrUpdate` methods causing a `RealmList` to be cleared when inserting a managed `RealmModel` (#3105).
* Fixed a concurrency allocation bug in storage engine which might lead to some random crashes.
* Bulk insertion now throws if it is not called in a transaction (#3173).
* The IllegalStateException thrown when accessing an empty RealmObject is now more meaningful (#3200).
* `insert()` now correctly throws an exception if two different objects have the same primary key (#3212).
* Blackberry Z10 throwing "Function not implemented" (#3178).
* Reduced the number of file descriptors used by Realm Core (#3197).
* Throw a proper `IllegalStateException` if a `RealmChangeListener` is used inside an IntentService (#2875).

### Enhancements

* The Realm Annotation processor no longer consumes the Realm annotations. Allowing other annotation processors to run.

### Internal

* Updated Realm Core to 1.4.2.
* Improved sorting speed.

## 1.1.0 (2016-06-30)

### Bug fixes

* A number of bug fixes in the storage engine related to memory management in rare cases when a Realm has been compacted.
* Disabled the optional API transformer since it has problems with DexGuard (#3022).
* `OnSuccess.OnSuccess()` might not be called with the correct Realm version for async transaction (#1893).
* Fixed a bug in `copyToRealm()` causing a cyclic dependency objects being duplicated.
* Fixed a build failure when model class has a conflicting name such as `Map`, `List`, `String`, ... (#3077).

### Enhancements

* Added `insert(RealmModel obj)`, `insertOrUpdate(RealmModel obj)`, `insert(Collection<RealmModel> collection)` and `insertOrUpdate(Collection<RealmModel> collection)` to perform batch inserts (#1684).
* Enhanced `Table.toString()` to show a PrimaryKey field details (#2903).
* Enabled ReLinker when loading a Realm from a custom path by adding a `RealmConfiguration.Builder(Context, File)` constructor (#2900).
* Changed `targetSdkVersion` of `realm-library` to 24.
* Logs warning if `DynamicRealm` is not closed when GC happens as it does for `Realm`.

### Deprecated

* `RealmConfiguration.Builder(File)`. Use `RealmConfiguration.Builder(Context, File)` instead.

### Internal

* Updated Realm Core to 1.2.0.

## 1.0.1 (2016-05-25)

### Bug fixes

* Fixed a crash when calling `Table.toString()` in debugger (#2429).
* Fixed a race condition which would cause some `RealmResults` to not be properly updated inside a `RealmChangeListener`. This could result in crashes when accessing items from those results (#2926/#2951).
* Revised `RealmResults.isLoaded()` description (#2895).
* Fixed a bug that could cause Realm to lose track of primary key when using `RealmObjectSchema.removeField()` and `RealmObjectSchema.renameField()` (#2829/#2926).
* Fixed a bug that prevented some devices from finding async related JNI methods correctly.
* Updated ProGuard configuration in order not to depend on Android's default configuration (#2972).
* Fixed a race condition between Realms notifications and other UI events. This could e.g. cause ListView to crash (#2990).
* Fixed a bug that allowed both `RealmConfiguration.Builder.assetFile()`/`deleteRealmIfMigrationNeeded()` to be configured at the same time, which leads to the asset file accidentally being deleted in migrations (#2933).
* Realm crashed outright when the same Realm file was opened in two processes. Realm will now optimistically retry opening for 1 second before throwing an Error (#2459).

### Enhancements

* Removes RxJava related APIs during bytecode transforming to make RealmObject plays well with reflection when rx.Observable doesn't exist.

## 1.0.0 (2016-05-25)

No changes since 0.91.1.

## 0.91.1 (2016-05-25)

* Updated Realm Core to 1.0.1.

### Bug fixes

* Fixed a bug when opening a Realm causes a staled memory mapping. Symptoms are error messages like "Bad or incompatible history type", "File format version doesn't match", and "Encrypted interprocess sharing is currently unsupported".

## 0.91.0 (2016-05-20)

* Updated Realm Core to 1.0.0.

### Breaking changes

* Removed all `@Deprecated` methods.
* Calling `Realm.setAutoRefresh()` or `DynamicRealm.setAutoRefresh()` from non-Looper thread throws `IllegalStateException` even if the `autoRefresh` is false (#2820).

### Bug fixes

* Calling RealmResults.deleteAllFromRealm() might lead to native crash (#2759).
* The annotation processor now correctly reports an error if trying to reference interfaces in model classes (#2808).
* Added null check to `addChangeListener` and `removeChangeListener` in `Realm` and `DynamicRealm` (#2772).
* Calling `RealmObjectSchema.addPrimaryKey()` adds an index to the primary key field, and calling `RealmObjectSchema.removePrimaryKey()` removes the index from the field (#2832).
* Log files are not deleted when calling `Realm.deleteRealm()` (#2834).

### Enhancements

* Upgrading to OpenSSL 1.0.1t. From July 11, 2016, Google Play only accept apps using OpenSSL 1.0.1r or later (https://support.google.com/faqs/answer/6376725, #2749).
* Added support for automatically copying an initial database from assets using `RealmConfiguration.Builder.assetFile()`.
* Better error messages when certain file operations fail.

### Credits

* Paweł Surówka (@thesurix) for adding the `RealmConfiguration.Builder.assetFile()`.

## 0.90.1

* Updated Realm Core to 0.100.2.

### Bug fixes

* Opening a Realm while closing a Realm in another thread could lead to a race condition.
* Automatic migration to the new file format could in rare circumstances lead to a crash.
* Fixing a race condition that may occur when using Async API (#2724).
* Fixed CannotCompileException when related class definition in android.jar cannot be found (#2703).

### Enhancements

* Prints path when file related exceptions are thrown.

## 0.90.0

* Updated Realm Core to 0.100.0.

### Breaking changes

* RealmChangeListener provides the changed object/Realm/collection as well (#1594).
* All JSON methods on Realm now only wraps JSONException in RealmException. All other Exceptions are thrown as they are.
* Marked all methods on `RealmObject` and all public classes final (#1594).
* Removed `BaseRealm` from the public API.
* Removed `HandlerController` from the public API.
* Removed constructor of `RealmAsyncTask` from the public API (#1594).
* `RealmBaseAdapter` has been moved to its own GitHub repository: https://github.com/realm/realm-android-adapters
  See https://github.com/realm/realm-android-adapters/blob/master/README.md for further info on how to include it.
* File format of Realm files is changed. Files will be automatically upgraded but opening a Realm file with older
  versions of Realm is not possible.

### Deprecated

* `Realm.allObjects*()`. Use `Realm.where(clazz).findAll*()` instead.
* `Realm.distinct*()`. Use `Realm.where(clazz).distinct*()` instead.
* `DynamicRealm.allObjects*()`. Use `DynamicRealm.where(className).findAll*()` instead.
* `DynamicRealm.distinct*()`. Use `DynamicRealm.where(className).distinct*()` instead.
* `Realm.allObjectsSorted(field, sort, field, sort, field, sort)`. Use `RealmQuery.findAllSorted(field[], sort[])`` instead.
* `RealmQuery.findAllSorted(field, sort, field, sort, field, sort)`. Use `RealmQuery.findAllSorted(field[], sort[])`` instead.
* `RealmQuery.findAllSortedAsync(field, sort, field, sort, field, sort)`. Use `RealmQuery.findAllSortedAsync(field[], sort[])`` instead.
* `RealmConfiguration.setModules()`. Use `RealmConfiguration.modules()` instead.
* `Realm.refresh()` and `DynamicRealm.refresh()`. Use `Realm.waitForChange()`/`stopWaitForChange()` or `DynamicRealm.waitForChange()`/`stopWaitForChange()` instead.

### Enhancements

* `RealmObjectSchema.getPrimaryKey()` (#2636).
* `Realm.createObject(Class, Object)` for creating objects with a primary key directly.
* Unit tests in Android library projects now detect Realm model classes.
* Better error message if `equals()` and `hashCode()` are not properly overridden in custom Migration classes.
* Expanding the precision of `Date` fields to cover full range (#833).
* `Realm.waitForChange()`/`stopWaitForChange()` and `DynamicRealm.waitForChange()`/`stopWaitForChange()` (#2386).

### Bug fixes

* `RealmChangeListener` on `RealmObject` is not triggered when adding listener on returned `RealmObject` of `copyToRealmOrUpdate()` (#2569).

### Credits

* Thanks to Brenden Kromhout (@bkromhout) for adding `RealmObjectSchema.getPrimaryKey()`.

## 0.89.1

### Bug fixes

* @PrimaryKey + @Required on String type primary key no longer throws when using copyToRealm or copyToRealmOrUpdate (#2653).
* Primary key is cleared/changed when calling RealmSchema.remove()/RealmSchema.rename() (#2555).
* Objects implementing RealmModel can be used as a field of RealmModel/RealmObject (#2654).

## 0.89.0

### Breaking changes

* @PrimaryKey field value can now be null for String, Byte, Short, Integer, and Long types. Older Realms should be migrated, using RealmObjectSchema.setNullable(), or by adding the @Required annotation (#2515).
* `RealmResults.clear()` now throws UnsupportedOperationException. Use `RealmResults.deleteAllFromRealm()` instead.
* `RealmResults.remove(int)` now throws UnsupportedOperationException. Use `RealmResults.deleteFromRealm(int)` instead.
* `RealmResults.sort()` and `RealmList.sort()` now return the sorted result instead of sorting in-place.
* `RealmList.first()` and `RealmList.last()` now throw `ArrayIndexOutOfBoundsException` if `RealmList` is empty.
* Removed deprecated method `Realm.getTable()` from public API.
* `Realm.refresh()` and `DynamicRealm.refresh()` on a Looper no longer have any effect. `RealmObject` and `RealmResults` are always updated on the next event loop.

### Deprecated

* `RealmObject.removeFromRealm()` in place of `RealmObject.deleteFromRealm()`
* `Realm.clear(Class)` in favour of `Realm.delete(Class)`.
* `DynamicRealm.clear(Class)` in place of `DynamicRealm.delete(Class)`.

### Enhancements

* Added a `RealmModel` interface that can be used instead of extending `RealmObject`.
* `RealmCollection` and `OrderedRealmCollection` interfaces have been added. `RealmList` and `RealmResults` both implement these.
* `RealmBaseAdapter` now accept an `OrderedRealmCollection` instead of only `RealmResults`.
* `RealmObjectSchema.isPrimaryKey(String)` (#2440)
* `RealmConfiguration.initialData(Realm.Transaction)` can now be used to populate a Realm file before it is used for the first time.

### Bug fixes

* `RealmObjectSchema.isRequired(String)` and `RealmObjectSchema.isNullable(String)` don't throw when the given field name doesn't exist.

### Credits

* Thanks to @thesurix for adding `RealmConfiguration.initialData()`.

## 0.88.3

* Updated Realm Core to 0.97.3.

### Enhancements

* Throws an IllegalArgumentException when calling Realm.copyToRealm()/Realm.copyToRealmOrUpdate() with a RealmObject which belongs to another Realm instance in a different thread.
* Improved speed of cleaning up native resources (#2496).

### Bug fixes

* Field annotated with @Ignored should not have accessors generated by the bytecode transformer (#2478).
* RealmResults and RealmObjects can no longer accidentially be GC'ed if using `asObservable()`. Previously this caused the observable to stop emitting (#2485).
* Fixed an build issue when using Realm in library projects on Windows (#2484).
* Custom equals(), toString() and hashCode() are no longer incorrectly overwritten by the proxy class (#2545).

## 0.88.2

* Updated Realm Core to 0.97.2.

### Enhancements

* Outputs additional information when incompatible lock file error occurs.

### Bug fixes

* Race condition causing BadVersionException when running multiple async writes and queries at the same time (#2021/#2391/#2417).

## 0.88.1

### Bug fixes

* Prevent throwing NullPointerException in RealmConfiguration.equals(RealmConfiguration) when RxJava is not in the classpath (#2416).
* RealmTransformer fails because of missing annotation classes in user's project (#2413).
* Added SONAME header to shared libraries (#2432).
* now DynamicRealmObject.toString() correctly shows null value as "null" and the format is aligned to the String from typed RealmObject (#2439).
* Fixed an issue occurring while resolving ReLinker in apps using a library based on Realm (#2415).

## 0.88.0 (2016-03-10)

* Updated Realm Core to 0.97.0.

### Breaking changes

* Realm has now to be installed as a Gradle plugin.
* DynamicRealm.executeTransaction() now directly throws any RuntimeException instead of wrapping it in a RealmException (#1682).
* DynamicRealm.executeTransaction() now throws IllegalArgumentException instead of silently accepting a null Transaction object.
* String setters now throw IllegalArgumentException instead of RealmError for invalid surrogates.
* DynamicRealm.distinct()/distinctAsync() and Realm.distinct()/distinctAsync() now throw IllegalArgumentException instead of UnsupportedOperationException for invalid type or unindexed field.
* All thread local change listeners are now delayed until the next Looper event instead of being triggered when committing.
* Removed RealmConfiguration.getSchemaMediator() from public API which was deprecated in 0.86.0. Please use RealmConfiguration.getRealmObjectClasses() to obtain the set of model classes (#1797).
* Realm.migrateRealm() throws a FileNotFoundException if the Realm file doesn't exist.
* It is now required to unsubscribe from all Realm RxJava observables in order to fully close the Realm (#2357).

### Deprecated

* Realm.getInstance(Context). Use Realm.getInstance(RealmConfiguration) or Realm.getDefaultInstance() instead.
* Realm.getTable(Class) which was public because of the old migration API. Use Realm.getSchema() or DynamicRealm.getSchema() instead.
* Realm.executeTransaction(Transaction, Callback) and replaced it with Realm.executeTransactionAsync(Transaction), Realm.executeTransactionAsync(Transaction, OnSuccess), Realm.executeTransactionAsync(Transaction, OnError) and Realm.executeTransactionAsync(Transaction, OnSuccess, OnError).

### Enhancements

* Support for custom methods, custom logic in accessors, custom accessor names, interface implementation and public fields in Realm objects (#909).
* Support to project Lombok (#502).
* RealmQuery.isNotEmpty() (#2025).
* Realm.deleteAll() and RealmList.deleteAllFromRealm() (#1560).
* RealmQuery.distinct() and RealmResults.distinct() (#1568).
* RealmQuery.distinctAsync() and RealmResults.distinctAsync() (#2118).
* Improved .so loading by using [ReLinker](https://github.com/KeepSafe/ReLinker).
* Improved performance of RealmList#contains() (#897).
* distinct(...) for Realm, DynamicRealm, RealmQuery, and RealmResults can take multiple parameters (#2284).
* "realm" and "row" can be used as field name in model classes (#2255).
* RealmResults.size() now returns Integer.MAX_VALUE when actual size is greater than Integer.MAX_VALUE (#2129).
* Removed allowBackup from AndroidManifest (#2307).

### Bug fixes

* Error occurring during test and (#2025).
* Error occurring during test and connectedCheck of unit test example (#1934).
* Bug in jsonExample (#2092).
* Multiple calls of RealmResults.distinct() causes to return wrong results (#2198).
* Calling DynamicRealmObject.setList() with RealmList<DynamicRealmObject> (#2368).
* RealmChangeListeners did not triggering correctly if findFirstAsync() didn't find any object. findFirstAsync() Observables now also correctly call onNext when the query completes in that case (#2200).
* Setting a null value to trigger RealmChangeListener (#2366).
* Preventing throwing BadVersionException (#2391).

### Credits

* Thanks to Bill Best (@wmbest2) for snapshot testing.
* Thanks to Graham Smith (@grahamsmith) for a detailed bug report (#2200).

## 0.87.5 (2016-01-29)
* Updated Realm Core to 0.96.2.
  - IllegalStateException won't be thrown anymore in RealmResults.where() if the RealmList which the RealmResults is created on has been deleted. Instead, the RealmResults will be treated as empty forever.
  - Fixed a bug causing a bad version exception, when using findFirstAsync (#2115).

## 0.87.4 (2016-01-28)
* Updated Realm Core to 0.96.0.
  - Fixed bug causing BadVersionException or crashing core when running async queries.

## 0.87.3 (2016-01-25)
* IllegalArgumentException is now properly thrown when calling Realm.copyFromRealm() with a DynamicRealmObject (#2058).
* Fixed a message in IllegalArgumentException thrown by the accessors of DynamicRealmObject (#2141).
* Fixed RealmList not returning DynamicRealmObjects of the correct underlying type (#2143).
* Fixed potential crash when rolling back removal of classes that reference each other (#1829).
* Updated Realm Core to 0.95.8.
  - Fixed a bug where undetected deleted object might lead to seg. fault (#1945).
  - Better performance when deleting objects (#2015).

## 0.87.2 (2016-01-08)
* Removed explicit GC call when committing a transaction (#1925).
* Fixed a bug when RealmObjectSchema.addField() was called with the PRIMARY_KEY modifier, the field was not set as a required field (#2001).
* Fixed a bug which could throw a ConcurrentModificationException in RealmObject's or RealmResults' change listener (#1970).
* Fixed RealmList.set() so it now correctly returns the old element instead of the new (#2044).
* Fixed the deployment of source and javadoc jars (#1971).

## 0.87.1 (2015-12-23)
* Upgraded to NDK R10e. Using gcc 4.9 for all architectures.
* Updated Realm Core to 0.95.6
  - Fixed a bug where an async query can be copied incomplete in rare cases (#1717).
* Fixed potential memory leak when using async query.
* Added a check to prevent removing a RealmChangeListener from a non-Looper thread (#1962). (Thank you @hohnamkung.)

## 0.87.0 (2015-12-17)
* Added Realm.asObservable(), RealmResults.asObservable(), RealmObject.asObservable(), DynamicRealm.asObservable() and DynamicRealmObject.asObservable().
* Added RealmConfiguration.Builder.rxFactory() and RxObservableFactory for custom RxJava observable factory classes.
* Added Realm.copyFromRealm() for creating detached copies of Realm objects (#931).
* Added RealmObjectSchema.getFieldType() (#1883).
* Added unitTestExample to showcase unit and instrumentation tests. Examples include jUnit3, jUnit4, Espresso, Robolectric, and MPowermock usage with Realm (#1440).
* Added support for ISO8601 based dates for JSON import. If JSON dates are invalid a RealmException will be thrown (#1213).
* Added APK splits to gridViewExample (#1834).

## 0.86.1 (2015-12-11)
* Improved the performance of removing objects (RealmResults.clear() and RealmResults.remove()).
* Updated Realm Core to 0.95.5.
* Updated ProGuard configuration (#1904).
* Fixed a bug where RealmQuery.findFirst() returned a wrong result if the RealmQuery had been created from a RealmResults.where() (#1905).
* Fixed a bug causing DynamicRealmObject.getObject()/setObject() to use the wrong class (#1912).
* Fixed a bug which could cause a crash when closing Realm instances in change listeners (#1900).
* Fixed a crash occurring during update of multiple async queries (#1895).
* Fixed listeners not triggered for RealmObject & RealmResults created using copy or create methods (#1884).
* Fixed RealmChangeListener never called inside RealmResults (#1894).
* Fixed crash when calling clear on a RealmList (#1886).

## 0.86.0 (2015-12-03)
* BREAKING CHANGE: The Migration API has been replaced with a new API.
* BREAKING CHANGE: RealmResults.SORT_ORDER_ASCENDING and RealmResults.SORT_ORDER_DESCENDING constants have been replaced by Sort.ASCENDING and Sort.DESCENDING enums.
* BREAKING CHANGE: RealmQuery.CASE_SENSITIVE and RealmQuery.CASE_INSENSITIVE constants have been replaced by Case.SENSITIVE and Case.INSENSITIVE enums.
* BREAKING CHANGE: Realm.addChangeListener, RealmObject.addChangeListener and RealmResults.addChangeListener hold a strong reference to the listener, you should unregister the listener to avoid memory leaks.
* BREAKING CHANGE: Removed deprecated methods RealmQuery.minimum{Int,Float,Double}, RealmQuery.maximum{Int,Float,Double}, RealmQuery.sum{Int,Float,Double} and RealmQuery.average{Int,Float,Double}. Use RealmQuery.min(), RealmQuery.max(), RealmQuery.sum() and RealmQuery.average() instead.
* BREAKING CHANGE: Removed RealmConfiguration.getSchemaMediator() which is public by mistake. And RealmConfiguration.getRealmObjectClasses() is added as an alternative in order to obtain the set of model classes (#1797).
* BREAKING CHANGE: Realm.addChangeListener, RealmObject.addChangeListener and RealmResults.addChangeListener will throw an IllegalStateException when invoked on a non-Looper thread. This is to prevent registering listeners that will not be invoked.
* BREAKING CHANGE: trying to access a property on an unloaded RealmObject obtained asynchronously will throw an IllegalStateException
* Added new Dynamic API using DynamicRealm and DynamicRealmObject.
* Added Realm.getSchema() and DynamicRealm.getSchema().
* Realm.createOrUpdateObjectFromJson() now works correctly if the RealmObject class contains a primary key (#1777).
* Realm.compactRealm() doesn't throw an exception if the Realm file is opened. It just returns false instead.
* Updated Realm Core to 0.95.3.
  - Fixed a bug where RealmQuery.average(String) returned a wrong value for a nullable Long/Integer/Short/Byte field (#1803).
  - Fixed a bug where RealmQuery.average(String) wrongly counted the null value for average calculation (#1854).

## 0.85.1 (2015-11-23)
* Fixed a bug which could corrupt primary key information when updating from a Realm version <= 0.84.1 (#1775).

## 0.85.0 (2016-11-19)
* BREAKING CHANGE: Removed RealmEncryptionNotSupportedException since the encryption implementation changed in Realm's underlying storage engine. Encryption is now supported on all devices.
* BREAKING CHANGE: Realm.executeTransaction() now directly throws any RuntimeException instead of wrapping it in a RealmException (#1682).
* BREAKING CHANGE: RealmQuery.isNull() and RealmQuery.isNotNull() now throw IllegalArgumentException instead of RealmError if the fieldname is a linked field and the last element is a link (#1693).
* Added Realm.isEmpty().
* Setters in managed object for RealmObject and RealmList now throw IllegalArgumentException if the value contains an invalid (unmanaged, removed, closed, from different Realm) object (#1749).
* Attempting to refresh a Realm while a transaction is in process will now throw an IllegalStateException (#1712).
* The Realm AAR now also contains the ProGuard configuration (#1767). (Thank you @skyisle.)
* Updated Realm Core to 0.95.
  - Removed reliance on POSIX signals when using encryption.

## 0.84.2
* Fixed a bug making it impossible to convert a field to become required during a migration (#1695).
* Fixed a bug making it impossible to read Realms created using primary keys and created by iOS (#1703).
* Fixed some memory leaks when an Exception is thrown (#1730).
* Fixed a memory leak when using relationships (#1285).
* Fixed a bug causing cached column indices to be cleared too soon (#1732).

## 0.84.1 (2015-10-28)
* Updated Realm Core to 0.94.4.
  - Fixed a bug that could cause a crash when running the same query multiple times.
* Updated ProGuard configuration. See [documentation](https://realm.io/docs/java/latest/#proguard) for more details.
* Updated Kotlin example to use 1.0.0-beta.
* Fixed warnings reported by "lint -Xlint:all" (#1644).
* Fixed a bug where simultaneous opening and closing a Realm from different threads might result in a NullPointerException (#1646).
* Fixed a bug which made it possible to externally modify the encryption key in a RealmConfiguration (#1678).

## 0.84.0 (2015-10-22)
* Added support for async queries and transactions.
* Added support for parsing JSON Dates with timezone information. (Thank you @LateralKevin.)
* Added RealmQuery.isEmpty().
* Added Realm.isClosed() method.
* Added Realm.distinct() method.
* Added RealmQuery.isValid(), RealmResults.isValid() and RealmList.isValid(). Each method checks whether the instance is still valid to use or not(for example, the Realm has been closed or any parent object has been removed).
* Added Realm.isInTransaction() method.
* Updated Realm Core to version 0.94.3.
  - Fallback for mremap() now work correctly on BlackBerry devices.
* Following methods in managed RealmList now throw IllegalStateException instead of native crash when RealmList.isValid() returns false: add(int,RealmObject), add(RealmObject)
* Following methods in managed RealmList now throw IllegalStateException instead of ArrayIndexOutOfBoundsException when RealmList.isValid() returns false: set(int,RealmObject), move(int,int), remove(int), get(int)
* Following methods in managed RealmList now throw IllegalStateException instead of returning 0/null when RealmList.isValid() returns false: clear(), removeAll(Collection), remove(RealmObject), first(), last(), size(), where()
* RealmPrimaryKeyConstraintException is now thrown instead of RealmException if two objects with same primary key are inserted.
* IllegalStateException is now thrown when calling Realm's clear(), RealmResults's remove(), removeLast(), clear() or RealmObject's removeFromRealm() from an incorrect thread.
* Fixed a bug affecting RealmConfiguration.equals().
* Fixed a bug in RealmQuery.isNotNull() which produced wrong results for binary data.
* Fixed a bug in RealmQuery.isNull() and RealmQuery.isNotNull() which validated the query prematurely.
* Fixed a bug where closed Realms were trying to refresh themselves resulting in a NullPointerException.
* Fixed a bug that made it possible to migrate open Realms, which could cause undefined behavior when querying, reading or writing data.
* Fixed a bug causing column indices to be wrong for some edge cases. See #1611 for details.

## 0.83.1 (2015-10-15)
* Updated Realm Core to version 0.94.1.
  - Fixed a bug when using Realm.compactRealm() which could make it impossible to open the Realm file again.
  - Fixed a bug, so isNull link queries now always return true if any part is null.

## 0.83 (2015-10-08)
* BREAKING CHANGE: Database file format update. The Realm file created by this version cannot be used by previous versions of Realm.
* BREAKING CHANGE: Removed deprecated methods and constructors from the Realm class.
* BREAKING CHANGE: Introduced boxed types Boolean, Byte, Short, Integer, Long, Float and Double. Added null support. Introduced annotation @Required to indicate a field is not nullable. String, Date and byte[] became nullable by default which means a RealmMigrationNeededException will be thrown if an previous version of a Realm file is opened.
* Deprecated methods: RealmQuery.minimum{Int,Float,Double}, RealmQuery.maximum{Int,Float,Double}. Use RealmQuery.min() and RealmQuery.max() instead.
* Added support for x86_64.
* Fixed an issue where opening the same Realm file on two Looper threads could potentially lead to an IllegalStateException being thrown.
* Fixed an issue preventing the call of listeners on refresh().
* Opening a Realm file from one thread will no longer be blocked by a transaction from another thread.
* Range restrictions of Date fields have been removed. Date fields now accepts any value. Milliseconds are still removed.

## 0.82.2 (2015-09-04)
* Fixed a bug which might cause failure when loading the native library.
* Fixed a bug which might trigger a timeout in Context.finalize().
* Fixed a bug which might cause RealmObject.isValid() to throw an exception if the object is deleted.
* Updated Realm core to version 0.89.9
  - Fixed a potential stack overflow issue which might cause a crash when encryption was used.
  - Embedded crypto functions into Realm dynamic lib to avoid random issues on some devices.
  - Throw RealmEncryptionNotSupportedException if the device doesn't support Realm encryption. At least one device type (HTC One X) contains system bugs that prevents Realm's encryption from functioning properly. This is now detected, and an exception is thrown when trying to open/create an encrypted Realm file. It's up to the application to catch this and decide if it's OK to proceed without encryption instead.

## 0.82.1 (2015-08-06)
* Fixed a bug where using the wrong encryption key first caused the right key to be seen as invalid.
* Fixed a bug where String fields were ignored when updating objects from JSON with null values.
* Fixed a bug when calling System.exit(0), the process might hang.

## 0.82 (2015-07-28)
* BREAKING CHANGE: Fields with annotation @PrimaryKey are indexed automatically now. Older schemas require a migration.
* RealmConfiguration.setModules() now accept ignore null values which Realm.getDefaultModule() might return.
* Trying to access a deleted Realm object throw throws a proper IllegalStateException.
* Added in-memory Realm support.
* Closing realm on another thread different from where it was created now throws an exception.
* Realm will now throw a RealmError when Realm's underlying storage engine encounters an unrecoverable error.
* @Index annotation can also be applied to byte/short/int/long/boolean/Date now.
* Fixed a bug where RealmQuery objects are prematurely garbage collected.
* Removed RealmQuery.between() for link queries.

## 0.81.1 (2015-06-22)
* Fixed memory leak causing Realm to never release Realm objects.

## 0.81 (2015-06-19)
* Introduced RealmModules for working with custom schemas in libraries and apps.
* Introduced Realm.getDefaultInstance(), Realm.setDefaultInstance(RealmConfiguration) and Realm.getInstance(RealmConfiguration).
* Deprecated most constructors. They have been been replaced by Realm.getInstance(RealmConfiguration) and Realm.getDefaultInstance().
* Deprecated Realm.migrateRealmAtPath(). It has been replaced by Realm.migrateRealm(RealmConfiguration).
* Deprecated Realm.deleteFile(). It has been replaced by Realm.deleteRealm(RealmConfiguration).
* Deprecated Realm.compactFile(). It has been replaced by Realm.compactRealm(RealmConfiguration).
* RealmList.add(), RealmList.addAt() and RealmList.set() now copy unmanaged objects transparently into Realm.
* Realm now works with Kotlin (M12+). (Thank you @cypressious.)
* Fixed a performance regression introduced in 0.80.3 occurring during the validation of the Realm schema.
* Added a check to give a better error message when null is used as value for a primary key.
* Fixed unchecked cast warnings when building with Realm.
* Cleaned up examples (remove old test project).
* Added checking for missing generic type in RealmList fields in annotation processor.

## 0.80.3 (2015-05-22)
* Calling Realm.copyToRealmOrUpdate() with an object with a null primary key now throws a proper exception.
* Fixed a bug making it impossible to open Realms created by Realm-Cocoa if a model had a primary key defined.
* Trying to using Realm.copyToRealmOrUpdate() with an object with a null primary key now throws a proper exception.
* RealmChangedListener now also gets called on the same thread that did the commit.
* Fixed bug where Realm.createOrUpdateWithJson() reset Date and Binary data to default values if not found in the JSON output.
* Fixed a memory leak when using RealmBaseAdapter.
* RealmBaseAdapter now allow RealmResults to be null. (Thanks @zaki50.)
* Fixed a bug where a change to a model class (`RealmList<A>` to `RealmList<B>`) would not throw a RealmMigrationNeededException.
* Fixed a bug where setting multiple RealmLists didn't remove the previously added objects.
* Solved ConcurrentModificationException thrown when addChangeListener/removeChangeListener got called in the onChange. (Thanks @beeender)
* Fixed duplicated listeners in the same realm instance. Trying to add duplicated listeners is ignored now. (Thanks @beeender)

## 0.80.2 (2015-05-04)
* Trying to use Realm.copyToRealmOrUpdate() with an object with a null primary key now throws a proper exception.
* RealmMigrationNeedException can now return the path to the Realm that needs to be migrated.
* Fixed bug where creating a Realm instance with a hashcode collision no longer returned the wrong Realm instance.
* Updated Realm Core to version 0.89.2
  - fixed bug causing a crash when opening an encrypted Realm file on ARM64 devices.

## 0.80.1 (2015-04-16)
* Realm.createOrUpdateWithJson() no longer resets fields to their default value if they are not found in the JSON input.
* Realm.compactRealmFile() now uses Realm Core's compact() method which is more failure resilient.
* Realm.copyToRealm() now correctly handles referenced child objects that are already in the Realm.
* The ARM64 binary is now properly a part of the Eclipse distribution package.
* A RealmMigrationExceptionNeeded is now properly thrown if @Index and @PrimaryKey are not set correctly during a migration.
* Fixed bug causing Realms to be cached even though they failed to open correctly.
* Added Realm.deleteRealmFile(File) method.
* Fixed bug causing queries to fail if multiple Realms has different field ordering.
* Fixed bug when using Realm.copyToRealm() with a primary key could crash if default value was already used in the Realm.
* Updated Realm Core to version 0.89.0
  - Improved performance for sorting RealmResults.
  - Improved performance for refreshing a Realm after inserting or modifying strings or binary data.
  - Fixed bug causing incorrect result when querying indexed fields.
  - Fixed bug causing corruption of string index when deleting an object where there are duplicate values for the indexed field.
  - Fixed bug causing a crash after compacting the Realm file.
* Added RealmQuery.isNull() and RealmQuery.isNotNull() for querying relationships.
* Fixed a potential NPE in the RealmList constructor.

## 0.80 (2015-03-11)
* Queries on relationships can be case sensitive.
* Fixed bug when importing JSONObjects containing NULL values.
* Fixed crash when trying to remove last element of a RealmList.
* Fixed bug crashing annotation processor when using "name" in model classes for RealmObject references
* Fixed problem occurring when opening an encrypted Realm with two different instances of the same key.
* Version checker no longer reports that updates are available when latest version is used.
* Added support for static fields in RealmObjects.
* Realm.writeEncryptedCopyTo() has been reenabled.

## 0.79.1 (2015-02-20)
* copyToRealm() no longer crashes on cyclic data structures.
* Fixed potential crash when using copyToRealmOrUpdate with an object graph containing a mix of elements with and without primary keys.

## 0.79 (2015-02-16)
* Added support for ARM64.
* Added RealmQuery.not() to negate a query condition.
* Added copyToRealmOrUpdate() and createOrUpdateFromJson() methods, that works for models with primary keys.
* Made the native libraries much smaller. Arm went from 1.8MB to 800KB.
* Better error reporting when trying to create or open a Realm file fails.
* Improved error reporting in case of missing accessors in model classes.
* Re-enabled RealmResults.remove(index) and RealmResults.removeLast().
* Primary keys are now supported through the @PrimaryKey annotation.
* Fixed error when instantiating a Realm with the wrong key.
* Throw an exception if deleteRealmFile() is called when there is an open instance of the Realm.
* Made migrations and compression methods synchronised.
* Removed methods deprecated in 0.76. Now Realm.allObjectsSorted() and RealmQuery.findAllSorted() need to be used instead.
* Reimplemented Realm.allObjectSorted() for better performance.

## 0.78 (2015-01-22)
* Added proper support for encryption. Encryption support is now included by default. Keys are now 64 bytes long.
* Added support to write an encrypted copy of a Realm.
* Realm no longer incorrectly warns that an instance has been closed too many times.
* Realm now shows a log warning if an instance is being finalized without being closed.
* Fixed bug causing Realms to be cached during a RealmMigration resulting in invalid realms being returned from Realm.getInstance().
* Updated core to 0.88.

## 0.77 (2015-01-16)
* Added Realm.allObjectsSorted() and RealmQuery.findAllSorted() and extending RealmResults.sort() for multi-field sorting.
* Added more logging capabilities at the JNI level.
* Added proper encryption support. NOTE: The key has been increased from 32 bytes to 64 bytes (see example).
* Added support for unmanaged objects and custom constructors.
* Added more precise imports in proxy classes to avoid ambiguous references.
* Added support for executing a transaction with a closure using Realm.executeTransaction().
* Added RealmObject.isValid() to test if an object is still accessible.
* RealmResults.sort() now has better error reporting.
* Fixed bug when doing queries on the elements of a RealmList, ie. like Realm.where(Foo.class).getBars().where().equalTo("name").
* Fixed bug causing refresh() to be called on background threads with closed Realms.
* Fixed bug where calling Realm.close() too many times could result in Realm not getting closed at all. This now triggers a log warning.
* Throw NoSuchMethodError when RealmResults.indexOf() is called, since it's not implemented yet.
* Improved handling of empty model classes in the annotation processor
* Removed deprecated static constructors.
* Introduced new static constructors based on File instead of Context, allowing to save Realm files in custom locations.
* RealmList.remove() now properly returns the removed object.
* Calling realm.close() no longer prevent updates to other open realm instances on the same thread.

## 0.76.0 (2014-12-19)
* RealmObjects can now be imported using JSON.
* Gradle wrapper updated to support Android Studio 1.0.
* Fixed bug in RealmObject.equals() so it now correctly compares two objects from the same Realm.
* Fixed bug in Realm crashing for receiving notifications after close().
* Realm class is now marked as final.
* Replaced concurrency example with a better thread example.
* Allowed to add/remove RealmChangeListeners in RealmChangeListeners.
* Upgraded to core 0.87.0 (encryption support, API changes).
* Close the Realm instance after migrations.
* Added a check to deny the writing of objects outside of a transaction.

## 0.75.1 (2014-12-03)
* Changed sort to be an in-place method.
* Renamed SORT_ORDER_DECENDING to SORT_ORDER_DESCENDING.
* Added sorting functionality to allObjects() and findAll().
* Fixed bug when querying a date column with equalTo(), it would act as lessThan()

## 0.75.0 (2014-11-28)
* Realm now implements Closeable, allowing better cleanup of native resources.
* Added writeCopyTo() and compactRealmFile() to write and compact a Realm to a new file.
* RealmObject.toString(), equals() and hashCode() now support models with cyclic references.
* RealmResults.iterator() and listIterator() now correctly iterates the results when using remove().
* Bug fixed in Exception text when field names was not matching the database.
* Bug fixed so Realm no longer throws an Exception when removing the last object.
* Bug fixed in RealmResults which prevented sub-querying.
* The Date type does not support millisecond resolution, and dates before 1901-12-13 and dates after 2038-01-19 are not supported on 32 bit systems.
* Fixed bug so Realm no longer throws an Exception when removing the last object.
* Fixed bug in RealmResults which prevented sub-querying.

## 0.74.0 (2014-11-19)
* Added support for more field/accessors naming conventions.
* Added case sensitive versions of string comparison operators equalTo and notEqualTo.
* Added where() to RealmList to initiate queries.
* Added verification of fields names in queries with links.
* Added exception for queries with invalid field name.
* Allow static methods in model classes.
* An exception will now be thrown if you try to move Realm, RealmResults or RealmObject between threads.
* Fixed a bug in the calculation of the maximum of date field in a RealmResults.
* Updated core to 0.86.0, fixing a bug in cancelling an empty transaction, and major query speedups with floats/doubles.
* Consistent handling of UTF-8 strings.
* removeFromRealm() now calls moveLastOver() which is faster and more reliable when deleting multiple objects.

## 0.73.1 (2014-11-05)
* Fixed a bug that would send infinite notifications in some instances.

## 0.73.0 (2014-11-04)
* Fixed a bug not allowing queries with more than 1024 conditions.
* Rewritten the notification system. The API did not change but it's now much more reliable.
* Added support for switching auto-refresh on and off (Realm.setAutoRefresh).
* Added RealmBaseAdapter and an example using it.
* Added deleteFromRealm() method to RealmObject.

## 0.72.0 (2014-10-27)
* Extended sorting support to more types: boolean, byte, short, int, long, float, double, Date, and String fields are now supported.
* Better support for Java 7 and 8 in the annotations processor.
* Better support for the Eclipse annotations processor.
* Added Eclipse support to the distribution folder.
* Added Realm.cancelTransaction() to cancel/abort/rollback a transaction.
* Added support for link queries in the form realm.where(Owner.class).equalTo("cat.age", 12).findAll().
* Faster implementation of RealmQuery.findFirst().
* Upgraded core to 0.85.1 (deep copying of strings in queries; preparation for link queries).

## 0.71.0 (2014-10-07)
* Simplified the release artifact to a single Jar file.
* Added support for Eclipse.
* Added support for deploying to Maven.
* Throw exception if nested transactions are used (it's not allowed).
* Javadoc updated.
* Fixed [bug in RealmResults](https://github.com/realm/realm-java/issues/453).
* New annotation @Index to add search index to a field (currently only supporting String fields).
* Made the annotations processor more verbose and strict.
* Added RealmQuery.count() method.
* Added a new example about concurrency.
* Upgraded to core 0.84.0.

## 0.70.1 (2014-09-30)
* Enabled unit testing for the realm project.
* Fixed handling of camel-cased field names.

## 0.70.0 (2014-09-29)
* This is the first public beta release.
