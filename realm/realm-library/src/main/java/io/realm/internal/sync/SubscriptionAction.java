/*
 * Copyright 2018 Realm Inc.
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

package io.realm.internal.sync;

import java.util.concurrent.TimeUnit;

/**
 * Wrapper class describing if and how a subscription should be created when creating a query result.
 */
public class SubscriptionAction {
    public static final SubscriptionAction NO_SUBSCRIPTION = new SubscriptionAction(null, 0, false);
    public static final SubscriptionAction ANONYMOUS_SUBSCRIPTION = new SubscriptionAction("", Long.MAX_VALUE, false);

    public static SubscriptionAction create(String subscriptionName, long timeToLiveMs) {
        return new SubscriptionAction(subscriptionName, timeToLiveMs, false);
    }

    public static SubscriptionAction update(String subscriptionName, long timeToLiveMs) {
        return new SubscriptionAction(subscriptionName, timeToLiveMs, true);
    }

    private final String subscriptionName;
    private final long timeToLiveMs;
    private final boolean update;

    public SubscriptionAction(String subscriptionName, long timeToLiveMs, boolean update) {
        this.subscriptionName = subscriptionName;
        this.timeToLiveMs = timeToLiveMs;
        this.update = update;
    }

    public boolean shouldCreateSubscriptions() {
        return subscriptionName != null;
    }

    public String getName() {
        return subscriptionName;
    }

    public long getTimeToLiveMs() {
        return timeToLiveMs;
    }

    public boolean isUpdate() {
        return update;
    }
}
