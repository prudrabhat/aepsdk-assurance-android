/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.assurance.internal.ui.floatingbutton

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.assurance.R
import com.adobe.marketing.mobile.assurance.internal.AssuranceAppState
import com.adobe.marketing.mobile.assurance.internal.AssuranceComponentRegistry
import com.adobe.marketing.mobile.assurance.internal.ui.AssuranceActivity
import com.adobe.marketing.mobile.services.Log

/**
 * A composable implementation of the Assurance floating button.
 * This allows app developers to embed the Assurance floating button directly into their UI
 * when using Jetpack Compose rather than relying on the default implementation.
 *
 * @param connected true if Assurance is connected, false otherwise
 * @param size the size of the button in dp
 * @param cornerRadius the corner radius of the button in dp
 */
@Composable
fun AssuranceSessionConnectionIndicator(
    size: Int = 50,
    cornerRadius: Float = 10f,
    alignment: Alignment = Alignment.BottomEnd,
) {
    val context = LocalContext.current

    val buttonState = remember { AssuranceComponentRegistry.appState.sessionPhase }

    val iconRes = if (buttonState.value == AssuranceAppState.SessionPhase.Connected) {
        R.drawable.ic_assurance_active
    } else {
        R.drawable.ic_assurance_inactive
    }
    val isFocused = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current


    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            modifier = Modifier
                .align(alignment)
                .run {
                    if (isFocused.value) {
                        border(
                            BorderStroke(2.dp, Color.White),
                            RoundedCornerShape(cornerRadius.dp)
                        )
                    } else {
                        this
                    }
                }
                .onFocusEvent {
                    isFocused.value = it.hasFocus
                    Log.debug(
                        Assurance.LOG_TAG,
                        "AssuranceFloatingButtonComposable",
                        "Assurance Floating Button Focused: ${it.hasFocus}"
                    )
                }
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
                .onKeyEvent { keyEvent ->
                    // Handle key events when the button is focused
                    if (isFocused.value) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                focusManager.clearFocus(true)
                                true
                            }

                            else -> false
                        }
                    } else {
                        false
                    }
                },
            onClick = {
                try {
                    // Start a new session if the button is clicked and the session is disconnected
                    if (buttonState.value is AssuranceAppState.SessionPhase.Disconnected) {
                        val disconnected =
                            buttonState.value as AssuranceAppState.SessionPhase.Disconnected
                        val isReconnecting = disconnected.reconnecting
                        if (!isReconnecting) Assurance.startSession() else launchAssuranceActivity(context)
                    } else {
                        launchAssuranceActivity(context)
                    }
                } catch (e: Exception) {
                    Log.debug(
                        Assurance.LOG_TAG,
                        "AssuranceFloatingButtonComposable",
                        "Failed to launch Assurance activity: ${e.localizedMessage}"
                    )
                }
            },
            backgroundColor = Color.Transparent,
            shape = RoundedCornerShape(0.dp),
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Assurance Floating Button",
                modifier = Modifier
                    .size(size.dp)
            )
        }
    }
}

private fun launchAssuranceActivity(context: Context) {
    val intent = Intent(context, AssuranceActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}