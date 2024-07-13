import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)

}

android {
    namespace = "org.ethereumhpone.chat"
    compileSdk = 34

    defaultConfig {
        minSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val properties =  Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField("String", "ETHEREUM_API", "\"${properties.getProperty("ETHEREUM_API")}\"")
        buildConfigField("String", "SEPOLIA_API", "\"${properties.getProperty("SEPOLIA_API")}\"")
        buildConfigField("String", "ARBITRUM_API", "\"${properties.getProperty("ARBITRUM_API")}\"")
        buildConfigField("String", "OPTIMISM_API", "\"${properties.getProperty("OPTIMISM_API")}\"")
        buildConfigField("String", "POLYGON_API", "\"${properties.getProperty("POLYGON_API")}\"")
        buildConfigField("String", "BASE_API", "\"${properties.getProperty("BASE_API")}\"")
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
        buildConfig = true
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
    implementation(project(":core:domain"))
    implementation(project(":core:database"))



    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.compose.material3)
    implementation(project(":core:common"))
    implementation(libs.androidx.constraintlayout)
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")

    implementation("com.github.EthereumPhone:ethOS-Component-Library:27d4348853")

    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)

    implementation(libs.compose.navigation)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")


    //animation
    implementation("androidx.compose.animation:animation:1.6.7")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.0-rc01")

    
    implementation(libs.rpc)
    implementation(libs.model)

    // Web3j needed for the WalletSDK
    implementation(libs.core)
    implementation(libs.walletsdk)


    // cameraX
    implementation("com.google.guava:guava:31.0.1-android")
    implementation(libs.bundles.camerax)

    // vCard
    implementation("com.googlecode.ez-vcard:ez-vcard:0.10.6")

    // mediaplayer
    implementation(libs.bundles.media3)




}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
