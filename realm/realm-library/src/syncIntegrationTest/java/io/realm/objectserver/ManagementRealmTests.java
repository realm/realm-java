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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.entities.Dog;
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
public class ManagementRealmTests extends BaseIntegrationTest {

    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @Ignore("TODO: Test is currently flaky. See https://github.com/realm/realm-java/pull/4066")
    @Test
    @RunTestInLooperThread
    public void create_acceptOffer() {
        SyncUser user1 = UserFactory.createUser(Constants.AUTH_URL, "user1");
        final SyncUser user2 = UserFactory.createUser(Constants.AUTH_URL, "user2");

        // 1. User1 creates Realm that user2 does not have access
        final String user1RealmUrl = "realm://127.0.0.1:9080/" + user1.getIdentity() + "/permission-offer-test";
        SyncConfiguration config1 = new SyncConfiguration.Builder(user1, user1RealmUrl).
                errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail("Realm 1 unexpected error: " + error);
                    }
                })
                .build();
        final Realm realm1 = Realm.getInstance(config1);
        looperThread.addTestRealm(realm1);
        realm1.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(Dog.class);
            }
        });

        // 2. Create configuration for User2's Realm.
        final SyncConfiguration config2 = new SyncConfiguration.Builder(user2, user1RealmUrl).build();

        // 3. Create PermissionOffer
        final AtomicReference<String> offerId = new AtomicReference<String>(null);
        final Realm user1ManagementRealm = user1.getManagementRealm();
        looperThread.addTestRealm(user1ManagementRealm);
        user1ManagementRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                boolean readPermission = true;
                boolean readWritePermission = true;
                boolean managePermission = false;
                Date expiresAt = null;
                PermissionOffer offer = new PermissionOffer(user1RealmUrl, readPermission, readWritePermission, managePermission, expiresAt);
                offerId.set(offer.getId());
                realm.copyToRealm(offer);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // 4. Wait for offer to get an token
                RealmLog.error("OfferID: " + offerId.get());
                RealmResults<PermissionOffer> offers = user1ManagementRealm.where(PermissionOffer.class)
                        .equalTo("id", offerId.get())
                        .findAllAsync();
                looperThread.keepStrongReference(offers);
                offers.addChangeListener(new RealmChangeListener<RealmResults<PermissionOffer>>() {
                    @Override
                    public void onChange(RealmResults<PermissionOffer> offers) {
                        final PermissionOffer offer = offers.first(null);
                        if (offer != null && offer.isSuccessful() && offer.getToken() != null) {
                            // 5. User2 uses the token to accept the offer
                            final String offerToken = offer.getToken();
                            final AtomicReference<String> offerResponseId = new AtomicReference<String>();
                            final Realm user2ManagementRealm = user2.getManagementRealm();
                            looperThread.addTestRealm(user2ManagementRealm);
                            user2ManagementRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    PermissionOfferResponse offerResponse = new PermissionOfferResponse(offerToken);
                                    offerResponseId.set(offerResponse.getId());
                                    realm.copyToRealm(offerResponse);
                                }
                            }, new Realm.Transaction.OnSuccess() {
                                @Override
                                public void onSuccess() {
                                    // 6. Wait for the offer response to be accepted
                                    RealmResults<PermissionOfferResponse> responses = user2ManagementRealm.where(PermissionOfferResponse.class)
                                            .equalTo("id", offerResponseId.get())
                                            .findAllAsync();
                                    looperThread.keepStrongReference(responses);
                                    responses.addChangeListener(new RealmChangeListener<RealmResults<PermissionOfferResponse>>() {
                                        @Override
                                        public void onChange(RealmResults<PermissionOfferResponse> responses) {
                                            PermissionOfferResponse response = responses.first(null);
                                            if (response != null && response.isSuccessful() && response.getToken().equals(offerToken)) {
                                                // 7. Response accepted. It should now be possible for user2 to access user1's Realm
                                                Realm realm = Realm.getInstance(config2);
                                                looperThread.addTestRealm(realm);
                                                RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
                                                looperThread.keepStrongReference(dogs);
                                                dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
                                                    @Override
                                                    public void onChange(RealmResults<Dog> element) {
                                                        assertEquals(1, element.size());
                                                        looperThread.testComplete();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
