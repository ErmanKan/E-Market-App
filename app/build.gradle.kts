plugins {
    id("kotlin-kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.ermanderici.casestudy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ermanderici.casestudy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            resources.excludes.add("META-INF/*")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.firebase.appdistribution.gradle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.github.glide)
    // Gson Converter
    implementation(libs.converter.gson)
    implementation(libs.gson)
    //Hilt
    implementation(libs.hilt.android) // Check for latest Hilt version
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler) // For Java projects

    // To use Kotlin annotation processing tool (kapt) for Room
    kapt(libs.androidx.room.compiler.v220rc01)

    // Optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)



}