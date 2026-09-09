plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "prayit.simplebudget.core.export"
        compileSdk = project.property("ANDROID_COMPILE_SDK").toString().toInt()
        minSdk = project.property("ANDROID_MIN_SDK").toString().toInt()
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    jvm()

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:utils"))
            implementation(libs.kotlinx.datetime)
            implementation(libs.kexcel)
            implementation(libs.kmp.zip)
        }
        androidMain.dependencies {
            implementation(project(":core:data"))
            implementation(libs.androidx.core.ktx)
        }
    }
}
