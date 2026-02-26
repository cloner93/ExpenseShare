package org.milad.expense_share.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.milad.expense_share.ext.configureKotlinJvmTarget
import org.milad.expense_share.ext.libs

class KotlinMultiplatformConventionPlugin : Plugin<Project> {

    @OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")

        extensions.configure<KotlinMultiplatformExtension> {

            pluginManager.withPlugin("com.android.application") {
                androidTarget {
                    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
                }
            }
            pluginManager.withPlugin("com.android.library") {
                androidTarget {
                    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
                }
            }

            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64(),
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }

            jvm()

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser {
                    binaries.executable()
                    val rootDirPath = project.rootDir.path
                    val projectDirPath = project.projectDir.path
                    commonWebpackConfig {
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static = (static ?: mutableListOf()).apply {
                                add(rootDirPath)
                                add(projectDirPath)
                            }
                        }
                    }
                }
            }

            applyDefaultHierarchyTemplate()

            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                    implementation(libs.findLibrary("kotlinx-serialization-json").get())
                    implementation(project.dependencies.platform(libs.findLibrary("koin-bom").get()))
                    implementation(libs.findLibrary("koin-core").get())
                }
                commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                }
                androidMain.dependencies {
                    implementation(libs.findLibrary("kotlinx-coroutines-android").get())
                }
                getByName("jvmMain").dependencies {
                    implementation(libs.findLibrary("kotlinx-coroutines-swing").get())
                }
            }
        }

        configureKotlinJvmTarget()
    }
}