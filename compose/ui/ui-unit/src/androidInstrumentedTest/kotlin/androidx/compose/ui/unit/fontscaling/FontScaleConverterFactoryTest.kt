/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.compose.ui.unit.fontscaling

import androidx.collection.forEach
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.lang.IllegalStateException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random.Default.nextFloat
import org.junit.Test
import org.junit.runner.RunWith

/** Unit tests for FontScaleConverterFactory. */
@RunWith(AndroidJUnit4::class)
class FontScaleConverterFactoryTest {

    @SmallTest
    @Test
    fun scale200IsTwiceAtSmallSizes() {
        val table = FontScaleConverterFactory.forScale(2F)!!
        assertThat(table.convertSpToDp(1F)).isWithin(CONVERSION_TOLERANCE).of(2f)
        assertThat(table.convertSpToDp(8F)).isWithin(CONVERSION_TOLERANCE).of(16f)
        assertThat(table.convertSpToDp(10F)).isWithin(CONVERSION_TOLERANCE).of(20f)
        assertThat(table.convertSpToDp(5F)).isWithin(CONVERSION_TOLERANCE).of(10f)
        assertThat(table.convertSpToDp(0F)).isWithin(CONVERSION_TOLERANCE).of(0f)
    }

    @LargeTest
    @Test
    fun missingLookupTablePastEnd_returnsLinear() {
        val table = FontScaleConverterFactory.forScale(3F)!!
        generateSequenceOfFractions(-10000f..10000f, step = 0.01f).map {
            assertThat(table.convertSpToDp(it)).isWithin(CONVERSION_TOLERANCE).of(it * 3f)
        }
        assertThat(table.convertSpToDp(1F)).isWithin(CONVERSION_TOLERANCE).of(3f)
        assertThat(table.convertSpToDp(8F)).isWithin(CONVERSION_TOLERANCE).of(24f)
        assertThat(table.convertSpToDp(10F)).isWithin(CONVERSION_TOLERANCE).of(30f)
        assertThat(table.convertSpToDp(5F)).isWithin(CONVERSION_TOLERANCE).of(15f)
        assertThat(table.convertSpToDp(0F)).isWithin(CONVERSION_TOLERANCE).of(0f)
        assertThat(table.convertSpToDp(50F)).isWithin(CONVERSION_TOLERANCE).of(150f)
        assertThat(table.convertSpToDp(100F)).isWithin(CONVERSION_TOLERANCE).of(300f)
    }

    @SmallTest
    @Test
    fun missingLookupTable106_returnsInterpolated() {
        // Wear uses 1.06
        val table = FontScaleConverterFactory.forScale(1.06F)!!
        assertThat(table.convertSpToDp(1F)).isWithin(INTERPOLATED_TOLERANCE).of(1f * 1.06F)
        assertThat(table.convertSpToDp(8F)).isWithin(INTERPOLATED_TOLERANCE).of(8f * 1.06F)
        assertThat(table.convertSpToDp(10F)).isWithin(INTERPOLATED_TOLERANCE).of(10f * 1.06F)
        assertThat(table.convertSpToDp(20F)).isLessThan(20f * 1.06F)
        assertThat(table.convertSpToDp(100F)).isLessThan(100f * 1.06F)
        assertThat(table.convertSpToDp(5F)).isWithin(INTERPOLATED_TOLERANCE).of(5f * 1.06F)
        assertThat(table.convertSpToDp(0F)).isWithin(INTERPOLATED_TOLERANCE).of(0f)
    }

    @SmallTest
    @Test
    fun missingLookupTable199_returnsInterpolated() {
        val table = FontScaleConverterFactory.forScale(1.9999F)!!
        assertThat(table.convertSpToDp(1F)).isWithin(INTERPOLATED_TOLERANCE).of(2f)
        assertThat(table.convertSpToDp(8F)).isWithin(INTERPOLATED_TOLERANCE).of(16f)
        assertThat(table.convertSpToDp(10F)).isWithin(INTERPOLATED_TOLERANCE).of(20f)
        assertThat(table.convertSpToDp(5F)).isWithin(INTERPOLATED_TOLERANCE).of(10f)
        assertThat(table.convertSpToDp(0F)).isWithin(INTERPOLATED_TOLERANCE).of(0f)
    }

