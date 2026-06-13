package org.milad.expense_share.plugins

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.milad.expense_share.ext.libs

class AndroidMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.kotlin.multiplatform.library")
        }

        val compileSdkValue = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        val minSdkVersionValue = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

        val kotlinExt = extensions["kotlin"] as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
        val androidLib = (kotlinExt as org.gradle.api.plugins.ExtensionAware).extensions.getByName("androidLibrary") as KotlinMultiplatformAndroidLibraryExtension
        
        androidLib.compileSdk = compileSdkValue
        androidLib.minSdk = minSdkVersionValue
    }
}
