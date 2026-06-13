plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
    alias(libs.plugins.expenseshare.kotlin.serialization)
    alias(libs.plugins.ksp) // Apply KSP before Kotest
    alias(libs.plugins.kotest)
    alias(libs.plugins.jacoco)
}

kotlin {
    androidLibrary {
        namespace = "com.pmb.expense_share.navigation"
    }

    jvm()
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}
