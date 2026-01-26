import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.org.jetbrains.kotlin.serialization)
}

android {
    namespace = "org.ethereumhpone.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Load secrets from local.properties
        val properties = Properties()
        properties.load(rootProject.file("local.properties").inputStream())

        val alchemyApi = properties.getProperty("ALCHEMY_API") ?: ""
        val tokenPriceApi = properties.getProperty("TOKEN_PRICE_API") ?: "fallback_value"
        val bundlerApi = properties.getProperty("BUNDLER_API") ?: ""

        buildConfigField("String", "ALCHEMY_API", "\"$alchemyApi\"")
        buildConfigField("String", "TOKEN_PRICE_API", "\"$tokenPriceApi\"")
        buildConfigField("String", "BUNDLER_API", "\"$bundlerApi\"")




    }

    buildFeatures {
        buildConfig = true
        aidl = true  // Enable AIDL for OS-level IPC with MsgSyncService
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

kapt {
    javacOptions {
        option("--release", "8")       // or  option("-source", "8"); option("-target", "8")
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(project(":core:database"))
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":android-smsmms"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))

    implementation(libs.ens)
    implementation(libs.model)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Web3j for Ethereum RPC
    implementation(libs.core)


    implementation(project(":core:domain"))
    implementation(project(":core:database"))


    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation("io.michaelrocks:libphonenumber-android:8.13.28")

    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation(libs.bundles.media3)

    implementation(libs.xmtp)
    implementation(libs.walletsdk)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.lifecycle.process)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    implementation("com.vdurmont:emoji-java:5.1.1") // emoji parser

    implementation(project(":dgenlibrary"))
    implementation("androidx.compose.ui:ui-graphics") // Required for showDgenToast Color parameter
    implementation("com.github.Nicola-Ceornea:BaseNameResolver:1.0.2")

    implementation(libs.core)


}