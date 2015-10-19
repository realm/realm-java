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

package io.realm.proxy;

import android.os.Handler;
import android.os.Message;

/**
 * Handler decorator, to help intercept some messages
 */
public abstract class HandlerProxy extends Handler {
    private final Handler handler;

    public HandlerProxy(Handler handler) {
        if (null == handler) throw new IllegalArgumentException("null handler");
        this.handler = handler;
    }

    /**
     * @see {@link Handler#postAtFrontOfQueue(Runnable)}
     */
    public void postAtFront (Runnable runnable) {
        handler.postAtFrontOfQueue(runnable);
    }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        boolean eventConsumed = onInterceptMessage(msg.what);
        return !eventConsumed && handler.sendMessageAtTime(msg, uptimeMillis);

    }

    // called on the Handler's Thread
    public abstract boolean onInterceptMessage(int what);
}
