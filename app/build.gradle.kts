plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.wastecollector"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wastecollector"
        minSdk = 24
        targetSdk = 34
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

    packaging {
        resources {
            excludes += setOf("META-INF/INDEX.LIST")
            excludes += setOf("META-INF/DEPENDENCIES")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.auth:google-auth-library-oauth2-http:1.17.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.41.3")
    implementation("com.google.api-client:google-api-client:1.34.1")
    implementation ("com.google.apis:google-api-services-sheets:v4-rev20241203-2.0.0")

    implementation("com.google.android.gms:play-services-maps:18.1.0")
}