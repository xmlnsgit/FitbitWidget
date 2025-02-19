# Android Widget for Fitbit AZM

This project sets up a widget in Android using Jetpack Compose Glance. It connects to fitbit API to refresh the fitbit active zone minutes for the last 7 days.

# Set up 

## Create a secret.properties file in the root of your project.

FITBIT_CLIENT_ID=XXXXXX
FITBIT_CLIENT_SECRET=
FITBIT_REDIRECT_URI=com.visuale.azmwidget://callback

# Versioning

Target SDK version: 35 <br />
Minimum SDK version: 28 <br />
Kotlin version: 2.1.0 <br />
Gradle version: 8.7.3 <br />

# References

https://developer.android.com/jetpack/compose/glance/create-app-widget  <br />