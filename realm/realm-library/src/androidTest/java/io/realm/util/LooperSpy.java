/*
 * Copyright 2016 Realm Inc.
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

package io.realm.util;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Debug util for dumping the content of a Looper message queue.
 * Inspired by: https://corner.squareup.com/2013/12/android-main-thread-2.html
 */
public class LooperSpy {

    private final Field messagesField;
    private final Field nextField;
    private final MessageQueue mainMessageQueue;

    /**
     * Creates a LooperSpy for the Looper on the main thread.
     */
    public static LooperSpy mainLooper() {
        return new LooperSpy(Looper.getMainLooper());
    }

    /**
     * Creates a LooperSpy for a specified Looper.
     */
    public static LooperSpy create(Looper looper) {
        return new LooperSpy(looper);
    }

    private LooperSpy(Looper looper) {
        try {
            Field queueField = Looper.class.getDeclaredField("mQueue");
            queueField.setAccessible(true);
            messagesField = MessageQueue.class.getDeclaredField("mMessages");
            messagesField.setAccessible(true);
            nextField = Message.class.getDeclaredField("next");
            nextField.setAccessible(true);
            mainMessageQueue = (MessageQueue) queueField.get(looper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Outputs the Looper's entire MessageQueue to LogCat.
     */
    public void dumpQueue() {
        try {
            Message nextMessage = (Message) messagesField.get(mainMessageQueue);
            Log.d("MainLooperSpy", "Begin dumping queue");
            dumpMessages(nextMessage);
            Log.d("MainLooperSpy", "End dumping queue");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void dumpMessages(Message message) throws IllegalAccessException {
        if (message != null) {
            Log.d("MainLooperSpy", message.toString());
            Message next = (Message) nextField.get(message);
            dumpMessages(next);
        }
    }
}

