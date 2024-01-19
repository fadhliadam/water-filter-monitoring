package com.unsika.waterfilterapp.ui.screen.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.unsika.waterfilterapp.R
import com.unsika.waterfilterapp.data.History
import com.unsika.waterfilterapp.data.Water
import com.unsika.waterfilterapp.data.remote.DataState
import com.unsika.waterfilterapp.ui.component.Gauge
import com.unsika.waterfilterapp.ui.component.StatusCard
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    homeVM: HomeViewModel = hiltViewModel()
) {
    HomeContent(homeVM)
}

@Composable
fun HomeContent(
    homeVM: HomeViewModel
) {
    val waterResult = homeVM.responseWater.collectAsState()
    val historyResult = homeVM.responseHistory.collectAsState()
    var waterLoading by remember { mutableStateOf(true) }
    var historyLoading by remember { mutableStateOf(true) }
    var waterData by remember { mutableStateOf<Water?>(null) }
    var listHistory by remember { mutableStateOf<List<History?>>(emptyList()) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.logo_kkn),
                    contentDescription = "logo kkn",
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "Pemantau Filter Air",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            SetWaterData(
                result = waterResult.value,
                water = { waterData = it },
                loading = { waterLoading = it }
            )
            SetHistoryData(
                result = historyResult.value,
                listHistory = { listHistory = it },
                loading = { historyLoading = it }
            )

            if (!waterLoading) {
                Column {
                    waterData?.let { WaterContent(water = it) }
                    HistoryContent(listHistory = listHistory)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun SetWaterData(
    result: DataState<Water?>,
    water: (Water) -> Unit,
    loading: (Boolean) -> Unit
) {
    result.let { dataState ->
        when (dataState) {
            is DataState.Success -> {
                dataState.data?.let { water(it) }
                Log.d("air", dataState.data.toString())
                loading(false)
            }

            is DataState.Loading -> {
                loading(true)
            }

            is DataState.Failure -> {
                loading(false)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Center
                ) {
                    Text(
                        text = dataState.message,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
fun SetHistoryData(
    result: DataState<List<History?>>,
    listHistory: (List<History?>) -> Unit,
    loading: (Boolean) -> Unit
) {
    result.let { dataState ->
        when (dataState) {
            is DataState.Success -> {
                listHistory(dataState.data)
                loading(false)
            }

            is DataState.Loading -> {
                loading(true)
            }

            is DataState.Failure -> {
                loading(false)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Center
                ) {
                    Text(
                        text = dataState.message,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            else -> {}
        }
    }
}


@Composable
fun WaterContent(water: Water) {
    var turbidityStatusColor by remember { mutableStateOf(Color.Cyan) }
    when (water.turbi_status) {
        "Sangat Baik" -> {
            turbidityStatusColor = MaterialTheme.colorScheme.primary
        }

        "Baik" -> {
            turbidityStatusColor = MaterialTheme.colorScheme.primary
        }

        "Cukup Baik" -> {
            turbidityStatusColor = MaterialTheme.colorScheme.tertiary
        }

        "Cukup Buruk" -> {
            turbidityStatusColor = Color(0xFFF57C00)
        }

        else -> {
            turbidityStatusColor = Color(0xFFD32F2F)
        }
    }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Center
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Gauge(
                    canvasSize = 200.dp,
                    indicatorValue = water.turbi_value,
                    foregroundIndicatorColor = turbidityStatusColor,
                )
            }
        }
        StatusCard(
            tempValue = water.temp_value,
            turbidityStatus = water.turbi_status
        )
    }
}

@Composable
fun HistoryContent(listHistory: List<History?>) {
    Column(modifier = Modifier.padding(14.dp)) {
        Text(
            text = "Riwayat Kejernihan Air",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        SingleLineChartWithGridLines(listHistory)
    }
}

@Composable
fun SingleLineChartWithGridLines(listHistory: List<History?>) {
    val pointsData = mutableListOf<Point>()
    val formattedDates = mutableListOf<String>()
    listHistory.forEachIndexed { index, history ->
        if (history != null) {
            val parseDate = SimpleDateFormat("d-MM-yyyy", Locale.getDefault()).parse(history.date)
            parseDate?.let { SimpleDateFormat("dd MMM yyyy", Locale("id")).format(it) }
                ?.let { formattedDates.add(it) }
            pointsData.add(Point(index.toFloat(), history.turbi_value.toFloat()))
        }
    }

    val steps = 5
    val xAxisData = AxisData.Builder()
        .axisStepSize(90.dp)
        .topPadding(105.dp)
        .steps(pointsData.size - 1)
        .labelData { i -> formattedDates[i] }
        .labelAndAxisLinePadding(15.dp)
        .build()
    val yAxisData = AxisData.Builder()
        .steps(steps)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            // Add yMin to get the negative axis values to the scale
            val yMin = pointsData.minOf { it.y }
            val yMax = pointsData.maxOf { it.y }
            val yScale = (yMax - yMin) / steps
            ((i * yScale) + yMin).formatToSinglePrecision()
        }.build()
    val data = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(),
                    IntersectionPoint(),
                    SelectionHighlightPoint(),
                    ShadowUnderLine(),
                    SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines()
    )
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        lineChartData = data
    )
}