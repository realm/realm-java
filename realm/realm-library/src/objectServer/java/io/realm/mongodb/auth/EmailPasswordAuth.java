/*
 * Copyright 2020 Realm Inc.
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
package io.realm.mongodb.auth;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.annotations.Beta;
import io.realm.internal.mongodb.Request;
import io.realm.mongodb.AppException;
import io.realm.RealmAsyncTask;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;

import static io.realm.mongodb.App.NETWORK_POOL_EXECUTOR;

/**
 * Class encapsulating functionality provided when {@link User}'s are logged in through the
 * {@link Credentials.Provider#EMAIL_PASSWORD} provider.
 */
@Beta
public abstract class EmailPasswordAuth {

    private static final int TYPE_REGISTER_USER = 1;
    private static final int TYPE_CONFIRM_USER = 2;
    private static final int TYPE_RESEND_CONFIRMATION_EMAIL = 3;
    private static final int TYPE_SEND_RESET_PASSWORD_EMAIL = 4;
    private static final int TYPE_CALL_RESET_PASSWORD_FUNCTION = 5;
    private static final int TYPE_RESET_PASSWORD = 6;
    private static final int TYPE_RETRY_CUSTOM_CONFIRMATION = 7;

    protected final App app;

    /**
     * Creates an authentication provider exposing functionality to using an email and password
     * for login into a Realm Application.
     */
    protected EmailPasswordAuth(App app) {
        this.app = app;
    }

