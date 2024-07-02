/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.wear.compose.material3.samples

import androidx.annotation.Sampled
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text

@Sampled
@Composable
fun EdgeButtonSample() {
    Box(Modifier.fillMaxSize()) {
        Text("Confirm", Modifier.align(Alignment.Center))
        EdgeButton(
            onClick = { /* Do something */ },
            buttonHeight = ButtonDefaults.EdgeButtonHeightMedium,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Check icon",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }
}
