/*
 * Copyright 2017 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm;

/**
 * Classes which implement this interface can be managed by a Realm, which will make them bound to a thread-specific
 * Realm instance. Managed objects that implement this interface must be explicitly exported and imported to be passed
 * between threads. Trying to access a managed object from another thread will throw an {@link IllegalStateException}.
 *
 * Managed instances of objects implementing this interface can be converted to a {@link ThreadSafeReference}
 * by calling {@ThreadSafeReference.create()}
 *
 * Note that only types defined by Realm can meaningfully implement this interface and defining new classes which
 * attempt to conform to it will not make them work with {@link ThreadSafeReference}.
 */
public interface ThreadConfined {

}
