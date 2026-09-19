import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
}

android {
    namespace = "prayit.simplebudget.androidApp"
    compileSdk = project.property("ANDROID_COMPILE_SDK").toString().toInt()

    defaultConfig {
        minSdk = project.property("ANDROID_MIN_SDK").toString().toInt()
        targetSdk = project.property("ANDROID_TARGET_SDK").toString().toInt()

        applicationId = "prayit.simplebudget.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        resValues = true
    }

    signingConfigs {
        create("release") {
            // Secrets come from local.properties or env (RELEASE_STORE_FILE etc.).
            // Never commit them — see README. Missing values fail only when signing.
            val localProps = Properties().apply {
                rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()
                    ?.use { load(it) }
            }

            fun secret(name: String): String? =
                System.getenv(name) ?: localProps.getProperty(name)
                ?: project.findProperty(name) as? String
            storeFile =
                secret("RELEASE_STORE_FILE")?.let(::file) ?: rootProject.file("release-key.jks")
            storePassword = secret("RELEASE_STORE_PASSWORD")
            keyAlias = secret("RELEASE_KEY_ALIAS")
            keyPassword = secret("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    bundle {
        abi {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:export"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(kotlin("test"))
}
