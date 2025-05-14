// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // Dependencia del plugin (¡debe estar aquí!)
        classpath (libs.gradle)
        classpath(libs.secrets.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false

    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version ("2.0.1")


}