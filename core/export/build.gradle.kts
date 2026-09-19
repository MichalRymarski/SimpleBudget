@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
                // NB: withAndroidTarget() is intentionally NOT used here — KGP silently
                // ignores it inside custom groups, leaving androidMain wired straight to
                // commonMain (see dumpHierarchy). The edge is added explicitly below.
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
            implementation(libs.kotlinx.datetime)
            implementation(libs.kmp.zip)
        }
        val nonJsMain by getting {
            dependencies {
                implementation(libs.kexcel)
            }
        }
        androidMain {
            dependsOn(nonJsMain)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.android.mail)
            implementation(libs.android.activation)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
