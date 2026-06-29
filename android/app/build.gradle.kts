plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(17)
}

val qnnVersion: String? = project.findProperty("qnnVersion") as? String
val executorchVersion: String = project.findProperty("executorchVersion") as? String ?: "1.0.0"
val useLocalAar: Boolean = (project.findProperty("useLocalAar") as? String)?.toBoolean() ?: false
val enableQnnBackend: Boolean =
    (project.findProperty("enableQnnBackend") as? String)?.toBoolean() ?: true
val autoLoadQwen: Boolean =
    (project.findProperty("autoLoadQwen") as? String)?.toBoolean() ?: false

android {
    namespace = "com.medic.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.medic.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2-hackathon"
        ndk { abiFilters += listOf("arm64-v8a") }
        buildConfigField("boolean", "ENABLE_QNN_BACKEND", enableQnnBackend.toString())
        buildConfigField("boolean", "AUTO_LOAD_QWEN", autoLoadQwen.toString())
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
            // Prefer the explicit qnn-runtime AAR over copies bundled inside executorch-android-qnn.
            pickFirsts += listOf(
                "**/libc++_shared.so",
                "**/libQnnHtp.so",
                "**/libQnnSystem.so",
                "**/libQnnHtpV*.so",
                "**/libQnnHtpPrepare.so",
                "**/libQnnCpu.so"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
    if (useLocalAar) {
        implementation(files("libs/executorch.aar"))
    } else {
        implementation("org.pytorch:executorch-android-qnn:$executorchVersion")
    }
    implementation("com.facebook.soloader:soloader:0.10.5")
    implementation("com.facebook.fbjni:fbjni:0.7.0")
    if (!qnnVersion.isNullOrEmpty()) {
        implementation("com.qualcomm.qti:qnn-runtime:$qnnVersion")
    }
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
