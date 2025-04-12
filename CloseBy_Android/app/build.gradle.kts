plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //firebase
   // id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.closeby"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.closeby"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:22.3.0"))
    implementation("com.google.firebase:firebase-auth:21.0.6")  // Firebase Authentication
    implementation("com.google.firebase:firebase-core:21.1.0") // Core for Firebase
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.play.services.tasks)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}