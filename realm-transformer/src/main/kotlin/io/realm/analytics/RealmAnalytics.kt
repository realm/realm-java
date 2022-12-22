/*
 * Copyright 2021 Realm Inc.
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
package io.realm.analytics

import io.realm.transformer.ProjectMetaData
import io.realm.transformer.CONNECT_TIMEOUT
import io.realm.transformer.READ_TIMEOUT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-logger")

/**
 * Asynchronously submits build information to Realm as part of running
 * the Gradle build.
 *
 * To be clear: this does *not* run when your app is in production or on
 * your end-user's devices; it will only run when you build your app from source.
 *
 * Why are we doing this? Because it helps us build a better product for you.
 * None of the data personally identifies you, your employer or your app, but it
 *  *will* help us understand what Realm version you use, what host OS you use,
 * etc. Having this info will help with prioritizing our time, adding new
 * features and deprecating old features. Collecting an anonymized bundle &
 * anonymized MAC is the only way for us to count actual usage of the other
 * metrics accurately. If we don't have a way to deduplicate the info reported,
 * it will be useless, as a single developer building their app on Windows ten
 * times would report 10 times more than a single developer that only builds
 * once from Mac OS X, making the data all but useless. No one likes sharing
 * data unless it's necessary, we get it, and we've debated adding this for a
 * long long time. Since Realm is a free product without an email signup, we
 * feel this is a necessary step so we can collect relevant data to build a
 * better product for you.
 *
 * Currently the following information is reported:
 * - What version of Realm is being used
 * - What OS you are running on
 * - An anonymized MAC address and bundle ID to aggregate the other information on.
 *
 */
class RealmAnalytics {

    private var data: AnalyticsData? = null

    /**
     * Sends the analytics.
     *
     * @param inputs the inputs provided by the Transform API
     * @param inputModelClasses a list of ctClasses describing the Realm models
     */
    public fun execute() {
        try {
            // If there is no data, analytics was disabled, so exit early.
            val analyticsData: AnalyticsData = data ?: return

            val pool = Executors.newFixedThreadPool(1);
            try {
                pool.execute { UrlEncodedAnalytics.create().execute(analyticsData) }
                pool.awaitTermination(CONNECT_TIMEOUT + READ_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (e: InterruptedException) {
                pool.shutdownNow()
            }
        } catch (e: Exception) {
            // Analytics failing for any reason should not crash the build
            logger.debug("Error happened when sending Realm analytics data: $e")
        }
    }

    fun calculateAnalyticsData(metadata: ProjectMetaData): Boolean {
        if (!isAnalyticsEnabled(metadata.isGradleOffline))  {
            return false
        }

        data = AnalyticsData(
            appId = PublicAppId(metadata.appId),
            usesKotlin = metadata.usesKotlin,
            usesSync = metadata.usesSync,
            targetSdk = metadata.targetSdk,
            minSdk = metadata.minSdk,
            target = metadata.targetType,
            gradleVersion = metadata.gradleVersion,
            agpVersion = metadata.agpVersion
        )
        return true
    }

    private fun isAnalyticsEnabled(isOffline: Boolean): Boolean {
        val env = System.getenv()
        return !isOffline
                && env["REALM_DISABLE_ANALYTICS"] == null
                && env["CI"] == null
    }
}