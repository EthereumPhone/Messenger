

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")


    id("com.google.protobuf") version "0.8.17"



    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)


}

android {
    namespace = "org.ethereumhpone.messenger"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.ethereumhpone.messenger"
        minSdk = 32
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(project(":feature:chat"))
    implementation(project(":feature:contracts"))

    implementation("androidx.navigation:navigation-compose:2.7.6")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(libs.room.ktx)
    implementation(project(":core:datastore"))
    implementation(libs.androidx.datastore.core)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    // Jetpack DataStore
//    implementation("androidx.datastore:datastore:1.0.0")
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//
//
//    // Protocol Buffers
//    implementation("androidx.datastore:datastore-core:1.0.0") // Check for the latest version
//    implementation("com.google.protobuf:protobuf-javalite:3.18.0") // Check for the latest version
//    implementation("com.google.protobuf:protobuf-kotlin-lite:3.18.0")

    implementation("androidx.datastore:datastore:1.0.0")

    // optional - RxJava2 support
    implementation("androidx.datastore:datastore-rxjava2:1.0.0")

    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-rxjava3:1.0.0")


    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation)
}




// Allow references to generated code
kapt {
    correctErrorTypes = true
}