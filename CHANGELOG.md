## 1.1.1

### Bug fixes

* Fixed a wrong JNI method declaration which might cause "method not found" crash on some devices.
* Fixed a bug that `Error` in the background async thread is not forwared to the caller thread.
* Fixed a crash when an empty `Collection` is passed to `insert()`/`insertOrUpdate()` (#3103).
* Fixed a concurrency allocation bug in storage engine which might lead to some random crashes.

### Internal

* Updated Realm Core to 1.4.0.

## 1.1.0

### Bug fixes

* A number of bug fixes in the storage engine related to memory management in rare cases when a Realm has been compacted.
* Disabled the optional API transformer since it has problems with DexGuard (#3022).
* `OnSuccess.OnSuccess()` might not be called with the correct Realm version for async transaction (#1893).
* Fixed a bug in `copyToRealm()` causing a cyclic dependency objects being duplicated.
* Fixed a build failure when model class has a conflicting name such as `Map`, `List`, `String`, ... (#3077).

### Enhancements

* Added `insert(RealmModel obj)`, `insertOrUpdate(RealmModel obj)`, `insert(Collection<RealmModel> collection)` and `insertOrUpdate(Collection<RealmModel> collection)` to perform batch inserts (#1684).
* Enhanced `Table.toString()` to show a `PrimaryKey` field details (#2903).
* Enabled ReLinker when loading a Realm from a custom path by adding a `RealmConfiguration.Builder(Context, File)` constructor (#2900).
* Changed `targetSdkVersion` of `realm-library` to 24.
* Logs warning if `DynamicRealm` is not closed when GC happens as it does for `Realm`.

### Deprecated

* `RealmConfiguration.Builder(File)`. Use `RealmConfiguration.Builder(Context, File)` instead.

### Internal

* Updated Realm Core to 1.2.0.

## 1.0.1

### Bug fixes

* Fixed a crash when calling `Table.toString()` in debugger (#2429).
* Fixed a race condition which would cause some `RealmResults` to not be properly updated inside a `RealmChangeListener`. This could result in crashes when accessing items from those results (#2926/#2951).
* Revised `RealmResults.isLoaded()` description (#2895).
* Fixed a bug that could cause Realm to lose track of primary key when using `RealmObjectSchema.removeField()` and `RealmObjectSchema.renameField()` (#2829/#2926).
* Fixed a bug that prevented some devices from finding async related JNI methods correctly.
* Updated ProGuard configuration in order not to depend on Android's default configuration (#2972).
* Fixed a race condition between Realms notifications and other UI events. This could e.g. cause `ListView` to crash (#2990).
* Fixed a bug that allowed both `RealmConfiguration.Builder.assetFile()`/`deleteRealmIfMigrationNeeded()` to be configured at the same time, which leads to the asset file accidentally being deleted in migrations (#2933).
* Realm crashed outright when the same Realm file was opened in two processes. Realm will now optimistically retry opening for 1 second before throwing an `Error` (#2459).

### Enhancements

* Removes RxJava related APIs during bytecode transforming to make `RealmObject` plays well with reflection when rx.Observable doesn't exist.

## 1.0.0

No changes since 0.91.1.

## 0.91.1

### Internal

* Updated Realm Core to 1.0.1.

### Bug fixes

* Fixed a bug when opening a Realm causes a staled memory mapping. Symptoms are error messages like "Bad or incompatible history type", "File format version doesn't match", and "Encrypted interprocess sharing is currently unsupported".

## 0.91.0

### Internal

* Updated Realm Core to 1.0.0.

### Breaking changes

* Removed all `@Deprecated` methods.
* Calling `Realm.setAutoRefresh()` or `DynamicRealm.setAutoRefresh()` from non-Looper thread throws `IllegalStateException` even if the `autoRefresh` is false (#2820).

### Bug fixes

* Calling `RealmResults.deleteAllFromRealm()` might lead to native crash (#2759).
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

### Internal

* Updated Realm Core to 0.100.2.

### Bug fixes

* Opening a Realm while closing a Realm in another thread could lead to a race condition.
* Automatic migration to the new file format could in rare circumstances lead to a crash.
* Fixing a race condition that may occur when using Async API (#2724).
* Fixed `CannotCompileException` when related class definition in android.jar cannot be found (#2703).

### Enhancements

* Prints path when file related exceptions are thrown.

## 0.90.0

### Internal

* Updated Realm Core to 0.100.0.

### Breaking changes

* `RealmChangeListener` provides the changed object/`Realm`/collection as well (#1594).
* All JSON methods on `Realm` now only wraps `JSONException` in `RealmException`. All other `Exception`s are thrown as they are.
* Marked all methods on `RealmObject` and all public classes final (#1594).
* Removed `BaseRealm` from the public API.
* Removed `HandlerController` from the public API.
* Removed constructor of `RealmAsyncTask` from the public API (#1594).
* `RealmBaseAdapter` has been moved to its own [GitHub repository](https://github.com/realm/realm-android-adapters). See [its README](https://github.com/realm/realm-android-adapters/README.md) for further info on how to include it.
* File format of Realm files is changed. Files will be automatically upgraded but opening a Realm file with older versions of Realm is not possible.

### Deprecated

* `Realm.allObjects*()`. Use `Realm.where(clazz).findAll*()` instead.
* `Realm.distinct*()`. Use `Realm.where(clazz).distinct*()` instead.
* `DynamicRealm.allObjects*()`. Use `DynamicRealm.where(className).findAll*()` instead.
* `DynamicRealm.distinct*()`. Use `DynamicRealm.where(className).distinct*()` instead.
* `Realm.allObjectsSorted(field, sort, field, sort, field, sort)`. Use `RealmQuery.findAllSorted(field[], sort[])` instead.
* `RealmQuery.findAllSorted(field, sort, field, sort, field, sort)`. Use `RealmQuery.findAllSorted(field[], sort[])` instead.
* `RealmQuery.findAllSortedAsync(field, sort, field, sort, field, sort)`. Use `RealmQuery.findAllSortedAsync(field[], sort[])` instead.
* `RealmConfiguration.setModules()`. Use `RealmConfiguration.modules()` instead.
* `Realm.refresh()` and `DynamicRealm.refresh()`. Use `Realm.waitForChange()`/`stopWaitForChange()` or `DynamicRealm.waitForChange()`/`stopWaitForChange()` instead.

### Enhancements

* `RealmObjectSchema.getPrimaryKey()` (#2636).
* `Realm.createObject(Class, Object)` for creating objects with a primary key directly.
* Unit tests in Android library projects now detect Realm model classes.
* Better error message if `equals()` and `hashCode()` are not properly overridden in custom `Migration` classes.
* Expanding the precision of `Date` fields to cover full range (#833).
* `Realm.waitForChange()`/`stopWaitForChange()` and `DynamicRealm.waitForChange()`/`stopWaitForChange()` (#2386).

### Bug fixes

* `RealmChangeListener` on `RealmObject` is not triggered when adding listener on returned `RealmObject` of `copyToRealmOrUpdate()` (#2569).

### Credit

* Thanks to Brenden Kromhout (@bkromhout) for adding `RealmObjectSchema.getPrimaryKey()`.

## 0.89.1

### Bug fixes

* `@PrimaryKey` + `@Required` on `String` type primary key no longer throws when using `copyToRealm()` or `copyToRealmOrUpdate()` (#2653).
* Primary key is cleared/changed when calling `RealmSchema.remove()`/`RealmSchema.rename()` (#2555).
* Objects implementing `RealmModel` can be used as a field of `RealmModel`/`RealmObject` (#2654).

## 0.89.0

### Breaking changes

* `@PrimaryKey` field value can now be null for `String`, `Byte`, `Short`, `Integer`, and `Long` types. Older Realms should be migrated, using `RealmObjectSchema.setNullable()`, or by adding the `@Required` annotation. (#2515).
* `RealmResults.clear()` now throws `UnsupportedOperationException`. Use `RealmResults.deleteAllFromRealm()` instead.
* `RealmResults.remove(int)` now throws `UnsupportedOperationException`. Use `RealmResults.deleteFromRealm(int)` instead.
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

### Internal

* Updated Realm Core to 0.97.3.

### Enhancements

* Throws an `IllegalArgumentException` when calling `Realm.copyToRealm()`/`Realm.copyToRealmOrUpdate()` with a `RealmObject` which belongs to another `Realm` instance in a different thread.
* Improved speed of cleaning up native resources (#2496).

### Bug fixes

* Field annotated with @Ignored should not have accessors generated by the bytecode transformer (#2478).
* `RealmResults` and `RealmObjects` can no longer accidentially be GC'ed if using `asObservable()`. Previously this caused the observable to stop emitting. (#2485).
* Fixed an build issue when using Realm in library projects on Windows (#2484).
* Custom `equals()`, `toString()` and `hashCode()` are no longer incorrectly overwritten by the proxy class (#2545).

## 0.88.2

* Updated Realm Core to 0.97.2.

### Enhancements

* Outputs additional information when incompatible lock file error occurs.

### Bug fixes

* Race condition causing `BadVersionException` when running multiple async writes and queries at the same time (#2021/#2391/#2417).

## 0.88.1

### Bug fixes

* Prevent throwing `NullPointerException` in `RealmConfiguration.equals(RealmConfiguration)` when RxJava is not in the classpath (#2416).
* `RealmTransformer` fails because of missing annotation classes in user's project (#2413).
* Added SONAME header to shared libraries (#2432).
* now `DynamicRealmObject.toString()` correctly shows null value as "`null`" and the format is aligned to the `String` from typed `RealmObject` (#2439).
* Fixed an issue occurring while resolving ReLinker in apps using a library based on Realm (#2415).

## 0.88.0

### Internal

* Updated Realm Core to 0.97.0.

### Breaking changes

* Realm has now to be installed as a Gradle plugin.
* `DynamicRealm.executeTransaction()` now directly throws any `RuntimeException` instead of wrapping it in a `RealmException` (#1682).
* `DynamicRealm.executeTransaction()` now throws `IllegalArgumentException` instead of silently accepting a `null` `Transaction` object.
* `String` setters now throw `IllegalArgumentException` instead of `RealmError` for invalid surrogates.
* `DynamicRealm.distinct()`/`DynamicRealm.distinctAsync()` and `Realm.distinct()`/`Realm.distinctAsync()` now throw `IllegalArgumentException` instead of `UnsupportedOperationException` for invalid type or unindexed field.
* All thread local change listeners are now delayed until the next Looper event instead of being triggered when committing.
* Removed `RealmConfiguration.getSchemaMediator()` from public API which was deprecated in 0.86.0. Please use `RealmConfiguration.getRealmObjectClasses()` to obtain the set of model classes (#1797).
* `Realm.migrateRealm()` throws a `FileNotFoundException` if the Realm file doesn't exist.
* It is now required to unsubscribe from all Realm RxJava observables in order to fully close the Realm (#2357).

### Deprecated

* `Realm.getInstance(Context)`. Use `Realm.getInstance(RealmConfiguration)` or `Realm.getDefaultInstance()` instead.
* `Realm.getTable(Class)` which was public because of the old migration API. Use `Realm.getSchema()` or `DynamicRealm.getSchema()` instead.

### Enhancements

* Support for custom methods, custom logic in accessors, custom accessor names, interface implementation and public fields in `Realm` objects (#909).
* Support to project Lombok (#502).
* `RealmQuery.isNotEmpty()` (#2025).
* `Realm.deleteAll()` and `RealmList.deleteAllFromRealm()` (#1560).
* `RealmQuery.distinct()` and `RealmResults.distinct()` (#1568).
* `RealmQuery.distinctAsync()` and `RealmResults.distinctAsync()` (#2118).
* Improved .so loading by using [ReLinker](https://github.com/KeepSafe/ReLinker).
* Improved performance of `RealmList.contains()` (#897).
* `distinct(...)` for `Realm`, `DynamicRealm`, `RealmQuery`, and `RealmResults` can take multiple parameters (#2284).
* "`realm`" and "`row`" can be used as field name in model classes (#2255).
* `RealmResults.size()` now returns `Integer.MAX_VALUE` when actual size is greater than `Integer.MAX_VALUE` (#2129).
* Removed `allowBackup` from `AndroidManifest` (#2307).

### Bug fixes

* Error occurring during test and connectedCheck of unit test example (#1934).
* Bug in jsonExample (#2092).
* Multiple calls of `RealmResults.distinct()` causes to return wrong results (#2198).
* Calling `DynamicRealmObject.setList()` with `RealmList<DynamicRealmObject>` (#2368).
* `RealmChangeListeners` did not triggering correctly if `findFirstAsync()` didn't find any object. `findFirstAsync()` Observables now also correctly call onNext when the query completes in that case (#2200).
* Setting a `null` value to trigger `RealmChangeListener` (#2366).
* Preventing throwing `BadVersionException` (#2391).

### Credits

* Thanks to Bill Best (@wmbest2) for snapshot testing.
* Thanks to Graham Smith (@grahamsmith) for a detailed bug report (#2200).

## 0.87.5

### Internal

* Updated Realm Core to 0.96.2.
  - `IllegalStateException` won't be thrown anymore in `RealmResults.where()` if the `RealmList` which the `RealmResults` is created on has been deleted. Instead, the `RealmResults` will be treated as empty forever.
  - Fixed a bug causing a bad version exception, when using `findFirstAsync()` (#2115).

## 0.87.4

### Internal

* Updated Realm Core to 0.96.0.
  - Fixed bug causing `BadVersionException` or crashing core when running async queries.

## 0.87.3

### Internal

* Updated Realm Core to 0.95.8.
  - Fixed a bug where undetected deleted object might lead to seg. fault (#1945).
  - Better performance when deleting objects (#2015).

### Bug fixes

* `IllegalArgumentException` is now properly thrown when calling `Realm.copyFromRealm()` with a `DynamicRealmObject` (#2058).
* Fixed a message in `IllegalArgumentException` thrown by the accessors of `DynamicRealmObject` (#2141).
* Fixed `RealmList` not returning `DynamicRealmObjects` of the correct underlying type (#2143).
* Fixed potential crash when rolling back removal of classes that reference each other (#1829).

## 0.87.2

### Enhancements

* Removed explicit GC call when committing a transaction (#1925).

### Bug fixes

* Fixed a bug when `RealmObjectSchema.addField()` was called with the `PRIMARY_KEY` modifier, the field was not set as a required field (#2001).
* Fixed a bug which could throw a `ConcurrentModificationException` in `RealmObject`'s or `RealmResults`' change listener (#1970).
* Fixed `RealmList.set()` so it now correctly returns the old element instead of the new (#2044).
* Fixed the deployment of source and javadoc jars (#1971).

## 0.87.1

### Internal

* Upgraded to NDK R10e. Using gcc 4.9 for all architectures.
* Updated Realm Core to 0.95.6
  - Fixed a bug where an async query can be copied incomplete in rare cases (#1717).

### Bug fixes

* Fixed potential memory leak when using async query.
* Added a check to prevent removing a `RealmChangeListener` from a non-Looper thread (#1962). (Thank you @hohnamkung)

## 0.87.0

### Enhancements

* Added `Realm.asObservable()`, `RealmResults.asObservable()`, `RealmObject.asObservable()`, `DynamicRealm.asObservable()` and `DynamicRealmObject.asObservable()`.
* Added `RealmConfiguration.Builder.rxFactory()` and `RxObservableFactory` for custom RxJava observable factory classes.
* Added `Realm.copyFromRealm()` for creating detached copies of `Realm` objects (#931).
* Added `RealmObjectSchema.getFieldType()` (#1883).
* Added unitTestExample to showcase unit and instrumentation tests. Examples include jUnit3, jUnit4, Espresso, Robolectric, and MPowermock usage with Realm (#1440).
* Added support for ISO8601 based dates for JSON import. If JSON dates are invalid a RealmException will be thrown (#1213).
* Added APK splits to gridViewExample (#1834).

## 0.86.1

### Internal

* Updated Realm Core to 0.95.5.

### Enhancements

* Improved the performance of removing objects (`RealmResults.clear()` and `RealmResults.remove()`).
* Updated ProGuard configuration (#1904).

### Bug fixes

* Fixed a bug where `RealmQuery.findFirst()` returned a wrong result if the `RealmQuery` had been created from a `RealmResults.where()` (#1905).
* Fixed a bug causing `DynamicRealmObject.getObject()`/`DynamicRealmObject.setObject()` to use the wrong class (#1912).
* Fixed a bug which could cause a crash when closing Realm instances in change listeners (#1900).
* Fixed a crash occurring during update of multiple async queries (#1895).
* Fixed listeners not triggered for `RealmObject` & `RealmResults` created using copy or create methods (#1884).
* Fixed `RealmChangeListener` never called inside `RealmResults` (#1894).
* Fixed crash when calling clear on a `RealmList` (#1886).

## 0.86.0

### Internal

* Updated Realm Core to 0.95.3.
  - Fixed a bug where `RealmQuery.average(String)` returned a wrong value for a nullable `Long`/`Integer`/`Short`/`Byte` field (#1803).
  - Fixed a bug where `RealmQuery.average(String)` wrongly counted the `null` value for average calculation (#1854).

### Breaking changes

* The Migration API has been replaced with a new API.
* `RealmResults.SORT_ORDER_ASCENDING` and `RealmResults.SORT_ORDER_DESCENDING` constants have been replaced by `Sort.ASCENDING` and `Sort.DESCENDING` enums.
* `RealmQuery.CASE_SENSITIVE` and `RealmQuery.CASE_INSENSITIVE` constants have been replaced by `Case.SENSITIVE` and `Case.INSENSITIVE` enums.
* `Realm.addChangeListener()`, `RealmObject.addChangeListener()` and `RealmResults.addChangeListener()` hold a strong reference to the listener, you should unregister the listener to avoid memory leaks.
* Removed deprecated methods `RealmQuery.minimum{Int,Float,Double}()`, `RealmQuery.maximum{Int,Float,Double}()`, `RealmQuery.sum{Int,Float,Double}()` and `RealmQuery.average{Int,Float,Double}()`. Use `RealmQuery.min()`, `RealmQuery.max()`, `RealmQuery.sum()` and `RealmQuery.average()` instead.
* Removed `RealmConfiguration.getSchemaMediator()` which is public by mistake, and `RealmConfiguration.getRealmObjectClasses()` is added as an alternative in order to obtain the set of model classes (#1797).
* `Realm.addChangeListener`, `RealmObject.addChangeListener()` and `RealmResults.addChangeListener()` will throw an `IllegalStateException` when invoked on a non-Looper thread. This is to prevent registering listeners that will not be invoked.
* trying to access a property on an unloaded `RealmObject` obtained asynchronously will throw an `IllegalStateException`.

### Enhancements

* Added new Dynamic API using `DynamicRealm` and `DynamicRealmObject`.
* Added `Realm.getSchema()` and `DynamicRealm.getSchema()`.
* `Realm.createOrUpdateObjectFromJson()` now works correctly if the `RealmObject` class contains a primary key (#1777).
* `Realm.compactRealm()` doesn't throw an exception if the Realm file is opened. It just returns false instead.

## 0.85.1

### Bug fixes

* Fixed a bug which could corrupt primary key information when updating from a Realm version <= 0.84.1 (#1775).

## 0.85.0

### Breaking changes

* Removed `RealmEncryptionNotSupportedException` since the encryption implementation changed in Realm's underlying storage engine. Encryption is now supported on all devices.
* `Realm.executeTransaction()` now directly throws any `RuntimeException` instead of wrapping it in a `RealmException` (#1682).
* `RealmQuery.isNull()` and `RealmQuery.isNotNull()` now throw `IllegalArgumentException` instead of `RealmError` if the fieldname is a linked field and the last element is a link (#1693).

### Internal

* Updated Realm Core to 0.95.
  - Removed reliance on POSIX signals when using encryption.

### Enhancements

* Added `Realm.isEmpty()`.
* Setters in managed object for `RealmObject` and `RealmList` now throw `IllegalArgumentException` if the value contains an invalid (unmanaged, removed, closed, from different Realm) object (#1749).
* Attempting to refresh a Realm while a transaction is in process will now throw an `IllegalStateException` (#1712).
* The Realm AAR now also contains the ProGuard configuration (#1767). (Thank you @skyisle)

## 0.84.2

### Bug fixes

* Fixed a bug making it impossible to convert a field to become required during a migration (#1695).
* Fixed a bug making it impossible to read Realms created using primary keys and created by iOS (#1703).
* Fixed some memory leaks when an `Exception` is thrown (#1730).
* Fixed a memory leak when using relationships (#1285).
* Fixed a bug causing cached column indices to be cleared too soon (#1732).

## 0.84.1

### Internal

* Updated Realm Core to 0.94.4.
  - Fixed a bug that could cause a crash when running the same query multiple times.

### Enhancements

* Updated ProGuard configuration. See [documentation](https://realm.io/docs/java/latest/#proguard) for more details.
* Updated Kotlin example to use 1.0.0-beta.

### Bug fixes

* Fixed warnings reported by "lint -Xlint:all" (#1644).
* Fixed a bug where simultaneous opening and closing a Realm from different threads might result in a `NullPointerException` (#1646).
* Fixed a bug which made it possible to externally modify the encryption key in a `RealmConfiguration` (#1678).

## 0.84.0

### Internal

* Updated Realm Core to version 0.94.3.
  - Fallback for `mremap()` now work correctly on BlackBerry devices.

### Enhancements

* Added support for async queries and transactions.
* Added support for parsing JSON Dates with timezone information. (Thank you @LateralKevin)
* Added `RealmQuery.isEmpty()`.
* Added `Realm.isClosed()` method.
* Added `Realm.distinct()` method.
* Added `RealmQuery.isValid()`, `RealmResults.isValid()` and `RealmList.isValid()`. Each method checks whether the instance is still valid to use or not(for example, the Realm has been closed or any parent object has been removed).
* Added `Realm.isInTransaction()` method.
* Following methods in managed `RealmList` now throw `IllegalStateException` instead of native crash when `RealmList.isValid()` returns `false`: `add(int,RealmObject)`, `add(RealmObject)`.
* Following methods in managed `RealmList` now throw `IllegalStateException` instead of `ArrayIndexOutOfBoundsException` when `RealmList.isValid()` returns `false`: `set(int,RealmObject)`, `move(int,int)`, `remove(int)`, `get(int)`.
* Following methods in managed `RealmList` now throw `IllegalStateException` instead of returning `0`/`null` when `RealmList.isValid()` returns `false`: `clear()`, `removeAll(Collection)`, `remove(RealmObject)`, `first()`, `last()`, `size()`, `where()`.
* `RealmPrimaryKeyConstraintException` is now thrown instead of `RealmException` if two objects with same primary key are inserted.
* `IllegalStateException` is now thrown when calling Realm's `clear()`, `RealmResults`' `remove()`, `removeLast()`, `clear()` or `RealmObject's `removeFromRealm()` from an incorrect thread.

### Bug fixes

* Fixed a bug affecting `RealmConfiguration.equals()`.
* Fixed a bug in `RealmQuery.isNotNull()` which produced wrong results for binary data.
* Fixed a bug in `RealmQuery.isNull()` and `RealmQuery.isNotNull()` which validated the query prematurely.
* Fixed a bug where closed `Realm`s were trying to refresh themselves resulting in a `NullPointerException`.
* Fixed a bug that made it possible to migrate open Realms, which could cause undefined behavior when querying, reading or writing data.
* Fixed a bug causing column indices to be wrong for some edge cases. See #1611 for details.

## 0.83.1

### Internal

* Updated Realm Core to version 0.94.1.
  - Fixed a bug when using `Realm.compactRealm()` which could make it impossible to open the Realm file again.
  - Fixed a bug, so `isNull()` link queries now always return true if any part is `null`.

## 0.83

### Breaking changes

* Database file format update. The Realm file created by this version cannot be used by previous versions of Realm.
* Removed deprecated methods and constructors from the `Realm` class.
* Introduced boxed types `Boolean`, `Byte`, `Short`, `Integer`, `Long`, `Float` and `Double`. Added `null` support. Introduced annotation `@Required` to indicate a field is not nullable. `String`, `Date` and `byte[]` became nullable by default which means a `RealmMigrationNeededException` will be thrown if an previous version of a Realm file is opened.

### Enhancements

* Added support for x86_64.
* Opening a Realm file from one thread will no longer be blocked by a transaction from another thread.
* Range restrictions of `Date` fields have been removed. Date fields now accepts any value. Milliseconds are still removed.

### Deprecated

* `RealmQuery.minimum{Int,Float,Double}()`, `RealmQuery.maximum{Int,Float,Double}()`. Use `RealmQuery.min()` and `RealmQuery.max()` instead.

### Bug fixes

* Fixed an issue where opening the same Realm file on two Looper threads could potentially lead to an `IllegalStateException` being thrown.
* Fixed an issue preventing the call of listeners on `refresh()`.

## 0.82.2

### Internal

* Updated Realm core to version 0.89.9
  - Fixed a potential stack overflow issue which might cause a crash when encryption was used.
  - Embedded crypto functions into Realm dynamic lib to avoid random issues on some devices.
  - Throw `RealmEncryptionNotSupportedException` if the device doesn't support Realm encryption. At least one device type (HTC One X) contains system bugs that prevents Realm's encryption from functioning properly. This is now detected, and an exception is thrown when trying to open/create an encrypted Realm file. It's up to the application to catch this and decide if it's OK to proceed without encryption instead.

### Bug fixes

* Fixed a bug which might cause failure when loading the native library.
* Fixed a bug which might trigger a timeout in `Context.finalize()`.
* Fixed a bug which might cause `RealmObject.isValid()` to throw an exception if the object is deleted.

## 0.82.1

### Bug fixes

* Fixed a bug where using the wrong encryption key first caused the right key to be seen as invalid.
* Fixed a bug where `String` fields were ignored when updating objects from JSON with `null` values.
* Fixed a bug when calling `System.exit(0)`, the process might hang.

## 0.82.0

### Breaking changes

* Fields with annotation `@PrimaryKey` are indexed automatically now. Older schemas require a migration.

### Enhancements

* `RealmConfiguration.setModules()` now accept ignore null values which `Realm.getDefaultModule()` might return.
* Trying to access a deleted `Realm` object throw throws a proper `IllegalStateException`.
* Added in-memory Realm support.
* Closing Realm on another thread different from where it was created now throws an exception.
* Realm will now throw a `RealmError` when Realm's underlying storage engine encounters an unrecoverable error.
* `@Index` annotation can also be applied to `byte`/`short`/`int`/`long`/`boolean`/`Date` now.
* Removed `RealmQuery.between()` for link queries.

### Bug fixes

* Fixed a bug where `RealmQuery` objects are prematurely garbage collected.

## 0.81.1

## Bug fixes

* Fixed memory leak causing Realm to never release `Realm` objects.

## 0.81.0

### Enhancements

* Introduced `RealmModules` for working with custom schemas in libraries and apps.
* Introduced `Realm.getDefaultInstance()`, `Realm.setDefaultInstance(RealmConfiguration)` and `Realm.getInstance(RealmConfiguration)`.
* `RealmList.add()`, `RealmList.addAt()` and `RealmList.set()` now copy unmanaged objects transparently into Realm.
* Realm now works with Kotlin (M12+). (Thank you @cypressious)
* Added a check to give a better error message when null is used as value for a primary key.
* Cleaned up examples (remove old test project).
* Added checking for missing generic type in RealmList fields in annotation processor.

### Deprecated

* Deprecated most constructors. They have been been replaced by `Realm.getInstance(RealmConfiguration)` and `Realm.getDefaultInstance()`.
* Deprecated `Realm.migrateRealmAtPath()`. It has been replaced by `Realm.migrateRealm(RealmConfiguration)`.
* Deprecated `Realm.deleteFile()`. It has been replaced by `Realm.deleteRealm(RealmConfiguration)`.
* Deprecated `Realm.compactFile()`. It has been replaced by `Realm.compactRealm(RealmConfiguration)`.

### Bug fixes

* Fixed a performance regression introduced in 0.80.3 occurring during the validation of the Realm schema.
* Fixed unchecked cast warnings when building with Realm.

## 0.80.3

### Enhancements

* Calling `Realm.copyToRealmOrUpdate()` with an object with a null primary key now throws a proper exception.
* Trying to using `Realm.copyToRealmOrUpdate()` with an object with a null primary key now throws a proper exception.
* `RealmChangedListener` now also gets called on the same thread that did the commit.
* `RealmBaseAdapter` now allow `RealmResults` to be null. (Thanks @zaki50)

### Bug fixes

* Fixed a bug making it impossible to open Realms created by Realm-Cocoa if a model had a primary key defined.
* Fixed bug where `Realm.createOrUpdateWithJson()` reset `Date` and `Binary` data to default values if not found in the JSON output.
* Fixed a memory leak when using `RealmBaseAdapter`.
* Fixed a bug where a change to a model class (`RealmList<A>` to `RealmList<B>`) would not throw a `RealmMigrationNeededException`.
* Fixed a bug where setting multiple `RealmLists` didn't remove the previously added objects.
* Solved `ConcurrentModificationException` thrown when `addChangeListener()`/`removeChangeListener()` got called in the `onChange`. (Thanks @beeender)
* Fixed duplicated listeners in the same realm instance. Trying to add duplicated listeners is ignored now. (Thanks @beeender)

## 0.80.2

### Internal

* Updated Realm Core to version 0.89.2
  - fixed bug causing a crash when opening an encrypted Realm file on ARM64 devices.

### Enhancements

* Trying to use `Realm.copyToRealmOrUpdate()` with an object with a null primary key now throws a proper exception.
* `RealmMigrationNeedException` can now return the path to the Realm that needs to be migrated.

### Bug fixes

* Fixed bug where creating a Realm instance with a hashcode collision no longer returned the wrong `Realm` instance.

## 0.80.1

### Internal

* Updated Realm Core to version 0.89.0
  - Improved performance for sorting `RealmResults`.
  - Improved performance for refreshing a `Realm` after inserting or modifying strings or binary data.
  - Fixed bug causing incorrect result when querying indexed fields.
  - Fixed bug causing corruption of string index when deleting an object where there are duplicate values for the indexed field.
  - Fixed bug causing a crash after compacting the Realm file.

### Enhancements

* `Realm.createOrUpdateWithJson()` no longer resets fields to their default value if they are not found in the JSON input.
* `Realm.compactRealmFile()` now uses Realm Core's `compact()` method which is more failure resilient.
* `Realm.copyToRealm()` now correctly handles referenced child objects that are already in the Realm.
* The ARM64 binary is now properly a part of the Eclipse distribution package.
* A `RealmMigrationExceptionNeeded` is now properly thrown if `@Index` and `@PrimaryKey` are not set correctly during a migration.
* Added `Realm.deleteRealmFile(File)` method.
* Added `RealmQuery.isNull()` and `RealmQuery.isNotNull()` for querying relationships.

### Bug fixes

* Fixed bug causing `Realm`s to be cached even though they failed to open correctly.
* Fixed bug causing queries to fail if multiple Realms has different field ordering.
* Fixed bug when using `Realm.copyToRealm()` with a primary key could crash if default value was already used in the `Realm`.
* Fixed a potential `NullPointerException` in the `RealmList` constructor.

## 0.80

### Enhancements

* Queries on relationships can be case sensitive.
* Version checker no longer reports that updates are available when latest version is used.
* Added support for static fields in `RealmObject`s.
* `Realm.writeEncryptedCopyTo()` has been reenabled.

### Bug fixes

* Fixed bug when importing `JSONObjects` containing `NULL` values.
* Fixed crash when trying to remove last element of a `RealmList`.
* Fixed bug crashing annotation processor when using "name" in model classes for `RealmObject` references
* Fixed problem occurring when opening an encrypted `Realm` with two different instances of the same key.

## 0.79.1

### Enhancements

* `copyToRealm()` no longer crashes on cyclic data structures.

### Buf fixes

* Fixed potential crash when using `copyToRealmOrUpdate()` with an object graph containing a mix of elements with and without primary keys.

## 0.79

### Deprecated

* Removed methods deprecated in 0.76. Now `Realm.allObjectsSorted()` and `RealmQuery.findAllSorted()` need to be used instead.

### Enhancements

* Added support for ARM64.
* Added `RealmQuery.not()` to negate a query condition.
* Added `copyToRealmOrUpdate()` and `createOrUpdateFromJson()` methods, that works for models with primary keys.
* Made the native libraries much smaller. Arm went from 1.8MB to 800KB.
* Better error reporting when trying to create or open a Realm file fails.
* Improved error reporting in case of missing accessors in model classes.
* Re-enabled `RealmResults.remove(index)` and `RealmResults.removeLast()`.
* Primary keys are now supported through the `@PrimaryKey` annotation.
* Throw an exception if `deleteRealmFile()` is called when there is an open instance of the `Realm`.
* Made migrations and compression methods synchronised.
* Reimplemented `Realm.allObjectSorted()` for better performance.

### Bug fixes

* Fixed error when instantiating a `Realm` with the wrong key.

## 0.78

### Internal

* Updated core to 0.88.

### Enhancements

* Added proper support for encryption. Encryption support is now included by default. Keys are now 64 bytes long.
* Added support to write an encrypted copy of a Realm.
* Realm no longer incorrectly warns that an instance has been closed too many times.
* Realm now shows a log warning if an instance is being finalized without being closed.

### Bug fixes

* Fixed bug causing `Realm`s to be cached during a `RealmMigration` resulting in invalid realms being returned from `Realm.getInstance()`.

## 0.77

### Enhancements

* Added `Realm.allObjectsSorted()`, `RealmQuery.findAllSorted()` and extending `RealmResults.sort()` for multi-field sorting.
* Added more logging capabilities at the JNI level.
* Added proper encryption support. NOTE: The key has been increased from 32 bytes to 64 bytes (see example).
* Added support for unmanaged objects and custom constructors.
* Added more precise imports in proxy classes to avoid ambiguous references.
* Added support for executing a transaction with a closure using `Realm.executeTransaction()`.
* Added `RealmObject.isValid()` to test if an object is still accessible.
* `RealmResults.sort()` now has better error reporting.
* Throw `NoSuchMethodError` when `RealmResults.indexOf()` is called, since it's not implemented yet.
* Improved handling of empty model classes in the annotation processor
* Removed deprecated static constructors.
* Introduced new static constructors based on File instead of Context, allowing to save Realm files in custom locations.
* `RealmList.remove()` now properly returns the removed object.
* Calling `realm.close()` no longer prevent updates to other open realm instances on the same thread.

### Bug fixes

* Fixed bug when doing queries on the elements of a `RealmList`, ie. like `Realm.where(Foo.class).getBars().where().equalTo("name")`.
* Fixed bug causing `refresh()` to be called on background threads with closed `Realm`s.
* Fixed bug where calling `Realm.close()` too many times could result in `Realm` not getting closed at all. This now triggers a log warning.

## 0.76.0

### Internal

* Upgraded to core 0.87.0 (encryption support, API changes).

### Enhancements

* `RealmObjects` can now be imported using JSON.
* Gradle wrapper updated to support Android Studio 1.0.
* `Realm` class is now marked as final.
* Replaced concurrency example with a better thread example.
* Allowed to add/remove `RealmChangeListeners` in `RealmChangeListeners`.
* Close the `Realm` instance after migrations.
* Added a check to deny the writing of objects outside of a transaction.

### Bug fixes

* Fixed bug in `RealmObject.equals()` so it now correctly compares two objects from the same `Realm`.
* Fixed bug in `Realm` crashing for receiving notifications after `close()`.

## 0.75.1 (03 December 2014)

### Enhancements

* Changed sort to be an in-place method.
* Renamed `SORT_ORDER_DECENDING` to `SORT_ORDER_DESCENDING`.
* Added sorting functionality to `allObjects()` and `findAll()`.

### Bug fixes

* Fixed bug when querying a date column with `equalTo()`, it would act as `lessThan()`.

## 0.75.0 (28 Nov 2014)

### Enhancements

* `Realm` now implements `Closeable`, allowing better cleanup of native resources.
* Added `writeCopyTo()` and `compactRealmFile()` to write and compact a `Realm` to a new file.
* `RealmObject.toString()`, `equals()` and `hashCode()` now support models with cyclic references.
* `RealmResults.iterator()` and `listIterator()` now correctly iterates the results when using `remove()`.

### Bug fixes

* Bug fixed in `Exception` text when field names was not matching the database.
* Bug fixed so `Realm` no longer throws an `Exception` when removing the last object.
* Bug fixed in `RealmResults` which prevented sub-querying.
* The `Date` type does not support millisecond resolution, and dates before `1901-12-13` and dates after `2038-01-19` are not supported on 32 bit systems.
* Fixed bug so `Realm` no longer throws an `Exception` when removing the last object.
* Fixed bug in `RealmResults` which prevented sub-querying.

## 0.74.0 (19 Nov 2014)

### Internal

* Updated core to 0.86.0, fixing a bug in cancelling an empty transaction, and major query speedups with `float`s/`double`s.

### Enhancements

* Added support for more field/accessors naming conventions.
* Added case sensitive versions of string comparison operators `equalTo()` and `notEqualTo()`.
* Added `where()` to `RealmList` to initiate queries.
* Added verification of fields names in queries with links.
* Added exception for queries with invalid field name.
* Allow static methods in model classes.
* An exception will now be thrown if you try to move `Realm`, `RealmResults` or `RealmObject` between threads.
* Consistent handling of UTF-8 strings.
* `removeFromRealm()` now calls `moveLastOver()` which is faster and more reliable when deleting multiple objects.

### Bug fixes

* Fixed a bug in the calculation of the maximum of date field in a `RealmResults`.

## 0.73.1 (05 Nov 2014)

### Bug fixes

* Fixed a bug that would send infinite notifications in some instances.

## 0.73.0 (04 Nov 2014)

### Enhancements

* Rewritten the notification system. The API did not change but it's now much more reliable.
* Added support for switching auto-refresh on and off (`Realm.setAutoRefresh()`).
* Added `RealmBaseAdapter` and an example using it.
* Added `deleteFromRealm()` method to `RealmObject`.

### Bug fixes

* Fixed a bug not allowing queries with more than 1024 conditions.

## 0.72.0 (27 Oct 2014)

### Internal

* Upgraded core to 0.85.1 (deep copying of strings in queries; preparation for link queries).

### Enhancements

* Extended sorting support to more types: `boolean`, `byte`, `short`, `int`, `long`, `float`, `double`, `Date`, and `String` fields are now supported.
* Better support for Java 7 and 8 in the annotations processor.
* Better support for the Eclipse annotations processor.
* Added Eclipse support to the distribution folder.
* Added `Realm.cancelTransaction()` to cancel/abort/rollback a transaction.
* Added support for link queries in the form `realm.where(Owner.class).equalTo("cat.age", 12).findAll()`.
* Faster implementation of `RealmQuery.findFirst()`.

## 0.71.0 (07 Oct 2014)

### Internal

* Upgraded to core 0.84.0.

### Enhancements

* Simplified the release artifact to a single Jar file.
* Added support for Eclipse.
* Added support for deploying to Maven.
* Throw exception if nested transactions are used (it's not allowed).
* Javadoc updated.
* Made the annotations processor more verbose and strict.
* Added `RealmQuery.count()` method.
* Added a new example about concurrency.
* New annotation `@Index` to add search index to a field (currently only supporting String fields).

### Bug fixes

* Fixed [bug in `RealmResults`](https://github.com/realm/realm-java/issues/453).

## 0.70.1 (30 Sep 2014)

### Enhancements

* Enabled unit testing for the realm project.

### Bug fixes

* Fixed handling of camel-cased field names.

## 0.70.0 (29 Sep 2014)

* This is the first public beta release.
