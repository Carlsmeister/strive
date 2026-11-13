package se.carlsmeister.strive.theme

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IconsTest {
    /** One vector per mask in bundle.css, in its order: 79 at dev 28264c4. */
    @Test
    fun oneVectorPerIconMaskInBundleOrder() {
        val names = StriveIcons.all.map { it.first }
        assertEquals(79, names.size)
        assertEquals(names.distinct(), names)
        assertEquals(listOf("home", "food", "plus"), names.take(3))
        assertEquals(listOf("play-f", "pause-f", "dot"), names.takeLast(3))
    }

    @Test
    fun everyIconIsOnThe24PointGrid() = StriveIcons.all.forEach { (name, v) ->
        assertEquals(24.dp to 24.dp, v.defaultWidth to v.defaultHeight, name)
        assertEquals(24f to 24f, v.viewportWidth to v.viewportHeight, name)
    }

    /** Four masks fill (fill='black'); the rest are 2 pt round strokes (Icons card). */
    @Test
    fun theFourFilledIconsFillAndTheRestStroke() {
        val paths = StriveIcons.all.map { (name, v) -> name to v.root[0] as VectorPath }
        assertEquals(listOf("star", "play-f", "pause-f", "dot"), paths.filter { it.second.fill != null }.map { it.first })
        paths.filter { it.second.fill == null }.forEach { (name, p) ->
            assertNotNull(p.stroke, name)
            assertEquals(2f, p.strokeLineWidth, name)
            assertEquals(StrokeCap.Round to StrokeJoin.Round, p.strokeLineCap to p.strokeLineJoin, name)
        }
    }

    /** bundle.css writes "5.9.9-4.3" for 5.9, .9, -4.3, as SVG allows; the star misdraws if the parser reads it otherwise. */
    @Test
    fun compactPathNumbersParseAsSvgReadsThem() = assertEquals(
        listOf(PathNode.RelativeLineTo(5.9f, .9f), PathNode.RelativeLineTo(-4.3f, 4.1f)),
        PathParser().parsePathString("l5.9.9-4.3 4.1").toNodes(),
    )
}
