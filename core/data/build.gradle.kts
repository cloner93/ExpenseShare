plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "org.milad.expense_share.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.network)
            implementation(projects.core.currency)
        }
    }
}
