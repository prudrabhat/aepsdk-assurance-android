/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.assurance.internal

import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.Log
import java.util.Locale

internal class ScanStateManager(val assuranceDataStoreService: DataStoring) {
    companion object {
        private const val LOG_TAG = "ScanStateManager"
    }

    private var currentScanState = getStoredScanState()

    fun updateScanState(scanState: AssuranceConstants.AppScanKeys.ScanState) {
        currentScanState = scanState
        storeScanState(scanState)
    }

    fun getScanState(): AssuranceConstants.AppScanKeys.ScanState {
        return currentScanState
    }

    fun getStoredScanState(): AssuranceConstants.AppScanKeys.ScanState {
        val assuranceCollection = assuranceDataStoreService.getNamedCollection(AssuranceConstants.DataStoreKeys.DATASTORE_NAME)
        val scanState = assuranceCollection?.getString("scan_state", AssuranceConstants.AppScanKeys.ScanState.INACTIVE.toString())
        Log.debug(Assurance.LOG_TAG, LOG_TAG, "Retrieved scan state from data store: $scanState")
        return try {
            AssuranceConstants.AppScanKeys.ScanState.valueOf(scanState ?: AssuranceConstants.AppScanKeys.ScanState.INACTIVE.toString())
        } catch (e: Exception) {
            Log.debug(Assurance.LOG_TAG, LOG_TAG, "Failed to parse scan state from data store, returning INACTIVE")
            return AssuranceConstants.AppScanKeys.ScanState.INACTIVE
        }
    }

    internal fun storeScanState(scanState: AssuranceConstants.AppScanKeys.ScanState) {
        val assuranceCollection = assuranceDataStoreService.getNamedCollection(AssuranceConstants.DataStoreKeys.DATASTORE_NAME)
        assuranceCollection?.setString("scan_state", scanState.toString())
        Log.debug(Assurance.LOG_TAG, LOG_TAG, "Stored scan state in data store: $scanState")
    }

    @JvmName("sendScanStateEvent")
    internal fun sendScanStateEvent(scanState: AssuranceConstants.AppScanKeys.ScanState) {
        val scanReadyEventData = HashMap<String, Any>()
        scanReadyEventData["scan_state"] =
            scanState.toString().lowercase(Locale.getDefault())
        val readyScan =
            Event.Builder(
                "Stance State",
                EventType.ASSURANCE,
                AssuranceConstants.AppScanKeys.APP_SCAN_EVENT_SOURCE
            )
                .setEventData(scanReadyEventData)
                .build()
        MobileCore.dispatchEvent(readyScan)
    }
}
