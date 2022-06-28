package eu.monniot.subpleaseapp.ui.downloads

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp


object Arrow {
    val Outline: Path
    val Inner: Path = Path()

    init {
        val outer = Path()
        outer.moveTo(1f, 0f)
        outer.lineTo(3f, 0f)
        outer.lineTo(3f, 4f)
        outer.lineTo(4f, 4f)
        outer.lineTo(2f, 6f)
        outer.lineTo(0f, 4f)
        outer.lineTo(1f, 4f)
        outer.lineTo(1f, 0f)

        Inner.moveTo(1.4f, .4f)
        Inner.lineTo(2.6f, .4f)
        Inner.lineTo(2.6f, 4.4f)
        Inner.lineTo(3.1f, 4.4f)
        Inner.lineTo(2f, 5.5f)
        Inner.lineTo(.9f, 4.4f)
        Inner.lineTo(1.4f, 4.4f)
        Inner.lineTo(1.4f, .4f)

        Outline = Path.combine(PathOperation.Difference, outer, Inner)
    }
}

// TODO Change colors based on the theme (currently doesn't work well with dark mode)
@Composable
fun DownloadIcon(percent: Float, modifier: Modifier = Modifier) {
    // Scale is dependent on the canvas size, here fixed at 50dp tall
    val scale = 23f

    // TODO Clip Float in text
    Canvas(
        modifier = modifier
            .size(34.dp, 50.dp)
            .semantics { contentDescription = "$percent% downloaded" }) {
        val separation = this.size.height * (percent / 100)

        // Outline
        scale(scale, pivot = Offset(0f, 0f)) {
            drawPath(
                Arrow.Outline, Color.Black
            )
        }

        // Filling
        clipRect(bottom = separation) {
            scale(scale, pivot = Offset(0f, 0f)) {
                drawPath(
                    Arrow.Inner, Color.Green
                )
            }
        }
    }
}

class PercentProvider : PreviewParameterProvider<Float> {
    override val values: Sequence<Float> =
        sequenceOf(0f, 33f, 70f, 100f)
}

@Preview(showBackground = true, backgroundColor = 0xcccccc, heightDp = 56, widthDp = 56)
@Composable
fun DownloadIconPreview(@PreviewParameter(PercentProvider::class) percent: Float) {

    Box {
        DownloadIcon(
            percent = percent,
            modifier = Modifier
                .align(Alignment.Center)
                .border(.5.dp, Color.Red)
        )
    }
}
