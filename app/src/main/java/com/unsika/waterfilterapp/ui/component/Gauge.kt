package com.unsika.waterfilterapp.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.unsika.waterfilterapp.ui.theme.WaterFilterTheme
import java.util.Locale


@Composable
fun Gauge(
    canvasSize: Dp = 300.dp,
    indicatorValue: Double = 0.0,
    maxIndicatorValue: Double = 3000.0,
    foregroundIndicatorColor: Color = MaterialTheme.colorScheme.primary,
//    indicatorStrokeCap: StrokeCap = StrokeCap.Round,
    bigTextFontSize: TextUnit = MaterialTheme.typography.titleLarge.fontSize,
    bigTextColor: Color = MaterialTheme.colorScheme.onSurface,
    bigTextSuffix: String = "NTU",
    smallText: String = "Kejernihan Air",
    smallTextFontSize: TextUnit = MaterialTheme.typography.titleSmall.fontSize,
    smallTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
) {
    var allowedIndicatorValue by remember {
        mutableDoubleStateOf(maxIndicatorValue)
    }
    allowedIndicatorValue = if (indicatorValue <= maxIndicatorValue) {
        indicatorValue
    } else {
        maxIndicatorValue
    }

    var animatedIndicatorValue by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(key1 = allowedIndicatorValue) {
        animatedIndicatorValue = allowedIndicatorValue.toFloat()
    }

    val animatedBigTextColor by animateColorAsState(
        targetValue = if (allowedIndicatorValue == 0.0)
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else
            bigTextColor,
        animationSpec = tween(1000),
        label = "big text animation"
    )

    Column(
        modifier = Modifier
            .size(canvasSize)
            .drawBehind {
                val componentSize = size / 1.25f
                embeddedBackground(
                    componentSize = componentSize,
                    indicatorColor = foregroundIndicatorColor
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmbeddedElements(
            bigText = String.format(Locale.ENGLISH, "%.1f", allowedIndicatorValue).toDouble(),
            bigTextFontSize = bigTextFontSize,
            bigTextColor = animatedBigTextColor,
            bigTextSuffix = bigTextSuffix,
            smallText = smallText,
            smallTextColor = smallTextColor,
            smallTextFontSize = smallTextFontSize
        )
    }
}

fun DrawScope.embeddedBackground(
    componentSize: Size,
    indicatorColor: Color,
) {
    drawCircle(
        color = indicatorColor,
        radius = componentSize.minDimension / 2f,
        style = Stroke(30f)
    )
    drawCircle(
        color = indicatorColor.copy(0.75f),
        radius = componentSize.minDimension / 2.25f,
        style = Stroke(25f)
    )
    drawCircle(
        color = indicatorColor.copy(0.25f),
        radius = componentSize.minDimension / 2.5f,
        style = Stroke(20f)
    )
}

@Composable
fun EmbeddedElements(
    bigText: Double,
    bigTextFontSize: TextUnit,
    bigTextColor: Color,
    bigTextSuffix: String,
    smallText: String,
    smallTextColor: Color,
    smallTextFontSize: TextUnit
) {
    Text(
        text = "$bigText ${bigTextSuffix.take(3)}",
        color = bigTextColor,
        fontSize = bigTextFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = smallText,
        color = smallTextColor,
        fontSize = smallTextFontSize,
        textAlign = TextAlign.Center
    )
}

@Composable
@Preview(showBackground = true)
fun CustomComponentPreview() {
    WaterFilterTheme(
        useDarkTheme = false
    ) {
        val turbiValue = 10.0
        val turbiStatus = "Baik"
        val (mainColor, secondaryColor) = when {
            turbiValue < 20 -> // Green
                MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer

            turbiValue < 40 -> // Orange
                Color(0xFFF57C00) to Color(0xFFFFE0B2)

            else -> // Red
                Color(0xFFD32F2F) to Color(0xFFFFCDD2)
        }
        Box(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 18.dp),
                    text = "Kejernihan Air",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Gauge(
                    canvasSize = 200.dp,
                    indicatorValue = turbiValue,
                    smallText = turbiStatus,
                    foregroundIndicatorColor = mainColor,
                    bigTextFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    smallTextFontSize = MaterialTheme.typography.titleSmall.fontSize
                )
            }
        }
    }
}