    @SmallTest
    @Test
    fun missingLookupTable160_returnsInterpolated() {
        val table = FontScaleConverterFactory.forScale(1.6F)!!
        assertThat(table.convertSpToDp(1F)).isWithin(INTERPOLATED_TOLERANCE).of(1f * 1.6F)
        assertThat(table.convertSpToDp(8F)).isWithin(INTERPOLATED_TOLERANCE).of(8f * 1.6F)
        assertThat(table.convertSpToDp(10F)).isWithin(INTERPOLATED_TOLERANCE).of(10f * 1.6F)
        assertThat(table.convertSpToDp(20F)).isLessThan(20f * 1.6F)
        assertThat(table.convertSpToDp(100F)).isLessThan(100f * 1.6F)
        assertThat(table.convertSpToDp(5F)).isWithin(INTERPOLATED_TOLERANCE).of(5f * 1.6F)
        assertThat(table.convertSpToDp(0F)).isWithin(INTERPOLATED_TOLERANCE).of(0f)
    }

    @SmallTest
    @Test
    fun missingLookupTable_cachesInterpolated() {
        val table = FontScaleConverterFactory.forScale(1.6F)
        assertThat(table).isNotNull()

        assertThat(FontScaleConverterFactory.sLookupTables.containsKey((1.6F * 100).toInt()))
            .isTrue()
        // Double check known existing values
        assertThat(FontScaleConverterFactory.sLookupTables.containsKey((1.5F * 100).toInt()))
            .isTrue()
    }

    @SmallTest
    @Test
    fun missingLookupTablePastEnd_cachesLinear() {
        val table = FontScaleConverterFactory.forScale(3F)
        assertThat(table).isNotNull()

        assertThat(FontScaleConverterFactory.sLookupTables.containsKey((3F * 100).toInt())).isTrue()
        // Double check known existing values
        assertThat(FontScaleConverterFactory.sLookupTables.containsKey((1.5F * 100).toInt()))
            .isTrue()
    }

    @SmallTest
    @Test
    fun missingLookupTableNegativeReturnsNull() {
        assertThat(FontScaleConverterFactory.forScale(-1F)).isNull()
    }

    @SmallTest
    @Test
    fun unnecessaryFontScalesReturnsNull() {
        assertThat(FontScaleConverterFactory.forScale(0F)).isNull()
        assertThat(FontScaleConverterFactory.forScale(1F)).isNull()
        assertThat(FontScaleConverterFactory.forScale(1.02F)).isNull()
        assertThat(FontScaleConverterFactory.forScale(0.85F)).isNull()
    }

    @SmallTest
    @Test
    fun tablesMatchAndAreMonotonicallyIncreasing() {
        FontScaleConverterFactory.sLookupTables.forEach { _, lookupTable ->
            if (lookupTable !is FontScaleConverterTable) {
                throw IllegalStateException("Didn't return a FontScaleConverterTable")
            }

            assertThat(lookupTable.mToDpValues).hasLength(lookupTable.mFromSpValues.size)
            assertThat(lookupTable.mToDpValues).isNotEmpty()

            assertThat(lookupTable.mFromSpValues.asList()).isInStrictOrder()
            assertThat(lookupTable.mToDpValues.asList()).isInStrictOrder()

            assertThat(lookupTable.mFromSpValues.asList()).containsNoDuplicates()
            assertThat(lookupTable.mToDpValues.asList()).containsNoDuplicates()
        }
    }

