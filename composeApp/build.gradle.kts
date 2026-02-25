plugins {
    alias(libs.plugins.expenseshare.android.application)
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.compose.multiplatform)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.jacoco)
}

android {
    namespace = "org.milad.expense_share"
    defaultConfig {
        applicationId = "org.milad.expense_share"
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.kotzilla.sdk.compose)
        }
        iosMain.dependencies {
            implementation(libs.kotzilla.sdk.compose)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.common)
            implementation(projects.core.navigation)
            implementation(projects.core.currency)
            implementation(projects.core.logger)
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.milad.expense_share.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "org.milad.expense_share"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}