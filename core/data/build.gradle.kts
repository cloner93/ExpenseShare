plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
    alias(libs.plugins.expenseshare.kotlin.serialization)
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
    }
}
