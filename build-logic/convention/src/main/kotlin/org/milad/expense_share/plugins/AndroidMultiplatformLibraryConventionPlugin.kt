package org.milad.expense_share.plugins

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.milad.expense_share.ext.libs

class AndroidMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.kotlin.multiplatform.library")
        }

        extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
            val compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
            val minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

            this.compileSdk = compileSdk
            this.minSdk = minSdk
        }
    }
}
