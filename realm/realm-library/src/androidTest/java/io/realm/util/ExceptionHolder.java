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

import junit.framework.AssertionFailedError;

import java.io.PrintWriter;
import java.io.StringWriter;

import static junit.framework.Assert.fail;

/**
 * Utility class for wrapping exceptions from background threads in unit tests.
 * It makes patterns like this possible:
 *
 * {@code
 *   final CountDownLatch taskDone = new CountDownLatch(1);
 *   final ExceptionHolder bgError = new ExceptionHolder();
 *   new Thread(new Runnable() {
 *      @Override
 *      public void run() {
 *          try {
 *              // Error prone code ...
 *          } catch (Exception e) {
 *              bgError.setException(e);
 *          } finall {
 *              taskDone.countDown();
 *          }
 *      }
 *   }).start()
 *   taskDone.await();
 *   bgError.checkFailure();
 * }
 */
public class ExceptionHolder {

    private Throwable exception;

    /**
     * Sets the exception held by this container. This is an one-shot operation.
     *
     * @param throwable error to save.
     * @throws IllegalStateException if an exception have already been put into this holder.
     */
    public void setException(Throwable throwable) {
        if (exception != null) {
            throw new IllegalStateException("An exception has already been set.");
        }
        this.exception = throwable;
    }

    /**
     * Sets a custom error message that can be used instead of setting an exception.
     * This will still trigger {@link #checkFailure()}.
     *
     * @param message error message
     */
    public void setError(String message) {
        setException(new AssertionFailedError(message));
    }

    /**
     * Returns any saved exception.
     *
     * @return {@link Throwable} held by this container
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Checks if an exception has been set and fails the unit test if that is the case.
     */
    public void checkFailure() {
        if (exception != null) {
            StringWriter stacktrace = new StringWriter();
            exception.printStackTrace(new PrintWriter(stacktrace));
            fail(stacktrace.toString());
        }
    }
}

