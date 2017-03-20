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
 * Enum describing how to listen for progress changes.
 * TODO Explain this better
 */
public enum ProgressMode {
    /**
     * When registering the {@link ProgressListener}, it will record the current number of changes, and will only
     * continue to report progress changes until those changes have been either downloaded or uploaded. After that
     * the progress listener will not report any further changes.
     *
     * This is useful when e.g. reporting progress when downloading a Realm for the first time.
     * TODO more info
     */
    CURRENT_CHANGES,

    /**
     * A {@link ProgressListener} registered in this mode, will continue to report progress changes.
     * TODO More info
     */
    INDEFINETELY
}
