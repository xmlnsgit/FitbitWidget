package com.visuale.azmwidget.glance_widget.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.unit.ColorProvider
import androidx.glance.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Data model
data class ActiveZoneMinutes(
    val fatBurn: Int,
    val cardio: Int,
    val dayOfWeek: String
)

fun parseJsonResponse(jsonResponse: String): List<ActiveZoneMinutes> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val jsonObject = JSONObject(jsonResponse)
    val activitiesArray = jsonObject.getJSONArray("activities-active-zone-minutes")

    return List(activitiesArray.length()) { index ->
        val entry = activitiesArray.getJSONObject(index)
        val valueMap = entry.getJSONObject("value")

        val fatBurn = valueMap.optInt("fatBurnActiveZoneMinutes", 0)
        val cardio = valueMap.optInt("cardioActiveZoneMinutes", 0)
        val dateStr = entry.getString("dateTime")
        val date = LocalDate.parse(dateStr, formatter)
        val dayOfWeek = date.dayOfWeek.name.first().toString()

        ActiveZoneMinutes(fatBurn, cardio, dayOfWeek)
    }
}

class FitbitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val sharedPreferences = context.getSharedPreferences("7dAzmFitbit", Context.MODE_PRIVATE)
        val jsonResponse = sharedPreferences.getString("json_response", "{}") ?: "{}"
        provideContent {
            //mock dataModel from jsonResponse
//            val jsonResponse = """
//                        {
//                            "activities-active-zone-minutes": [
//                                {"dateTime": "2024-02-10", "value": {"fatBurnActiveZoneMinutes": 23, "cardioActiveZoneMinutes": 12}},
//                                {"dateTime": "2024-02-11", "value": {"fatBurnActiveZoneMinutes": 30, "cardioActiveZoneMinutes": 18}}
//                            ]
//                        }
//                    """.trimIndent()

            val activeZoneMinutesList = parseJsonResponse(jsonResponse)
            val maxTotalMinutes = activeZoneMinutesList.maxOfOrNull { it.fatBurn + it.cardio } ?: 100

            val scaleFactor = when {
                maxTotalMinutes < 120 -> 1
                maxTotalMinutes < 200 -> 2
                maxTotalMinutes < 300 -> 3
                else -> 1
            }

            val openFitbitIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.fitbit.com/in-app/today")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            Row(
              modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(openFitbitIntent)),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeZoneMinutesList.forEach { data ->
                    Column(
                        modifier = GlanceModifier.height(140.dp).width(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.dayOfWeek,
                            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White)),
                            modifier = GlanceModifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = GlanceModifier.fillMaxHeight().defaultWeight().width(20.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column {
                                Box(
                                    modifier = GlanceModifier.height(data.cardio.dp/scaleFactor).width(20.dp)
                                        .background(ColorProvider(Color(0xFFFF5722)))
                                ) {}
                                Box(
                                    modifier = GlanceModifier.height(data.fatBurn.dp/scaleFactor).width(20.dp)
                                        .background(ColorProvider(Color(0xFFfab31b)))
                                ) {}

                            }
                        }
                        Text(
                            text = "${data.fatBurn + data.cardio}",
                            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.White)),
                            modifier = GlanceModifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

