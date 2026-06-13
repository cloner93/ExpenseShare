plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
    alias(libs.plugins.expenseshare.kotlin.serialization)
    alias(libs.plugins.ksp) // Apply KSP before Kotest
    alias(libs.plugins.kotest)
    id("io.mockative")
    alias(libs.plugins.jacoco)
}
jacoco {
    toolVersion = libs.versions.jacoco.get()
}


kotlin {
    androidLibrary {
        namespace = "org.milad.expense_share.network"
    }

    jvm()

    sourceSets {

        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.mockative)

            implementation(projects.core.logger)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        findByName("jvmMain")?.dependencies {
            implementation(kotlin("reflect"))
            implementation(libs.ktor.client.cio)
        }
        findByName("jvmTest")?.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(kotlin("reflect"))
        }
        findByName("wasmJsMain")?.dependencies {
            implementation(libs.ktor.client.js)
        }

        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.extensions.koin)
            implementation(libs.kotest.property)
            implementation(libs.ktor.client.content.negotiation)

            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.mockative)

        }
    }
}
