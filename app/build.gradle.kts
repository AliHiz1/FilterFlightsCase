plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.alihiz.filterflightscase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alihiz.filterflightscase"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Resolve secrets from (in order): -P Gradle props -> ENV vars -> local.properties
        val lp = org.jetbrains.kotlin.konan.properties.Properties()
        val lpFile = rootProject.file("local.properties")
        if (lpFile.exists()) lp.load(lpFile.inputStream())

        fun readSecret(key: String, default: String = ""): String {
            val fromGradle = project.findProperty(key) as String?
            val fromEnv = System.getenv(key)
            val fromLocal = lp.getProperty(key)
            return fromGradle ?: fromEnv ?: fromLocal ?: default
        }

        // Google Maps API Key (used as manifest placeholder)
        val mapsApiKey = readSecret("MAPS_API_KEY", "")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey.ifBlank { "YOUR_API_KEY_HERE" }

        // OpenSky OAuth2
        val openskyClientId = readSecret("OPENSKY_CLIENT_ID")
        val openskyClientSecret = readSecret("OPENSKY_CLIENT_SECRET")
        buildConfigField("String", "OPENSKY_CLIENT_ID", "\"$openskyClientId\"")
        buildConfigField("String", "OPENSKY_CLIENT_SECRET", "\"$openskyClientSecret\"")

        // OpenSky Basic (legacy)
        val openskyUsername = readSecret("OPENSKY_USERNAME")
        val openskyPassword = readSecret("OPENSKY_PASSWORD")
        buildConfigField("String", "OPENSKY_USERNAME", "\"$openskyUsername\"")
        buildConfigField("String", "OPENSKY_PASSWORD", "\"$openskyPassword\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Google Maps
    implementation(libs.google.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    implementation(libs.okhttp.logging)
    
    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    
    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}