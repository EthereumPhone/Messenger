plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.ethereumphone.onboarding"
    compileSdk = 34

    defaultConfig {
        minSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        kotlinCompilerExtensionVersion = "1.5.5"
    }
}

dependencies {

    implementation(libs.androidx.core)


    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.4.3")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.compose.foundation:foundation:1.6.0-alpha02")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.compose.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:datastore"))
    testImplementation(libs.junit)
    androidTestImplementation("junit:junit:4.12")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.github.EthereumPhone:ethOS-Component-Library:5e20c8f4a0")

    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)

    implementation(libs.compose.navigation)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    implementation(libs.rpc)
    implementation(libs.model)

    // Web3j needed for the WalletSDK
    implementation(libs.core)
    implementation(libs.walletsdk)

    implementation(libs.xmtp)

}