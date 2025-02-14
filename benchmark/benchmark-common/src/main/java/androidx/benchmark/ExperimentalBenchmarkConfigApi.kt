/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.benchmark

/**
 * Annotation indicating experimental API primarily intended to allow configuration of
 * microbenchmarks from code, overriding instrumentation arguments like
 * `androidx.benchmark.profiling.mode`, and custom microbenchmark metrics.
 */
@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalBenchmarkConfigApi
