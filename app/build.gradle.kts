plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.handartracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.handartracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    // Pour les modèles MediaPipe
    aaptOptions {
        noCompress("tflite", "task")
    }
}

dependencies {
    // Dépendances Android de base
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.11.0")

    // Google AR Core
    implementation("com.google.ar:core:1.40.0")

    // MediaPipe pour le suivi de la main
    implementation("com.google.mediapipe:tasks-vision:0.10.10")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}