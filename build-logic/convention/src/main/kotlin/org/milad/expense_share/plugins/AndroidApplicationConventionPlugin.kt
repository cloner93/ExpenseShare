package org.milad.expense_share.plugins

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.milad.expense_share.ext.configureAndroidCommon
import org.milad.expense_share.ext.libs

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.application")
        }

        extensions.configure<ApplicationExtension> {
            configureAndroidCommon(this)

            defaultConfig {
                targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()
                versionCode = 1
                versionName = "1.0.0"
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                }
            }

            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }
    }
}