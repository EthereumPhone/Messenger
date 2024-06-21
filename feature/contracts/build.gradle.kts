import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)


}

android {
    namespace = "org.ethereumhpone.contracts"
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




    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.4.3")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.compose.foundation:foundation:1.6.0-alpha02")
    implementation(project(":feature:chat"))
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)

    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))








    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.compose.material3)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("junit:junit:4.12")
    debugImplementation("androidx.compose.ui:ui-tooling")




    implementation(libs.coil.compose.v210)
    implementation("com.github.EthereumPhone:ethOS-Component-Library:1a10060494")

    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)

    implementation("androidx.navigation:navigation-compose:2.7.6")

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")


    implementation(libs.hilt.navigation)

    implementation("com.google.accompanist:accompanist-permissions:0.32.0")





}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}