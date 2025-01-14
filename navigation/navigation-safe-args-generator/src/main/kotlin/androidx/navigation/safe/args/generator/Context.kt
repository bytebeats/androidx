/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.navigation.safe.args.generator

import androidx.navigation.safe.args.generator.models.Argument
import androidx.navigation.safe.args.generator.models.Destination
import androidx.navigation.safe.args.generator.models.IncludedDestination
import androidx.navigation.safe.args.generator.models.ResReference

internal class Context {
    val logger = NavLogger()
    private var nextId = 0

    fun createStubId() = ResReference("error", "id", "errorId${next()}")

    fun createStubArg() = Argument("errorArg${next()}", StringType)

    fun createStubDestination() =
        Destination(createStubId(), null, "stub", emptyList(), emptyList())

    fun createStubIncludedDestination() = IncludedDestination(createStubId())

    private fun next() = nextId++
}
