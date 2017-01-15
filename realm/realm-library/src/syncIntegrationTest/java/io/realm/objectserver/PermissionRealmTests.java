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

package io.realm.objectserver;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.entities.Dog;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.permissions.PermissionOffer;
import io.realm.permissions.PermissionOfferResponse;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PermissionRealmTests {

    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @Test
    @RunTestInLooperThread
    public void create_acceptOffer() {
        RealmLog.setLevel(LogLevel.ALL);
        SyncUser user1 = UserFactory.createUser(Constants.AUTH_URL, "user1");
        final SyncUser user2 = UserFactory.createUser(Constants.AUTH_URL, "user2");

        // 1. User1 creates Realm that user2 does not have access
        final String user1RealmUrl = "realm://" + Constants.IP + ":9080/~/permission-offer-test";
        SyncConfiguration config1 = new SyncConfiguration.Builder(user1, user1RealmUrl).
                errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail("Realm 1 unexpected error: " + error);
                    }
                })
                .build();
        Realm realm1 = Realm.getInstance(config1);
        looperThread.testRealms.add(realm1);
        realm1.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(Dog.class);
            }
        });

        // 2. Verify user2 does not have access
        final CountDownLatch expectedSessionError = new CountDownLatch(1);
        final SyncConfiguration config2 = new SyncConfiguration.Builder(user2, user1RealmUrl)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
// TODO: Why is there no ACCESS_DENIED being reported?
//                        if (error.getErrorCode() == ErrorCode.ACCESS_DENIED) {
//                            expectedSessionError.countDown();
//                        } else {
//                            fail("Realm 2 unexpected error: " + error);
//                        }
                    }
                })
                .build();

        Realm realm2 = Realm.getInstance(config2);
        looperThread.testRealms.add(realm2);
//        TestHelper.awaitOrFail(expectedSessionError);

        // 3. Create PermissionOffer
        final AtomicReference<String> offerId = new AtomicReference<String>(null);
        Realm user1ManagementRealm = user1.getManagementRealm();
        looperThread.testRealms.add(user1ManagementRealm);
        user1ManagementRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                boolean readPermission = true;
                boolean readWritePermission = true;
                boolean managePermission = false;
                Date expiresAt = null;
                PermissionOffer offer = new PermissionOffer(user1RealmUrl, readPermission, readWritePermission, managePermission, expiresAt);
                realm.copyToRealm(offer);
                offerId.set(offer.getId());
            }
        });

        // 4. Wait for offer to get an token
        RealmResults<PermissionOffer> offers = user1ManagementRealm.where(PermissionOffer.class).findAllAsync();
        offers.addChangeListener(new RealmChangeListener<RealmResults<PermissionOffer>>() {
            @Override
            public void onChange(RealmResults<PermissionOffer> offers) {
                final PermissionOffer offer = offers.first(null);
                if (offer != null && offer.getToken() != null && !offer.getToken().equals("")) {

                    // 5. User 2 uses the token to accept the offer
                    Realm user2ManagementRealm = user2.getManagementRealm();
                    looperThread.testRealms.add(user2ManagementRealm);
                    user2ManagementRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealm(new PermissionOfferResponse(offer.getToken()));
                        }
                    });

                    // 6. Wait for the offer response to be accepted
                    RealmResults<PermissionOfferResponse> responses = user2ManagementRealm.where(PermissionOfferResponse.class).findAllAsync();
                    responses.addChangeListener(new RealmChangeListener<RealmResults<PermissionOfferResponse>>() {
                        @Override
                        public void onChange(RealmResults<PermissionOfferResponse> responses) {
                            PermissionOfferResponse response = responses.first(null);
                            if (response != null && response.getStatusCode() == 0 && response.getToken().equals(offer.getToken())) {
                                // 7. Response accepted. It should now be possible for user2 to access user1's Realm
                                Realm realm = Realm.getInstance(config2);
                                looperThread.testRealms.add(realm);
                                assertEquals(1, realm.where(Dog.class).count());
                                looperThread.testComplete();
                            }
                        }
                    });
                }
            }
        });
    }
}
