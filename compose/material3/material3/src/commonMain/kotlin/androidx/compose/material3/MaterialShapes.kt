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

package androidx.compose.material3

import androidx.compose.material3.internal.toPath
import androidx.compose.material3.internal.transformed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.TransformResult
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.pill
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Returns a normalized [Path] that is remembered across compositions for this [RoundedPolygon].
 *
 * @param startAngle an angle to rotate the Material shape's path to start drawing from. The
 *   rotation pivot is set to be the shape's centerX and centerY coordinates.
 * @see RoundedPolygon.normalized
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun RoundedPolygon.toPath(startAngle: Int = 0): Path {
    val path = remember { Path() }
    return remember(this, startAngle) {
        this.toPath(path = path, startAngle = startAngle, repeatPath = false, closePath = true)
    }
}

/**
 * Returns a [Shape] that is remembered across compositions for this [RoundedPolygon].
 *
 * @param startAngle an angle to rotate the Material shape's path to start drawing from. The
 *   rotation pivot is always set to be the shape's centerX and centerY coordinates.
 */
@ExperimentalMaterial3ExpressiveApi
@Composable
fun RoundedPolygon.toShape(startAngle: Int = 0): Shape {
    return remember(this, startAngle) {
        object : Shape {
            private val path: Path = toPath(startAngle = startAngle)

            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val scaleMatrix = Matrix().apply { scale(x = size.width, y = size.height) }
                // Scale and translate the path to align its center with the available size
                // center.
                path.transform(scaleMatrix)
                path.translate(size.center - path.getBounds().center)
                return Outline.Generic(path)
            }
        }
    }
}

// TODO: Document all shapes and possible add screenshots.
/**
 * Holds predefined Material Design shapes as [RoundedPolygon]s that can be used at various
 * components as they are, or as part of a [Morph].
 *
 * Note that each [RoundedPolygon] in this class is normalized.
 *
 * @see RoundedPolygon.normalized
 */
@ExperimentalMaterial3ExpressiveApi
sealed class MaterialShapes {

