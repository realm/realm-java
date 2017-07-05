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

package io.realm.rule;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.realm.services.RemoteTestService;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This should be used along with {@link RunWithRemoteService}. See comments there for usage.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface RunTestWithRemoteService {
    Class<? extends RemoteTestService> remoteService();
    boolean onLooperThread();
}
