/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.pdf.testapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

@RestrictTo(RestrictTo.Scope.LIBRARY)
class MainActivity : AppCompatActivity() {

    companion object {
        private const val MIME_TYPE_PDF = "application/pdf"
    }

    private val filePicker =
        registerForActivityResult(GetContent()) { uri: Uri? ->
            uri?.let {
                setPdfView()
                // TODO: Implement loading PDF from URI using latest APIs
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getContentButton: MaterialButton = findViewById(R.id.launch_button)

        getContentButton.setOnClickListener { filePicker.launch(MIME_TYPE_PDF) }
    }

    private fun setPdfView() {
        // TODO: Implement this based on new APIs
    }
}