    companion object {

        // Cache various roundings for use below
        private val cornerRound10 = CornerRounding(radius = .1f)
        private val cornerRound15 = CornerRounding(radius = .15f)
        private val cornerRound20 = CornerRounding(radius = .2f)
        private val cornerRound30 = CornerRounding(radius = .3f)
        private val cornerRound40 = CornerRounding(radius = .4f)
        private val cornerRound50 = CornerRounding(radius = .5f)
        private val cornerRound100 = CornerRounding(radius = 1f)

        private val rotateNeg45 = Matrix().apply { rotateZ(-45f) }
        private val rotate45 = Matrix().apply { rotateZ(45f) }
        private val rotateNeg90 = Matrix().apply { rotateZ(-90f) }
        private val rotate90 = Matrix().apply { rotateZ(90f) }
        private val rotateNeg135 = Matrix().apply { rotateZ(-135f) }
        private val unrounded = CornerRounding.Unrounded

        private var _circle: RoundedPolygon? = null
        private var _square: RoundedPolygon? = null
        private var _slanted: RoundedPolygon? = null
        private var _arch: RoundedPolygon? = null
        private var _fan: RoundedPolygon? = null
        private var _arrow: RoundedPolygon? = null
        private var _semiCircle: RoundedPolygon? = null
        private var _oval: RoundedPolygon? = null
        private var _pill: RoundedPolygon? = null
        private var _triangle: RoundedPolygon? = null
        private var _diamond: RoundedPolygon? = null
        private var _clamShell: RoundedPolygon? = null
        private var _pentagon: RoundedPolygon? = null
        private var _gem: RoundedPolygon? = null
        private var _verySunny: RoundedPolygon? = null
        private var _sunny: RoundedPolygon? = null
        private var _cookie4Sided: RoundedPolygon? = null
        private var _cookie6Sided: RoundedPolygon? = null
        private var _cookie7Sided: RoundedPolygon? = null
        private var _cookie9Sided: RoundedPolygon? = null
        private var _cookie12Sided: RoundedPolygon? = null
        private var _ghostish: RoundedPolygon? = null
        private var _clover4Leaf: RoundedPolygon? = null
        private var _clover8Leaf: RoundedPolygon? = null
        private var _burst: RoundedPolygon? = null
        private var _softBurst: RoundedPolygon? = null
        private var _boom: RoundedPolygon? = null
        private var _softBoom: RoundedPolygon? = null
        private var _flower: RoundedPolygon? = null
        private var _puffy: RoundedPolygon? = null
        private var _puffyDiamond: RoundedPolygon? = null
        private var _pixelCircle: RoundedPolygon? = null
        private var _pixelTriangle: RoundedPolygon? = null
        private var _bun: RoundedPolygon? = null
        private var _heart: RoundedPolygon? = null

        val Circle
            get() = _circle ?: circle().normalized().also { _circle = it }

        val Square
            get() = _square ?: square().normalized().also { _square = it }

        val Slanted
            get() = _slanted ?: slanted().normalized().also { _slanted = it }

        val Arch
            get() = _arch ?: arch().normalized().also { _arch = it }

        val Fan
            get() = _fan ?: fan().normalized().also { _fan = it }

        val Arrow
            get() = _arrow ?: arrow().normalized().also { _arrow = it }

        val SemiCircle
            get() = _semiCircle ?: semiCircle().normalized().also { _semiCircle = it }

        val Oval
            get() = _oval ?: oval().normalized().also { _oval = it }

        val Pill
            get() = _pill ?: pill().normalized().also { _pill = it }

        val Triangle
            get() = _triangle ?: triangle().normalized().also { _triangle = it }

        val Diamond
            get() = _diamond ?: diamond().normalized().also { _diamond = it }

        val ClamShell
            get() = _clamShell ?: clamShell().normalized().also { _clamShell = it }

        val Pentagon
            get() = _pentagon ?: pentagon().normalized().also { _pentagon = it }

        val Gem
            get() = _gem ?: gem().normalized().also { _gem = it }

        val VerySunny
            get() = _verySunny ?: verySunny().normalized().also { _verySunny = it }

        val Sunny
            get() = _sunny ?: sunny().normalized().also { _sunny = it }

        val Cookie4Sided
            get() = _cookie4Sided ?: cookie4().normalized().also { _cookie4Sided = it }

        val Cookie6Sided
            get() = _cookie6Sided ?: cookie6().normalized().also { _cookie6Sided = it }

        val Cookie7Sided
            get() = _cookie7Sided ?: cookie7().normalized().also { _cookie7Sided = it }

        val Cookie9Sided
            get() = _cookie9Sided ?: cookie9().normalized().also { _cookie9Sided = it }

        val Cookie12Sided
            get() = _cookie12Sided ?: cookie12().normalized().also { _cookie12Sided = it }

        val Ghostish
            get() = _ghostish ?: ghostish().normalized().also { _ghostish = it }

        val Clover4Leaf
            get() = _clover4Leaf ?: clover4().normalized().also { _clover4Leaf = it }

        val Clover8Leaf
            get() = _clover8Leaf ?: clover8().normalized().also { _clover8Leaf = it }

        val Burst
            get() = _burst ?: burst().normalized().also { _burst = it }

        val SoftBurst
            get() = _softBurst ?: softBurst().normalized().also { _softBurst = it }

        val Boom
            get() = _boom ?: boom().normalized().also { _boom = it }

        val SoftBoom
            get() = _softBoom ?: softBoom().normalized().also { _softBoom = it }

        val Flower
            get() = _flower ?: flower().normalized().also { _flower = it }

        val Puffy
            get() = _puffy ?: puffy().normalized().also { _puffy = it }

        val PuffyDiamond
            get() = _puffyDiamond ?: puffyDiamond().normalized().also { _puffyDiamond = it }

        val PixelCircle
            get() = _pixelCircle ?: pixelCircle().normalized().also { _pixelCircle = it }

        val PixelTriangle
            get() = _pixelTriangle ?: pixelTriangle().normalized().also { _pixelTriangle = it }

        val Bun
            get() = _bun ?: bun().normalized().also { _bun = it }

        val Heart
            get() = _heart ?: heart().normalized().also { _heart = it }

        internal fun circle(numVertices: Int = 10): RoundedPolygon {
            return RoundedPolygon.circle(numVertices = numVertices)
        }

        internal fun square(): RoundedPolygon {
            return RoundedPolygon.rectangle(width = 1f, height = 1f, rounding = cornerRound30)
        }

        internal fun slanted(): RoundedPolygon {
            return RoundedPolygon(
                    numVertices = 4,
                    rounding = CornerRounding(radius = 0.3f, smoothing = 0.5f)
                )
                .transformed(rotateNeg45)
                .transformed { x, y ->
                    TransformResult(x - 0.1f * y, y) // Compose's matrix doesn't support skew!?
                }
        }

        internal fun arch(): RoundedPolygon {
            return RoundedPolygon(
                    numVertices = 4,
                    perVertexRounding =
                        listOf(cornerRound100, cornerRound100, cornerRound20, cornerRound20)
                )
                .transformed(rotateNeg135)
        }

        internal fun fan(): RoundedPolygon {
            return RoundedPolygon(
                    numVertices = 4,
                    perVertexRounding =
                        listOf(cornerRound100, cornerRound20, cornerRound20, cornerRound20)
                )
                .transformed(rotateNeg45)
        }

        internal fun arrow(): RoundedPolygon {
            return triangleChip(innerRadius = .2f, CornerRounding(radius = .22f))
        }

        internal fun triangleChip(innerRadius: Float, rounding: CornerRounding): RoundedPolygon {
            val points =
                floatArrayOf(
                    radialToCartesian(radius = 1f, 270f.toRadians()).x,
                    radialToCartesian(radius = 1f, 270f.toRadians()).y,
                    radialToCartesian(radius = 1f, 30f.toRadians()).x,
                    radialToCartesian(radius = 1f, 30f.toRadians()).y,
                    radialToCartesian(radius = innerRadius, 90f.toRadians()).x,
                    radialToCartesian(radius = innerRadius, 90f.toRadians()).y,
                    radialToCartesian(radius = 1f, 150f.toRadians()).x,
                    radialToCartesian(radius = 1f, 150f.toRadians()).y
                )
            return RoundedPolygon(points, rounding)
        }

        internal fun semiCircle(): RoundedPolygon {
            return RoundedPolygon.rectangle(
                width = 1.6f,
                height = 1f,
                perVertexRounding =
                    listOf(cornerRound20, cornerRound20, cornerRound100, cornerRound100)
            )
        }

        internal fun oval(scaleX: Float = 1f, scaleY: Float = .7f): RoundedPolygon {
            val m = Matrix().apply { scale(x = scaleX, y = scaleY) }
            return RoundedPolygon.circle().transformed(m).transformed(rotateNeg45)
        }

        internal fun pill(width: Float = 1.25f, height: Float = 1f): RoundedPolygon {
            return RoundedPolygon.pill(width = width, height = height)
        }

        internal fun triangle(): RoundedPolygon {
            return RoundedPolygon(numVertices = 3, rounding = cornerRound20)
                .transformed(rotateNeg90)
        }

        internal fun diamond(scaleX: Float = 1f, scaleY: Float = 1.2f): RoundedPolygon {
            return RoundedPolygon(numVertices = 4, rounding = cornerRound30)
                .transformed(Matrix().apply { scale(x = scaleX, y = scaleY) })
        }

        internal fun clamShell(): RoundedPolygon {
            val cornerInset = .6f
            val edgeInset = .4f
            val height = .7f
            val hexPoints =
                floatArrayOf(
                    1f,
                    0f,
                    cornerInset,
                    height,
                    edgeInset,
                    height,
                    -edgeInset,
                    height,
                    -cornerInset,
                    height,
                    -1f,
                    0f,
                    -cornerInset,
                    -height,
                    -edgeInset,
                    -height,
                    edgeInset,
                    -height,
                    cornerInset,
                    -height,
                )
            val pvRounding =
                listOf(
                    cornerRound30,
                    cornerRound30,
                    unrounded,
                    unrounded,
                    cornerRound30,
                    cornerRound30,
                    cornerRound30,
                    unrounded,
                    unrounded,
                    cornerRound30,
                )
            return RoundedPolygon(hexPoints, perVertexRounding = pvRounding)
        }

        internal fun pentagon(): RoundedPolygon {
            return RoundedPolygon(numVertices = 5, rounding = cornerRound30)
                .transformed(Matrix().apply { rotateZ(-360f / 20f) })
        }

        internal fun gem(): RoundedPolygon {
            // irregular hexagon (right narrower than left, then rotated)
            // First, generate a standard hexagon
            val numVertices = 6
            val radius = 1f
            val points = FloatArray(numVertices * 2)
            var index = 0
            for (i in 0 until numVertices) {
                val vertex = radialToCartesian(radius, (PI.toFloat() / numVertices * 2 * i))
                points[index++] = vertex.x
                points[index++] = vertex.y
            }
            // Now adjust-in the points at the top (next-to-last and second vertices, post rotation)
            points[2] -= .1f
            points[3] -= .1f
            points[10] -= .1f
            points[11] += .1f
            return RoundedPolygon(points, cornerRound40).transformed(rotateNeg90)
        }

        internal fun verySunny(): RoundedPolygon {
            return RoundedPolygon.star(
                numVerticesPerRadius = 8,
                innerRadius = .65f,
                rounding = cornerRound15
            )
        }

        internal fun sunny(): RoundedPolygon {
            return RoundedPolygon.star(
                numVerticesPerRadius = 8,
                innerRadius = .83f,
                rounding = cornerRound15
            )
        }

        internal fun cookie4(): RoundedPolygon {
            return RoundedPolygon.star(
                    numVerticesPerRadius = 4,
                    innerRadius = .5f,
                    rounding = cornerRound30
                )
                .transformed(rotateNeg45)
        }

        internal fun cookie6(): RoundedPolygon {
            // 6-point cookie
            return RoundedPolygon.star(
                    numVerticesPerRadius = 6,
                    innerRadius = .75f,
                    rounding = cornerRound50
                )
                .transformed(rotateNeg90)
        }

        internal fun cookie7(): RoundedPolygon {
            // 7-point cookie
            return RoundedPolygon.star(
                    numVerticesPerRadius = 7,
                    innerRadius = .75f,
                    rounding = cornerRound50
                )
                .transformed(rotateNeg90)
        }

        internal fun cookie9(): RoundedPolygon {
            return RoundedPolygon.star(
                    numVerticesPerRadius = 9,
                    innerRadius = .8f,
                    rounding = cornerRound50
                )
                .transformed(rotateNeg90)
        }

        internal fun cookie12(): RoundedPolygon {
            return RoundedPolygon.star(
                    numVerticesPerRadius = 12,
                    innerRadius = .8f,
                    rounding = cornerRound50
                )
                .transformed(rotateNeg90)
        }

        internal fun ghostish(): RoundedPolygon {
            val inset = .5f
            val w = .88f
            val points = floatArrayOf(1f, w, -1f, w, -inset, 0f, -1f, -w, 1f, -w)
            val pvRounding =
                listOf(cornerRound100, cornerRound50, cornerRound100, cornerRound50, cornerRound100)
            return RoundedPolygon(points, perVertexRounding = pvRounding).transformed(rotateNeg90)
        }

        internal fun clover4(): RoundedPolygon {
            // (no inner rounding)
            return RoundedPolygon.star(
                    numVerticesPerRadius = 4,
                    innerRadius = .2f,
                    rounding = cornerRound40,
                    innerRounding = unrounded
                )
                .transformed(rotate45)
        }

        internal fun clover8(): RoundedPolygon {
            // (no inner rounding)
            return RoundedPolygon.star(
                    numVerticesPerRadius = 8,
                    innerRadius = .65f,
                    rounding = cornerRound30,
                    innerRounding = unrounded
                )
                .transformed(Matrix().apply { rotateZ(360f / 16) })
        }

        internal fun burst(): RoundedPolygon {
            return RoundedPolygon.star(numVerticesPerRadius = 12, innerRadius = .7f)
        }

        internal fun softBurst(): RoundedPolygon {
            return RoundedPolygon.star(
                radius = 1f,
                numVerticesPerRadius = 10,
                innerRadius = .65f,
                rounding = cornerRound10,
                innerRounding = cornerRound10
            )
        }

        internal fun boom(): RoundedPolygon {
            return RoundedPolygon.star(numVerticesPerRadius = 15, innerRadius = .42f)
        }

        internal fun softBoom(): RoundedPolygon {
            val points =
                arrayOf(
                    Offset(0.456f, 0.224f),
                    Offset(0.460f, 0.170f),
                    Offset(0.500f, 0.100f),
                    Offset(0.540f, 0.170f),
                    Offset(0.544f, 0.224f),
                    Offset(0.538f, 0.308f)
                )
            val actualPoints = doRepeat(points, 16, center = Offset(0.5f, 0.5f))
            val roundings =
                listOf(
                        CornerRounding(radius = 0.020f),
                        CornerRounding(radius = 0.143f),
                        CornerRounding(radius = 0.025f),
                        CornerRounding(radius = 0.143f),
                        CornerRounding(radius = 0.190f),
                        CornerRounding(radius = 0f)
                    )
                    .let { l -> (0 until 16).flatMap { l } }

            return RoundedPolygon(
                actualPoints,
                perVertexRounding = roundings,
                centerX = 0.5f,
                centerY = 0.5f
            )
        }

        internal fun flower(): RoundedPolygon {
            val smoothRound = CornerRounding(radius = .13f, smoothing = .95f)
            return RoundedPolygon.star(
                numVerticesPerRadius = 8,
                radius = 1f,
                innerRadius = .575f,
                rounding = smoothRound,
                innerRounding = unrounded
            )
        }

        internal fun puffy(): RoundedPolygon {
            val pnr =
                listOf(
                    PointNRound(Offset(0.500f, 0.260f), CornerRounding.Unrounded),
                    PointNRound(Offset(0.526f, 0.188f), CornerRounding(0.095f)),
                    PointNRound(Offset(0.676f, 0.226f), CornerRounding(0.095f)),
                    PointNRound(Offset(0.660f, 0.300f), CornerRounding.Unrounded),
                    PointNRound(Offset(0.734f, 0.230f), CornerRounding(0.095f)),
                    PointNRound(Offset(0.838f, 0.350f), CornerRounding(0.095f)),
                    PointNRound(Offset(0.782f, 0.418f), CornerRounding.Unrounded),
                    PointNRound(Offset(0.874f, 0.414f), CornerRounding(0.095f)),
                )
            val actualPoints =
                doRepeat(pnr, reps = 4, center = Offset(0.5f, 0.5f), mirroring = true)

            return RoundedPolygon(
                actualPoints.fastFlatMap { listOf(it.o.x, it.o.y) }.toFloatArray(),
                perVertexRounding = actualPoints.fastMap { it.r },
                centerX = 0.5f,
                centerY = 0.5f
            )
        }

        internal fun puffyDiamond(): RoundedPolygon {
            val points =
                arrayOf(
                    Offset(0.390f, 0.260f),
                    Offset(0.390f, 0.130f),
                    Offset(0.610f, 0.130f),
                    Offset(0.610f, 0.260f),
                    Offset(0.740f, 0.260f)
                )
            val actualPoints = doRepeat(points, reps = 4, center = Offset(0.5f, 0.5f))
            val roundings =
                listOf(
                        CornerRounding(radius = 0.000f),
                        CornerRounding(radius = 0.104f),
                        CornerRounding(radius = 0.104f),
                        CornerRounding(radius = 0.000f),
                        CornerRounding(radius = 0.104f)
                    )
                    .let { l -> (0 until 4).flatMap { l } }

            return RoundedPolygon(
                actualPoints,
                perVertexRounding = roundings,
                centerX = 0.5f,
                centerY = 0.5f
            )
        }

        internal fun pixelCircle(): RoundedPolygon {
            val pixelSize = .1f
            val points =
                floatArrayOf(
                    // BR quadrant
                    6 * pixelSize,
                    0 * pixelSize,
                    6 * pixelSize,
                    2 * pixelSize,
                    5 * pixelSize,
                    2 * pixelSize,
                    5 * pixelSize,
                    4 * pixelSize,
                    4 * pixelSize,
                    4 * pixelSize,
                    4 * pixelSize,
                    5 * pixelSize,
                    2 * pixelSize,
                    5 * pixelSize,
                    2 * pixelSize,
                    6 * pixelSize,

                    // BL quadrant
                    -2 * pixelSize,
                    6 * pixelSize,
                    -2 * pixelSize,
                    5 * pixelSize,
                    -4 * pixelSize,
                    5 * pixelSize,
                    -4 * pixelSize,
                    4 * pixelSize,
                    -5 * pixelSize,
                    4 * pixelSize,
                    -5 * pixelSize,
                    2 * pixelSize,
                    -6 * pixelSize,
                    2 * pixelSize,
                    -6 * pixelSize,
                    0 * pixelSize,

                    // TL quadrant
                    -6 * pixelSize,
                    -2 * pixelSize,
                    -5 * pixelSize,
                    -2 * pixelSize,
                    -5 * pixelSize,
                    -4 * pixelSize,
                    -4 * pixelSize,
                    -4 * pixelSize,
                    -4 * pixelSize,
                    -5 * pixelSize,
                    -2 * pixelSize,
                    -5 * pixelSize,
                    -2 * pixelSize,
                    -6 * pixelSize,

                    // TR quadrant
                    2 * pixelSize,
                    -6 * pixelSize,
                    2 * pixelSize,
                    -5 * pixelSize,
                    4 * pixelSize,
                    -5 * pixelSize,
                    4 * pixelSize,
                    -4 * pixelSize,
                    5 * pixelSize,
                    -4 * pixelSize,
                    5 * pixelSize,
                    -2 * pixelSize,
                    6 * pixelSize,
                    -2 * pixelSize
                )
            return RoundedPolygon(points, unrounded)
        }

        @Suppress("ListIterator", "PrimitiveInCollection")
        internal fun pixelTriangle(): RoundedPolygon {
            var point = Offset(0f, 0f)
            val points = mutableListOf<Offset>()
            points.add(point)
            val sizes = listOf(56f, 28f, 44f, 26f, 44f, 32f, 38f, 26f, 38f, 32f)
            sizes.chunked(2).forEach { (dx, dy) ->
                point += Offset(dx, 0f)
                points.add(point)
                point += Offset(0f, dy)
                points.add(point)
            }
            point += Offset(32f, 0f)
            points.add(point)
            point += Offset(0f, 38f)
            points.add(point)
            point += Offset(-32f, 0f)
            points.add(point)
            sizes.reversed().chunked(2).forEach { (dy, dx) ->
                point += Offset(0f, dy)
                points.add(point)
                point += Offset(-dx, 0f)
                points.add(point)
            }
            val centerX = points.fastMaxBy { it.x }!!.x / 2
            val centerY = points.fastMaxBy { it.y }!!.y / 2

            return RoundedPolygon(
                points.fastFlatMap { listOf(it.x, it.y) }.toFloatArray(),
                centerX = centerX,
                centerY = centerY,
            )
        }

        internal fun bun(): RoundedPolygon {
            // Basically, two pills stacked on each other
            val inset = .4f
            val sandwichPoints =
                floatArrayOf(
                    1f,
                    1f,
                    inset,
                    1f,
                    -inset,
                    1f,
                    -1f,
                    1f,
                    -1f,
                    0f,
                    -inset,
                    0f,
                    -1f,
                    0f,
                    -1f,
                    -1f,
                    -inset,
                    -1f,
                    inset,
                    -1f,
                    1f,
                    -1f,
                    1f,
                    0f,
                    inset,
                    0f,
                    1f,
                    0f
                )
            val pvRounding =
                listOf(
                    cornerRound100,
                    unrounded,
                    unrounded,
                    cornerRound100,
                    cornerRound100,
                    unrounded,
                    cornerRound100,
                    cornerRound100,
                    unrounded,
                    unrounded,
                    cornerRound100,
                    cornerRound100,
                    unrounded,
                    cornerRound100
                )
            return RoundedPolygon(sandwichPoints, perVertexRounding = pvRounding)
        }

        internal fun heart(): RoundedPolygon {
            val points =
                floatArrayOf(
                    .2f,
                    0f,
                    -.4f,
                    .5f,
                    -1f,
                    1f,
                    -1.5f,
                    .5f,
                    -1f,
                    0f,
                    -1.5f,
                    -.5f,
                    -1f,
                    -1f,
                    -.4f,
                    -.5f
                )
            val pvRounding =
                listOf(
                    unrounded,
                    unrounded,
                    cornerRound100,
                    cornerRound100,
                    unrounded,
                    cornerRound100,
                    cornerRound100,
                    unrounded
                )
            return RoundedPolygon(points, perVertexRounding = pvRounding).transformed(rotate90)
        }

        private data class PointNRound(val o: Offset, val r: CornerRounding)

        private fun doRepeat(points: Array<Offset>, reps: Int, center: Offset) =
            points.size.let { np ->
                (0 until np * reps)
                    .flatMap {
                        val point = points[it % np].rotateDegrees((it / np) * 360f / reps, center)
                        listOf(point.x, point.y)
                    }
                    .toFloatArray()
            }

        @Suppress("PrimitiveInCollection")
        private fun doRepeat(
            points: List<PointNRound>,
            reps: Int,
            center: Offset,
            mirroring: Boolean
        ) =
            if (mirroring) {
                buildList {
                    val angles = points.fastMap { (it.o - center).angleDegrees() }
                    val distances = points.fastMap { (it.o - center).getDistance() }
                    val sectionAngle = 360f / reps
                    repeat(reps) {
                        points.indices.forEach { index ->
                            val i = if (it % 2 == 0) index else points.lastIndex - index
                            if (i > 0 || it % 2 == 0) {
                                val a =
                                    (sectionAngle * it +
                                            if (it % 2 == 0) angles[i]
                                            else sectionAngle - angles[i] + 2 * angles[0])
                                        .toRadians()
                                val finalPoint = Offset(cos(a), sin(a)) * distances[i] + center
                                add(PointNRound(finalPoint, points[i].r))
                            }
                        }
                    }
                }
            } else {
                points.size.let { np ->
                    (0 until np * reps).map {
                        val point = points[it % np].o.rotateDegrees((it / np) * 360f / reps, center)
                        PointNRound(point, points[it % np].r)
                    }
                }
            }

        private fun Offset.rotateDegrees(angle: Float, center: Offset = Offset.Zero) =
            (angle.toRadians()).let { a ->
                val off = this - center
                Offset(off.x * cos(a) - off.y * sin(a), off.x * sin(a) + off.y * cos(a)) + center
            }

        private fun Float.toRadians(): Float {
            return this / 360f * 2 * PI.toFloat()
        }

        private fun Offset.angleDegrees() = atan2(y, x) * 180f / PI.toFloat()

        private fun directionVector(angleRadians: Float) =
            Offset(cos(angleRadians), sin(angleRadians))

        private fun radialToCartesian(
            radius: Float,
            angleRadians: Float,
            center: Offset = Offset.Zero
        ) = directionVector(angleRadians) * radius + center
    }
}
