/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.work.multiprocess

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

public class RemoteSuccessWorker(context: Context, parameters: WorkerParameters) :
    RemoteCoroutineWorker(context, parameters) {
    override suspend fun doRemoteWork(): Result {
        delay(100)
        return Result.success(outputData())
    }

    public companion object {
        public fun outputData(): Data {
            return workDataOf("success" to true)
        }
    }
}
