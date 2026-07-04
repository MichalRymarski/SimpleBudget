import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
}

android {
    namespace = "prayit.simplebudget.androidApp"
    compileSdk = project.property("ANDROID_COMPILE_SDK").toString().toInt()

    defaultConfig {
        minSdk = project.property("ANDROID_MIN_SDK").toString().toInt()
        targetSdk = 36

        applicationId = "prayit.simplebudget.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("release-key.jks")
            storePassword = "simplebudget123"
            keyAlias = "simplebudget"
            keyPassword = "simplebudget123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.androidx.activityCompose)
}
