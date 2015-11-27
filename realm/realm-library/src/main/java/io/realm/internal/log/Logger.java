/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal.log;

/**
 * Interface for Realm logger implementations.
 */
public interface Logger {
    void v(String message);
    void v(String message, Throwable t);
    void d(String message);
    void d(String message, Throwable t);
    void i(String message);
    void i(String message, Throwable t);
    void w(String message);
    void w(String message, Throwable t);
    void e(String message);
    void e(String message, Throwable t);
}
