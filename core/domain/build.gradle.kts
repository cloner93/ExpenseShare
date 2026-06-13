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
        namespace = "org.milad.expense_share.domain"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.currency)
            implementation(libs.mockative)
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.mockative)
            }
        }
        jvmMain.dependencies {
            implementation(kotlin("reflect"))
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(kotlin("reflect"))
        }
        
        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }
    }
}
