@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "org.ethereumhpone.common"
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

    configurations {
        all { // You should exclude one of them not both of them
            //exclude(group = "com.android.support", module = "support-compat")
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)






    implementation("com.github.bumptech.glide:glide:4.9.0") {
        exclude(group = "com.android.support", module = "support-compat")
    }
    implementation("com.github.bumptech.glide:gifencoder-integration:4.9.0") {
        exclude(group = "com.android.support", module = "support-compat")
    }
    


    kapt("com.github.bumptech.glide:compiler:4.9.0")





}