    /**
     * Registers a new user with the given email and password.
     *
     * @param email the email to register with. This will be the username used during log in.
     * @param password the password to associate with the email. The password must be between
     * 6 and 128 characters long.
     *
     * @throws AppException if the server failed to register the user.
     */
    public void registerUser(String email, String password) throws AppException {
        Util.checkEmpty(email, "email");
        Util.checkEmpty(password, "password");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_REGISTER_USER, new OsJNIVoidResultCallback(error), email, password);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Registers a new user with the given email and password.
     *
     * @param email the email to register with. This will be the username used during log in.
     * @param password the password to associated with the email. The password must be between
     * 6 and 128 characters long.
     * @param callback callback when registration has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     *
     * @throws IllegalStateException if called from a non-looper thread.
     * @throws AppException if the server failed to register the user.
     */
    public RealmAsyncTask registerUserAsync(String email, String password, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous registration of a user is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                registerUser(email, password);
                return null;
            }
        }.start();
    }

    /**
     * Confirms a user with the given token and token id.
     *
     * @param token the confirmation token.
     * @param tokenId the id of the confirmation token.
     * @throws AppException if the server failed to confirm the user.
     */
    public void confirmUser(String token, String tokenId) throws AppException {
        Util.checkEmpty(token, "token");
        Util.checkEmpty(tokenId, "tokenId");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_CONFIRM_USER, new OsJNIVoidResultCallback(error), token, tokenId);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Confirms a user with the given token and token id.
     *
     * @param token the confirmation token.
     * @param tokenId the id of the confirmation token.
     * @param callback callback when confirmation has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask confirmUserAsync(String token, String tokenId, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous confirmation of a user is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                confirmUser(token, tokenId);
                return null;
            }
        }.start();
    }

    /**
     * Resend the confirmation for a user to the given email.
     *
     * @param email the email of the user.
     * @throws AppException if the server failed to confirm the user.
     */
    public void resendConfirmationEmail(String email) throws AppException {
        Util.checkEmpty(email, "email");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_RESEND_CONFIRMATION_EMAIL, new OsJNIVoidResultCallback(error), email);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Resend the confirmation for a user to the given email.
     *
     * @param email the email of the user.
     * @param callback callback when resending the email has completed or failed. The callback will
     * always happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask resendConfirmationEmailAsync(String email, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous resending the confirmation email is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                resendConfirmationEmail(email);
                return null;
            }
        }.start();
    }

    /**
     * Retries the custom confirmation on a user for a given email.
     *
     * @param email the email of the user.
     * @throws AppException if the server failed to confirm the user.
     */
    public void retryCustomConfirmation(String email) throws AppException {
        Util.checkEmpty(email, "email");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_RETRY_CUSTOM_CONFIRMATION, new OsJNIVoidResultCallback(error), email);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Retries the custom confirmation on a user for a given email.
     *
     * @param email the email of the user.
     * @param callback callback when retrying the custom confirmation has completed or failed. The callback will
     * always happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask retryCustomConfirmationAsync(String email, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous retry custom confirmation is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                retryCustomConfirmation(email);
                return null;
            }
        }.start();
    }

    /**
     * Sends a user a password reset email for the given email.
     *
     * @param email the email of the user.
     * @throws AppException if the server failed to confirm the user.
     */
    public void sendResetPasswordEmail(String email) throws AppException {
        Util.checkEmpty(email, "email");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_SEND_RESET_PASSWORD_EMAIL, new OsJNIVoidResultCallback(error), email);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Sends a user a password reset email for the given email.
     *
     * @param email the email of the user.
     * @param callback callback when sending the email has completed or failed. The callback will
     * always happen on the same thread as this method is called on.
     * @throws AppException if the server failed to confirm the user.
     */
    public RealmAsyncTask sendResetPasswordEmailAsync(String email, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous sending the reset password email is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                sendResetPasswordEmail(email);
                return null;
            }
        }.start();
    }

    /**
     * Call the reset password function configured to the
     * {@link Credentials.Provider#EMAIL_PASSWORD} provider.
     *
     * @param email the email of the user.
     * @param newPassword the new password of the user.
     * @param args any additional arguments provided to the reset function. All arguments must
     * be able to be converted to JSON compatible values using {@code toString()}.
     * @throws AppException if the server failed to confirm the user.
     */
    public void callResetPasswordFunction(String email, String newPassword, Object... args) throws AppException {
        Util.checkEmpty(email, "email");
        Util.checkEmpty(newPassword, "newPassword");
        String encodedArgs = JniBsonProtocol.encode(Arrays.asList(args), app.getConfiguration().getDefaultCodecRegistry());
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_CALL_RESET_PASSWORD_FUNCTION, new OsJNIVoidResultCallback(error), email, newPassword, encodedArgs);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Call the reset password function configured to the
     * {@link Credentials.Provider#EMAIL_PASSWORD} provider.
     *
     * @param email the email of the user.
     * @param newPassword the new password of the user.
     * @param args any additional arguments provided to the reset function. All arguments must
     * be able to be converted to JSON compatible values using {@code toString()}.
     * @param callback callback when the reset has completed or failed. The callback will always
     * happen on the same thread as this this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask callResetPasswordFunctionAsync(String email, String newPassword, Object[] args, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous calling the password reset function is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                callResetPasswordFunction(email, newPassword, args);
                return null;
            }
        }.start();
    }

    /**
     * Resets the password of a user with the given token, token id, and new password.
     *
     * @param token the reset password token.
     * @param tokenId the id of the reset password token.
     * @param newPassword the new password for the user identified by the {@code token}. The password
     * must be between 6 and 128 characters long.
     * @throws AppException if the server failed to confirm the user.
     */
    public void resetPassword(String token, String tokenId, String newPassword) throws AppException {
        Util.checkEmpty(token, "token");
        Util.checkEmpty(tokenId, "tokenId");
        Util.checkEmpty(newPassword, "newPassword");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        // The order of arguments in ObjectStore is different than the order of arguments in the
        // Java API. The Java API order came from the old Stitch API.
        call(TYPE_RESET_PASSWORD, new OsJNIVoidResultCallback(error), newPassword, token, tokenId);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Resets the newPassword of a user with the given token, token id, and new password.
     *
     * @param token the reset password token.
     * @param tokenId the id of the reset password token.
     * @param newPassword the new password for the user identified by the {@code token}. The password
     * must be between 6 and 128 characters long.
     * @param callback callback when the reset has completed or failed. The callback will always
     * happen on the same thread as this this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask resetPasswordAsync(String token, String tokenId, String newPassword, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous reset of a password is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                resetPassword(token, tokenId, newPassword);
                return null;
            }
        }.start();
    }

    protected abstract void call(int functionType, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback, String... args);

}
