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
 * Interface used when interested in updates on data either being uploaded to or downloaded from
 * a Realm Object Server.
 */
public interface ProgressListener {
    /**
     * This method will be called periodically from the underlying Object Server Client responsible
     * for uploading and downloading changes from the remote Object Server.
     * <p>
     * This callback will <i>not</i> happen on the UI thread, but on the worker thread controlling
     * the Object Server Client. Use {@code Activity.runOnUiThread(Runnable)} or similar to update
     * any UI elements.
     * <p>
     * <pre>
     * {@code
     * // Adding an upload progress listener that completes when all known changes have been
     * // uploaded.
     * session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
     *   \@Override
     *    public void onChange(Progress progress) {
     *      activity.runOnUiThread(new Runnable() {
     *        \@Override
     *         public void run() {
     *           updateProgressBar(progress);
     *         }
     *      });
     *      if (progress.isTransferComplete() {
     *        session.removeProgressListener(this);
     *      }
     *    }
     * });
     * }
     * </pre>
     *
     * @param progress an immutable progress change event with information about current progress. This object is thread safe.
     */
    void onChange(Progress progress);
}
