import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// ----------------------------------------------------------------
// Đọc GROQ_API_KEY từ local.properties (file này KHÔNG commit lên git)
// Nếu chưa cấu hình, trả về chuỗi rỗng để app vẫn build được,
// AiChatRepository sẽ báo lỗi rõ ràng cho người dùng lúc chạy.
// ----------------------------------------------------------------
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val groqApiKey: String = localProperties.getProperty("GROQ_API_KEY", "")
val usdaApiKey: String = localProperties.getProperty("USDA_API_KEY", "")

android {
    namespace = "com.example.moblie_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.moblie_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Đưa API key vào BuildConfig để dùng trong code: BuildConfig.GROQ_API_KEY
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "USDA_API_KEY", "\"$usdaApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")

    // Google Fit (legacy assignment requirement; see ActivityFragment for sensor-first flow)
    implementation("com.google.android.gms:play-services-fitness:21.2.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ZXing Barcode Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
