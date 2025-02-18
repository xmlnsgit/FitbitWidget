package com.visuale.azmwidget.glance_widget.receiver

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.visuale.azmwidget.glance_widget.widget.FitbitWidget

class AZMWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FitbitWidget()
}