/*
 * Copyright 2022 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.assurance.testapp

import android.app.Application
import android.util.Log
import com.adobe.marketing.mobile.Analytics
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.assurance.testapp.AssuranceTestAppConstants.TAG
import com.adobe.marketing.mobile.edge.identity.Identity

class AssuranceTestApp : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)

        MobileCore.registerExtensions(
            listOf(
                Assurance.EXTENSION,
                Lifecycle.EXTENSION,
                Signal.EXTENSION,
                Analytics.EXTENSION,
                Messaging.EXTENSION,
                Edge.EXTENSION,
                Identity.EXTENSION
            )
        ) {
            MobileCore.lifecycleStart(null)
            Log.d(TAG, "AEP Mobile SDK initialization complete.");
        }
    }

}