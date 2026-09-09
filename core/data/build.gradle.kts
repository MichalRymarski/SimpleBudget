import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
}

kotlin {
    android {
        namespace = "prayit.simplebudget.core.data"
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

    wasmJs {
        browser()
        binaries.executable()
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonJs") {
                withAndroidTarget()
                withJvm()
                group("ios") {
                    withIos()
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(project(":core:utils"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        val nonJsMain by getting {
            dependencies {
                api(libs.room.runtime)
                implementation(libs.room.sqlite.bundled)
            }
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    with(libs.room.compiler) {
        add("kspAndroid", this)
        add("kspJvm", this)
        add("kspIosArm64", this)
        add("kspIosSimulatorArm64", this)
    }
}
