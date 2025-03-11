
plugins {
    id("com.android.application") version "8.7.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.android.library") version "8.7.1" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false // Use a version compatible with your Kotlin version


    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false

    alias(libs.plugins.org.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false

}



