package com.visuale.azmwidget

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

//@Composable
//fun JsonResponseDisplay(jsonResponse: String, modifier: Modifier = Modifier) {
//    Text(
//        text = jsonResponse,
//        modifier = modifier
//    )
//}

@Composable
private fun SaveJsonToPreferences(json: String) {
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("fitbit_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("json_response", json).apply()
}


