plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
    alias(libs.plugins.expenseshare.compose.multiplatform)
}

kotlin {
    androidLibrary {
        namespace = "org.milad.common"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)
        }
    }
}
