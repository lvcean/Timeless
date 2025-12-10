package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.entity.EventRecordEntity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.util.Calendar

@Composable
fun EventStatsChart(
    records: List<EventRecordEntity>,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer.build() }
    
    LaunchedEffect(records) {
        // Group records by day for the last 7 days
        val now = Calendar.getInstance()
        val dailyCounts = mutableMapOf<Int, Int>() 
        
        // Initialize last 7 days with 0
        for (i in 0..6) {
             val cal = Calendar.getInstance()
             cal.add(Calendar.DAY_OF_YEAR, -i)
             dailyCounts[cal.get(Calendar.DAY_OF_YEAR)] = 0
        }

        records.forEach { record ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = record.timestamp
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            // Only count if within known range (simplification)
            if (dailyCounts.containsKey(dayOfYear)) {
                dailyCounts[dayOfYear] = dailyCounts[dayOfYear]!! + 1
            }
        }
        
        // Convert to sorted lists for X and Y axes
        val xValues = (0..6).reversed().toList() // 0 is today, 1 is yesterday... logic reversed for chart: 0 is 6 days ago
        val yValues = xValues.map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -(6-offset)) 
            dailyCounts[cal.get(Calendar.DAY_OF_YEAR)] ?: 0
        }

        modelProducer.tryRunTransaction {
            columnSeries {
                series(yValues)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            "最近7天趋势",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
