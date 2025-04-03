@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt) // Put kapt back
    // alias(libs.plugins.org.jetbrains.kotlin.serialization)
    kotlin("plugin.serialization") version "2.0.20"
}

android {
    namespace = "org.ethereumhpone.database"
    compileSdk = 34

    defaultConfig {
        minSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments.put("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        // Force Kotlin to use version 1.9
        languageVersion = "1.9"
        apiVersion = "1.9"
        // Add freeCompilerArgs to force compatibility
        freeCompilerArgs = listOf("-Xskip-prerelease-check", "-Xskip-metadata-version-check")
    }
}

// Configure kapt to be less strict
kapt {
    correctErrorTypes = true
    useBuildCache = false
    includeCompileClasspath = false
    arguments {
        arg("kapt.incremental.apt", "false")
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.bundles.roomb) {
        exclude(group = "com.intellij", module = "annotations")
    }
    kapt(libs.bundles.roomb) {
        exclude(group = "com.intellij", module = "annotations")
    }

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Exclude transitive Kotlin dependencies that might be causing issues
    implementation(libs.xmtp) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
    }

    implementation(project(":core:model"))

    implementation(libs.kotlinx.datetime)
}