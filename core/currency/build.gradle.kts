plugins {
    alias(libs.plugins.expenseshare.kotlin.multiplatform)
    alias(libs.plugins.expenseshare.android.multiplatform.library)
    alias(libs.plugins.expenseshare.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "org.milad.expense_share.currency"
    }

    jvm()
}
