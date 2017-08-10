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

import io.realm.internal.Keep;

/**
 * This interface is used to determine if a Realm file should be compacted the first time the file is opened and before
 * the instance is returned.
 * <p>
 * Note that compacting a file can take a while, so compacting should generally only be done on a background thread or
 * when used in combination with {@link Realm#getInstanceAsync(RealmConfiguration, io.realm.Realm.Callback)}.
 */
@Keep
public interface CompactOnLaunchCallback {

    /**
     * This method determines if the Realm file should be compacted before opened and returned to the user. It is passed
     * the total file size (data + free space) and the bytes used by data in the file.
     *
     * @param totalBytes the total file size (data + free space).
     * @param usedBytes the total bytes used by data in the file.
     * @return {code true} to indicate an attempt to compact the file should be made. Otherwise, the compaction will be
     * skipped.
     */
    boolean shouldCompact(long totalBytes, long usedBytes);
}
