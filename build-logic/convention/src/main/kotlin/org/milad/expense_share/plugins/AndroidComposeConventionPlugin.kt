package org.milad.expense_share.plugins

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.milad.expense_share.ext.configureAndroidCompose

class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val commonExtension = extensions.getByType<CommonExtension<*, *, *, *, *, *>>()
        configureAndroidCompose(commonExtension)
    }
}
