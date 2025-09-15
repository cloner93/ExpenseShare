plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.pmb.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
//    implementation(libs.androidx.core.ktx)

    implementation(libs.kotlinx.serialization.json)
}