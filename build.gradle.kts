plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
    id("com.google.gms.google-services") version "4.4.2" apply false // ✅ esto es lo que faltaba
}
