plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.niu.android.globally"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.niu.android.globally"
        minSdk = 21
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
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.google.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.firebase:firebase-auth:22.3.0")  // Firebase Authentication
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Google Sign-In
    implementation("com.google.firebase:firebase-firestore:24.9.1") // Firestore Database
    implementation("com.google.firebase:firebase-database:20.3.0") // Realtime Database
    implementation("com.google.firebase:firebase-storage:20.3.0") // Firebase Storage
}