    @SmallTest
    @Test
    fun testIsNonLinearFontScalingActive() {
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1f)).isFalse()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(0f)).isFalse()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(-1f)).isFalse()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(0.85f)).isFalse()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.02f)).isFalse()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.03f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.06f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.10f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.15f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.1499999f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(1.5f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(2f)).isTrue()
        assertThat(FontScaleConverterFactory.isNonLinearFontScalingActive(3f)).isTrue()
    }

    @SmallTest
    @Test
    fun wearFontScalesAreMonotonicAsScaleIncreases() {
        wearFontSizes.forEach { fontSize ->
            var lastDp = 0f

            wearFontScales.forEach { fontScale ->
                val converter = FontScaleConverterFactory.forScale(fontScale)
                val currentDp = converter?.convertSpToDp(fontSize) ?: (fontSize * fontScale)

                assertWithMessage("Font Scale $fontScale and Size $fontSize")
                    .that(currentDp)
                    .isAtLeast(lastDp)

                lastDp = currentDp
            }
        }
    }

    @SmallTest
    @Test
    fun wearFontScalesAreMonotonicAsSpIncreases() {
        wearFontScales.forEach { fontScale ->
            val converter = FontScaleConverterFactory.forScale(fontScale)
            var lastDp = 0f

            wearFontSizes.forEach { fontSize ->
                val currentDp = converter?.convertSpToDp(fontSize) ?: (fontSize * fontScale)

                assertWithMessage("Font Scale $fontScale and Size $fontSize")
                    .that(currentDp)
                    .isAtLeast(lastDp)

                lastDp = currentDp
            }
        }
    }

    @LargeTest
    @Test
    fun allFeasibleScalesAndConversionsDoNotCrash() {
        generateSequenceOfFractions(-10f..10f, step = 0.1f)
            .fuzzFractions()
            .mapNotNull { FontScaleConverterFactory.forScale(it) }
            .flatMap { table ->
                generateSequenceOfFractions(-2000f..2000f, step = 0.1f).fuzzFractions().map {
                    Pair(table, it)
                }
            }
            .forEach { (table, sp) ->
                try {
                    // Truth is slow because it creates a bunch of
                    // objects. Don't use it unless we need to.
                    if (!table.convertSpToDp(sp).isFinite()) {
                        assertWithMessage("convertSpToDp(%s) on table: %s", sp, table)
                            .that(table.convertSpToDp(sp))
                            .isFinite()
                    }
                } catch (e: Exception) {
                    throw Exception("Exception during convertSpToDp($sp) on table: $table", e)
                }
            }
    }

    @LargeTest
    @Test
    fun testGenerateSequenceOfFractions() {
        val fractions = generateSequenceOfFractions(-1000f..1000f, step = 0.1f).toList()
        fractions.forEach {
            assertThat(it).isAtLeast(-1000f)
            assertThat(it).isAtMost(1000f)
        }

        assertThat(fractions).isInStrictOrder()
        assertThat(fractions).hasSize(1000 * 2 * 10 + 1) // Don't forget the 0 in the middle!

        assertThat(fractions).contains(100f)
        assertThat(fractions).contains(500.1f)
        assertThat(fractions).contains(500.2f)
        assertThat(fractions).contains(0.2f)
        assertThat(fractions).contains(0f)
        assertThat(fractions).contains(-10f)
        assertThat(fractions).contains(-10f)
        assertThat(fractions).contains(-10.3f)

        assertThat(fractions).doesNotContain(-10.31f)
        assertThat(fractions).doesNotContain(0.35f)
        assertThat(fractions).doesNotContain(0.31f)
        assertThat(fractions).doesNotContain(-.35f)
    }

    @LargeTest
    @Test
    fun testFuzzFractions() {
        val numFuzzedFractions = 6
        val fractions =
            generateSequenceOfFractions(-1000f..1000f, step = 0.1f).fuzzFractions().toList()
        fractions.forEach {
            assertThat(it).isAtLeast(-1000f)
            assertThat(it).isLessThan(1001f)
        }

        val numGeneratedFractions = 1000 * 2 * 10 + 1 // Don't forget the 0 in the middle!
        assertThat(fractions).hasSize(numGeneratedFractions * numFuzzedFractions)

        assertThat(fractions).contains(100f)
        assertThat(fractions).contains(500.1f)
        assertThat(fractions).contains(500.2f)
        assertThat(fractions).contains(0.2f)
        assertThat(fractions).contains(0f)
        assertThat(fractions).contains(-10f)
        assertThat(fractions).contains(-10f)
        assertThat(fractions).contains(-10.3f)
    }

    companion object {
        private const val CONVERSION_TOLERANCE = 0.05f
        private const val INTERPOLATED_TOLERANCE = 0.3f

        private val wearFontScales = listOf(0.94f, 1.0f, 1.06f, 1.12f, 1.18f, 1.24f)
        // 32 is added to check for an edge case
        private val wearFontSizes = listOf(10f, 12f, 14f, 15f, 16f, 20f, 24f, 30f, 32f, 34f)
    }
}

fun generateSequenceOfFractions(
    range: ClosedFloatingPointRange<Float>,
    step: Float
): Sequence<Float> {
    val multiplier = 1f / step
    val start = floor(range.start * multiplier).toInt()
    val endInclusive = ceil(range.endInclusive * multiplier).toInt()
    return generateSequence(start) { it + 1 }
        .takeWhile { it <= endInclusive }
        .map { it.toFloat() / multiplier }
}

private fun Sequence<Float>.fuzzFractions(): Sequence<Float> {
    return flatMap { i ->
        listOf(i, i + 0.01f, i + 0.054f, i + 0.099f, i + nextFloat(), i + nextFloat())
    }
}
