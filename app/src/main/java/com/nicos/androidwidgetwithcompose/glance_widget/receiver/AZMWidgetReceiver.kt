package com.nicos.androidwidgetwithcompose.glance_widget.receiver

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.nicos.androidwidgetwithcompose.glance_widget.widget.FitbitWidget

class AZMWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FitbitWidget()
}