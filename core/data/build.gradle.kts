plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
    alias(libs.plugins.expenseshare.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.jacoco)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

kotlin {
    androidLibrary {
        namespace = "org.milad.expense_share.data"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.network)
            implementation(projects.core.currency)
        }

        androidMain.dependencies {
            // ContextHolder access from core:network
        }

        findByName("jvmMain")?.dependencies {
            implementation(kotlin("reflect"))
        }

        findByName("jvmTest")?.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(kotlin("reflect"))
        }

        commonTest.dependencies {
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
            implementation(libs.kotest.extensions.koin)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

