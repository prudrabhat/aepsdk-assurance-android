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

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.BlobKeys.RESPONSE_KEY_BLOB_ID
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.BlobKeys.UPLOAD_ENDPOINT_FORMAT
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.BlobKeys.UPLOAD_PATH_API
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.BlobKeys.UPLOAD_PATH_FILEUPLOAD
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.BlobKeys.UPLOAD_QUERY_KEY
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.QuickConnect
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.NetworkingConstants
import com.adobe.marketing.mobile.services.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream

internal class AssuranceAppScanner : AssurancePlugin {
    companion object {
        private const val LOG_TAG = "AssuranceAppScanner"
    }

    val scope = CoroutineScope(Dispatchers.Main)

    private var currentSession: AssuranceSession? = null

    override fun getVendor(): String {
        return AssuranceConstants.VENDOR_ASSURANCE_MOBILE
    }

    override fun getControlType(): String {
        return AssuranceConstants.ControlType.SCREENSHOT
    }

    override fun onEventReceived(event: AssuranceEvent?) {

        val currentActivity = ServiceProvider.getInstance().appContextService.currentActivity
        if (currentActivity == null) {
            Log.debug(
                LOG_TAG,
                Assurance.LOG_TAG,
                "Current activity is null, cannot take screenshot"
            )
            return
        }

        scope.launch {
            manageScreenShot(currentActivity)
        }
    }

    override fun onRegistered(parentSession: AssuranceSession?) {
        currentSession = parentSession
        Log.debug(LOG_TAG, Assurance.LOG_TAG, "AssuranceAppScanner registered")
    }

    override fun onSessionConnected() {
        Log.debug(LOG_TAG, Assurance.LOG_TAG, "AssuranceAppScanner session connected")
    }

    override fun onSessionDisconnected(code: Int) {
        Log.debug(LOG_TAG, Assurance.LOG_TAG, "AssuranceAppScanner session disconnected")
    }

    override fun onSessionTerminated() {
        currentSession = null
        Log.debug(LOG_TAG, Assurance.LOG_TAG, "AssuranceAppScanner session terminated")
    }

    private fun manageScreenShot(currentActivity: Activity) {
        val view = currentActivity.window.decorView.rootView

        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas: Canvas = Canvas(returnedBitmap)
        view.draw(canvas)

        val networkService = ServiceProvider.getInstance().networkService

        val endpoint: String = String.format(UPLOAD_ENDPOINT_FORMAT, "")
        val uri =
            Uri.Builder()
                .encodedPath(endpoint)
                .appendPath(UPLOAD_PATH_API)
                .appendPath(UPLOAD_PATH_FILEUPLOAD)
                .appendQueryParameter(
                    UPLOAD_QUERY_KEY, currentSession?.sessionId ?: ""
                )
                .build()
                .toString()

        val headers: Map<String, String> = mapOf(
            NetworkingConstants.Headers.ACCEPT to NetworkingConstants.HeaderValues.CONTENT_TYPE_JSON_APPLICATION,
            NetworkingConstants.Headers.CONTENT_TYPE to "application/octet-stream",
            "Content-Length" to returnedBitmap.byteCount.toString()

        )

        // bf7248f92b53/aa5e4a9c2c72/launch-0e7e47cda07d-development

        val stream = ByteArrayOutputStream()
        returnedBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val imageBytes = stream.toByteArray()

        val bodyBytes: ByteArray = imageBytes
        val request = NetworkRequest(
            uri,
            HttpMethod.POST,
            bodyBytes,
            headers,
            QuickConnect.CONNECTION_TIMEOUT_MS,
            QuickConnect.READ_TIMEOUT_MS
        )

        networkService.connectAsync(request) { response ->
            if (response == null) {
                Log.debug(
                    LOG_TAG,
                    Assurance.LOG_TAG,
                    "Failed to upload screenshot"
                )
                return@connectAsync
            }

            Log.debug(
                LOG_TAG,
                Assurance.LOG_TAG,
                "Response code ${response.responseCode}, response body $response",
            )

            val responseJson = JSONObject(response.inputStream.readBytes().toString(Charsets.UTF_8))
            if (responseJson.has(RESPONSE_KEY_BLOB_ID)) {

                val screenShotEventData = mapOf(
                    "blobId" to responseJson.getString(RESPONSE_KEY_BLOB_ID),
                    "mimeType" to "image/png"
                )

                val assuranceEvent = AssuranceEvent(
                    AssuranceConstants.AssuranceEventType.BLOB,
                    screenShotEventData
                )
                currentSession?.queueOutboundEvent(assuranceEvent)
            } else {
                Log.debug(
                    LOG_TAG,
                    Assurance.LOG_TAG,
                    "Failed to upload screenshot"
                )
            }
        }
    }
}