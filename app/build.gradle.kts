plugins {
    alias(libs.plugins.android.application)
}

fun gitCommitCount(): Int {
    return try {
        providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            isIgnoreExitValue = true
        }.standardOutput.asText.get().trim().toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
}

fun formatCommitCountVersion(commitCount: Int): String {
    val count = commitCount.coerceAtLeast(0)
    val major = count / 1000
    val hundreds = (count / 100) % 10
    val tens = (count / 10) % 10
    val ones = count % 10
    return "$major.$hundreds.$tens.$ones"
}

val gitCommitCount = gitCommitCount()

android {
    namespace = "com.example.videobrowser"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.videobrowser"
        minSdk = 24
        targetSdk = 36
        versionCode = gitCommitCount.coerceAtLeast(1)
        versionName = formatCommitCountVersion(gitCommitCount)

        buildConfigField("int", "GIT_COMMIT_COUNT", gitCommitCount.toString())

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

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.rtsp)
    implementation(libs.androidx.media3.exoplayer.smoothstreaming)
    implementation(libs.androidx.media3.effect)
    implementation(libs.androidx.media3.ui)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
