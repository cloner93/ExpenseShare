package org.milad.expense_share.plugins

import org.milad.expense_share.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            val compose = extensions.getByType(ComposeExtension::class.java)

            sourceSets.apply {
                commonMain.dependencies {
                    // ── Compose core ──────────────────────────────────────────
                    implementation(compose.dependencies.runtime)
                    implementation(compose.dependencies.foundation)
                    implementation(compose.dependencies.material3)
                    implementation(compose.dependencies.ui)
                    implementation(compose.dependencies.components.resources)
                    implementation(compose.dependencies.components.uiToolingPreview)

                    // ── Navigation Suite (NavigationSuiteType lives here) ──────
                    implementation(compose.dependencies.material3AdaptiveNavigationSuite)

                    // ── Material3 Adaptive ────────────────────────────────────
                    implementation(libs.findBundle("compose-adaptive").get())

                    // ── Navigation ────────────────────────────────────────────
                    implementation(libs.findLibrary("jetbrains-navigation-compose").get())

                    // ── Material Icons ────────────────────────────────────────
                    implementation(libs.findLibrary("material-icons-extended").get())

                    // ── Lifecycle ─────────────────────────────────────────────
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
                    implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())

                    // ── Koin Compose ──────────────────────────────────────────
                    implementation(project.dependencies.platform(libs.findLibrary("koin-bom").get()))
                    implementation(libs.findLibrary("koin-compose-viewmodel").get())
                    implementation(libs.findLibrary("koin-core").get())
                }

                androidMain.dependencies {
                    implementation(compose.dependencies.preview)
                    implementation(libs.findLibrary("androidx-activity-compose").get())
                    implementation(libs.findLibrary("koin-android").get())
                }

                getByName("jvmMain").dependencies {
                    implementation(compose.dependencies.desktop.currentOs)
                }
            }
        }
    }
}
