package com.visuale.azmwidget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.visuale.azmwidget.ui.theme.AndroidWidgetWithComposeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.visuale.azmwidget.glance_widget.widget.FitbitWidget


class MainActivity : ComponentActivity() {
    private val clientId = BuildConfig.CLIENT_ID
    private val clientSecret = BuildConfig.CLIENT_SECRET
    private val redirectUri = BuildConfig.REDIRECT_URI

    private val authUrl =
        "https://www.fitbit.com/oauth2/authorize?response_type=code&client_id=$clientId&redirect_uri=$redirectUri&scope=activity&expires_in=604800"
    private val tokenUrl = "https://api.fitbit.com/oauth2/token"
    private val apiUrl =
        "https://api.fitbit.com/1/user/-/activities/active-zone-minutes/date/today/7d.json"

    private var accessToken by mutableStateOf<String?>(null)
    private var dataFetchStatus by mutableStateOf("Waiting for data...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidWidgetWithComposeTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Greeting(dataFetchStatus)
                }
            }
        }

        when {
            intent?.data?.host == "callback" -> handleIntent(intent)
            accessToken == null -> launchFitbitAuth()
            else -> fetchFitbitData()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun launchFitbitAuth() = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))

    private fun handleIntent(intent: Intent) {
        val code = intent.data?.getQueryParameter("code") ?: return
        Log.e("FitbitOAuth", "Auth code missing")
        exchangeCodeForToken(code)
    }

    private fun exchangeCodeForToken(authCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val encodedCredentials =
                    Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)

                val requestBody = FormBody.Builder()
                    .add("client_id", clientId)
                    .add("grant_type", "authorization_code")
                    .add("redirect_uri", redirectUri)
                    .add("code", authCode)
                    .build()

                val request = Request.Builder()
                    .url(tokenUrl)
                    .post(requestBody)
                    .addHeader("Authorization", "Basic $encodedCredentials")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build()

                val responseBody = client.newCall(request).execute().body?.string()
                val token = JSONObject(responseBody ?: "").optString("access_token")
                if (token.isNullOrEmpty()) throw Exception("Token missing")

                accessToken = token
                fetchFitbitData()

                saveJsonToPreferences("last7dData", responseBody ?: "")
            } catch (e: Exception) {
                Log.e("FitbitOAuth", "Error: ${e.message}")
                runOnUiThread { dataFetchStatus = "Error: ${e.message}" }
            }
        }
    }

    private fun fetchFitbitData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val responseBody = OkHttpClient().newCall(request).execute().body?.string()

                if (!responseBody.isNullOrEmpty()) {
                    saveJsonToPreferences("json_response", responseBody)
                    runOnUiThread { dataFetchStatus = "Success" }
                    // Update widget after new data is saved
                    FitbitWidget().updateAll(this@MainActivity)
                } else {
                    runOnUiThread { dataFetchStatus = "Error fetching data" }
                }
            } catch (e: Exception) {
                Log.e("FitbitAPI", "Error: ${e.message}")
                runOnUiThread { dataFetchStatus = "Error: ${e.message}" }
            }
        }
    }

    private fun saveJsonToPreferences(key: String, json: String) {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("7dAzmFitbit", Context.MODE_PRIVATE)

        sharedPreferences.edit().remove(key).apply()
        sharedPreferences.edit().putString(key, json).apply()

        // Update the widget
        CoroutineScope(Dispatchers.Main).launch {
            val manager = GlanceAppWidgetManager(this@MainActivity)
            val glanceIds = manager.getGlanceIds(FitbitWidget::class.java)

            glanceIds.forEach { _ ->
                FitbitWidget().updateAll(this@MainActivity)
            }

            FitbitWidget().updateAll(this@MainActivity)
        }
    }
}

@Composable
fun Greeting(text: String) = Text(text = text)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() = AndroidWidgetWithComposeTheme { Greeting("Waiting for data...") }
