import java.util.Properties

// 这个文件是 app 模块的 Gradle 构建脚本。
// 初学者可以把它理解为“Android Studio 如何编译、打包、签名这个 App”的说明书。
plugins {
    alias(libs.plugins.android.application)
}

// ReleaseSigningProperties 保存正式版签名需要的四个字段。
// 这些值不应该写死在仓库里，所以后面会从 local.properties 读取。
data class ReleaseSigningProperties(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String
)

// local.properties 通常只存在于开发者本机，用来放 SDK 路径或私密配置。
// 这里用 Properties 读取它，是为了在本机有签名配置时自动启用 release 签名。
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

// takeIf { it.isNotBlank() } 可以把空字符串当作“没有配置”处理。
fun localProperty(name: String): String? = localProperties.getProperty(name)?.takeIf { it.isNotBlank() }

// 四个签名字段必须同时存在才创建 release 签名配置。
// 少一个就返回 null，Gradle 仍然可以正常构建 debug 包。
val releaseSigningProperties = run {
    val storeFile = localProperty("RELEASE_STORE_FILE")
    val storePassword = localProperty("RELEASE_STORE_PASSWORD")
    val keyAlias = localProperty("RELEASE_KEY_ALIAS")
    val keyPassword = localProperty("RELEASE_KEY_PASSWORD")

    if (storeFile != null && storePassword != null && keyAlias != null && keyPassword != null) {
        ReleaseSigningProperties(storeFile, storePassword, keyAlias, keyPassword)
    } else {
        null
    }
}

// 用 git 提交数量生成 versionCode。
// 这样每次提交后版本号都会自然递增，适合这个项目按提交发布 APK 的工作流。
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

// 把提交数量格式化成 0.0.0.0 这样的 versionName。
// 例如提交数 123 会显示成 0.1.2.3，用户更容易读。
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
    // namespace 决定 R、BuildConfig 等生成类所在的 Kotlin/Java 包名。
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
        // applicationId 是系统安装包名，设备上用它区分不同 App。
        applicationId = "com.example.videobrowser"
        minSdk = 24
        targetSdk = 36
        versionCode = gitCommitCount.coerceAtLeast(1)
        versionName = formatCommitCountVersion(gitCommitCount)

        buildConfigField("int", "GIT_COMMIT_COUNT", gitCommitCount.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        releaseSigningProperties?.let { properties ->
            create("release") {
                storeFile = file(properties.storeFile)
                storePassword = properties.storePassword
                keyAlias = properties.keyAlias
                keyPassword = properties.keyPassword
            }
        }
    }

    buildTypes {
        release {
            releaseSigningProperties?.let {
                signingConfig = signingConfigs.getByName("release")
            }
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
    // AndroidX 和 Material 负责 Activity、兼容控件、布局、播放器等基础能力。
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
