import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.internal.platform.wasm.WasmPlatforms.wasmJs
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
    id("io.mockative")
    alias(libs.plugins.jacoco)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}
kotlin {
    androidLibrary {
        namespace = "org.milad.expense_share.domain"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
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
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation("io.mockative:mockative:3.0.1")
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.core.currency)
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation("io.mockative:mockative:3.0.1")
            }
        }
        jvmMain.dependencies {
            implementation(kotlin("reflect"))
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(kotlin("reflect"))
        }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            filter {
                isFailOnNoMatchingTests = false
            }
            testLogging {
                showExceptions = true
                showStandardStreams = true
                events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
                )
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
    }
}