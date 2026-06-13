plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "org.milad.expense_share.logger"
    }

    jvm()
}
