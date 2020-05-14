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
package io.realm;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.network.ResultHandler;
import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;

import static io.realm.RealmApp.NETWORK_POOL_EXECUTOR;

/**
 * Class encapsulating functionality provided when {@link RealmUser}'s are logged in through the
 * {@link RealmCredentials.IdentityProvider#EMAIL_PASSWORD} provider.
 */
public class EmailPasswordAuth {

    private static final int TYPE_REGISTER_USER = 1;
    private static final int TYPE_CONFIRM_USER = 2;
    private static final int TYPE_RESEND_CONFIRMATION_EMAIL = 3;
    private static final int TYPE_SEND_RESET_PASSWORD_EMAIL = 4;
    private static final int TYPE_CALL_RESET_PASSWORD_FUNCTION = 5;
    private static final int TYPE_RESET_PASSWORD = 6;

    private final RealmApp app;

    /**
     * Creates an authentication provider exposing functionality to using an email and password
     * for login into a Realm Application.
     */
    public EmailPasswordAuth(RealmApp app) {
        this.app = app;
    }

    /**
     * Registers a new user with the given email and password.
     *
     * @param email the email to register with. This will be the username used during log in.
     * @param password the password to associate with the email. The password must be between
     * 6 and 128 characters long.
     *
     * @throws ObjectServerError if the server failed to register the user.
     */
    public void registerUser(String email, String password) throws ObjectServerError {
        Util.checkEmpty(email, "email");
        Util.checkEmpty(password, "password");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_REGISTER_USER,
                app.nativePtr,
                new OsJNIVoidResultCallback(error),
                email, password);
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
     * @throws ObjectServerError if the server failed to register the user.
     */
    public RealmAsyncTask registerUserAsync(String email, String password, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous registration of a user is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
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
     * @throws ObjectServerError if the server failed to confirm the user.
     */
    public void confirmUser(String token, String tokenId) throws ObjectServerError {
        Util.checkEmpty(token, "token");
        Util.checkEmpty(tokenId, "tokenId");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_CONFIRM_USER,
                app.nativePtr,
                new OsJNIVoidResultCallback(error),
                token, tokenId);
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
    public RealmAsyncTask confirmUserAsync(String token, String tokenId, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous confirmation of a user is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                confirmUser(token, tokenId);
                return null;
            }
        }.start();
    }

    /**
     * Resend the confirmation for a user to the given email.
     *
     * @param email the email of the user.
     * @throws ObjectServerError if the server failed to confirm the user.
     */
    public void resendConfirmationEmail(String email) throws ObjectServerError {
        Util.checkEmpty(email, "email");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_RESEND_CONFIRMATION_EMAIL,
                app.nativePtr,
                new OsJNIVoidResultCallback(error),
                email);
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
    public RealmAsyncTask resendConfirmationEmailAsync(String email, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous resending the confirmation email is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                resendConfirmationEmail(email);
                return null;
            }
        }.start();
    }

    /**
     * Sends a user a password reset email for the given email.
     *
     * @param email the email of the user.
     * @throws ObjectServerError if the server failed to confirm the user.
     */
    public void sendResetPasswordEmail(String email) throws ObjectServerError {
        Util.checkEmpty(email, "email");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_SEND_RESET_PASSWORD_EMAIL,
                app.nativePtr,
                new OsJNIVoidResultCallback(error),
                email);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Sends a user a password reset email for the given email.
     *
     * @param email the email of the user.
     * @param callback callback when sending the email has completed or failed. The callback will
     * always happen on the same thread as this method is called on.
     * @throws ObjectServerError if the server failed to confirm the user.
     */
    public RealmAsyncTask sendResetPasswordEmailAsync(String email, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous sending the reset password email is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                sendResetPasswordEmail(email);
                return null;
            }
        }.start();
    }

    /**
     * Call the reset password function configured to the
     * {@link RealmCredentials.IdentityProvider#EMAIL_PASSWORD} provider.
     *
     * @param email the email of the user.
     * @param newPassword the new password of the user.
     * @param args any additional arguments provided to the reset function. All arguments must
     * be able to be converted to JSON compatible values using {@code toString()}.
     * @throws ObjectServerError if the server failed to confirm the user.
     */
    public void callResetPasswordFunction(String email, String newPassword, Object... args) throws ObjectServerError {
        Util.checkEmpty(email, "email");
        Util.checkEmpty(newPassword, "newPassword");
        String encodedArgs = JniBsonProtocol.encode(Arrays.asList(args), app.getConfiguration().getDefaultCodecRegistry());
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_CALL_RESET_PASSWORD_FUNCTION,
                app.nativePtr,
                new OsJNIVoidResultCallback(error),
                email, newPassword, encodedArgs);
        ResultHandler.handleResult(null, error);
    }

    /**
     * Call the reset password function configured to the
     * {@link RealmCredentials.IdentityProvider#EMAIL_PASSWORD} provider.
     *
     * @param email the email of the user.
     * @param newPassword the new password of the user.
     * @param args any additional arguments provided to the reset function. All arguments must
     * be able to be converted to JSON compatible values using {@code toString()}.
     * @param callback callback when the reset has completed or failed. The callback will always
     * happen on the same thread as this this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask callResetPasswordFunctionAsync(String email, String newPassword, Object[] args, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous calling the password reset function is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
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
     * @throws ObjectServerError if the server failed to confirm the user.
     */
    public void resetPassword(String token, String tokenId, String newPassword) throws ObjectServerError {
        Util.checkEmpty(token, "token");
        Util.checkEmpty(tokenId, "tokenId");
        Util.checkEmpty(newPassword, "newPassword");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_RESET_PASSWORD,
                app.nativePtr,
                new OsJNIVoidResultCallback(error),
                token, tokenId, newPassword);
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
    public RealmAsyncTask resetPasswordAsync(String token, String tokenId, String newPassword, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous reset of a password is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                resetPassword(token, tokenId, newPassword);
                return null;
            }
        }.start();
    }

    private static native void nativeCallFunction(int functionType,
                                                  long appNativePtr,
                                                  OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback,
                                                  String... args);
}
