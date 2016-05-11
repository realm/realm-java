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

package io.realm;

import android.os.Handler;
import android.os.Message;

/**
 * Handler decorator, to help intercept some messages before they are sent and received.
 */
abstract class HandlerProxy extends Handler {

    private final HandlerController controller;

    public HandlerProxy(HandlerController controller) {
        if (null == controller) {
            throw new IllegalArgumentException("non-null HandlerController required.");
        }
        this.controller = controller;
    }

    /**
     * @see {@link Handler#postAtFrontOfQueue(Runnable)}
     */
    public void postAtFront(Runnable runnable) {
        if (onInterceptOutMessage(0)) {
            postAtFrontOfQueue(runnable);
        }
    }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        boolean eventConsumed = onInterceptOutMessage(msg.what);
        return !eventConsumed && super.sendMessageAtTime(msg, uptimeMillis);
    }

    @Override
    public void handleMessage(Message msg) {
        boolean eventConsumed = onInterceptInMessage(msg.what);
        if (!eventConsumed) {
            controller.handleMessage(msg);
        }
    }

    /**
     * Intercepts a message as it is being posted. Return {@code false} to continue sending it. {@code true} to
     * swallow it.
     *
     * This method will be executed on the thread sending the message.
     *
     * @return {@code true} if message should be swallowed. {@code false} to continue processing it.
     */
    protected boolean onInterceptOutMessage(int what) {
        return false;
    }

    /**
     * Intercepts a message as it is being received. Return {@code false} to let subclasses continue the handling.
     * {@code true} to swallow it.
     *
     * This method will be executed on the thread of the Looper backing the Handler
     *
     * @return {@code true} if message should be swallowed. {@code false} to continue processing it.
     */
    protected boolean onInterceptInMessage(int what) {
        return false;
    }
}
