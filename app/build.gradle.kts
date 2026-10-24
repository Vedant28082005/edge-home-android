plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing comes from CI secrets (see .github/workflows/build-apk.yml).
// With no keystore configured, release builds fall back to the debug key so
// local and pull-request builds still work. CI sets REQUIRE_RELEASE_SIGNING
// on main so a published APK is never signed with the throwaway key.
val releaseKeystore: String? = System.getenv("ANDROID_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
val requireReleaseSigning = System.getenv("REQUIRE_RELEASE_SIGNING") == "true"

android {
    namespace = "com.edgehome.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edgehome.app"
        minSdk = 26
        // 34 keeps the classic colored status and navigation bars
        targetSdk = 34
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "0.2.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"
        buildConfigField("String", "DEFAULT_GATEWAY_URL", "\"https://homebench.tailffe052.ts.net\"")
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore != null) {
                storeFile = file(releaseKeystore)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = when {
                releaseKeystore != null -> signingConfigs.getByName("release")
                requireReleaseSigning -> throw GradleException(
                    "Release signing is required but ANDROID_KEYSTORE_PATH is not set. " +
                        "Add the ANDROID_KEYSTORE_BASE64, ANDROID_KEYSTORE_PASSWORD, " +
                        "ANDROID_KEY_ALIAS and ANDROID_KEY_PASSWORD repository secrets."
                )
                else -> signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}
