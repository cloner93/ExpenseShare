package org.milad.expense_share.plugins

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.milad.expense_share.ext.configureKotlinJvmTarget
import org.milad.expense_share.ext.libs

class KtorServerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.jvm")
            apply("io.ktor.plugin")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        dependencies {
            // ── Ktor Server bundle ────────────────────────────────────────────
            "implementation"(libs.findBundle("ktor-server").get())

            // ── Database ──────────────────────────────────────────────────────
            "implementation"(libs.findBundle("exposed").get())
            "implementation"(libs.findLibrary("postgresql").get())
            "implementation"(libs.findLibrary("hikari").get())

            // ── Logging ───────────────────────────────────────────────────────
            "implementation"(libs.findLibrary("logback").get())

            // ── DI ────────────────────────────────────────────────────────────
            "implementation"(project.dependencies.platform(libs.findLibrary("koin-bom").get()))
            "implementation"(libs.findLibrary("koin-ktor").get())
            "implementation"(libs.findLibrary("koin-core").get())

            // ── Serialization ─────────────────────────────────────────────────
            "implementation"(libs.findLibrary("kotlinx-serialization-json").get())

            // ── jbcrypt ─────────────────────────────────────────────────
            "implementation"(libs.findLibrary("jbcrypt").get())

            // ── Testing ───────────────────────────────────────────────────────
            "testImplementation"(libs.findBundle("kotest").get())
            "testImplementation"(libs.findLibrary("ktor-server-test-host").get())
            "testImplementation"(libs.findLibrary("koin-test-junit5").get())
        }

        configureKotlinJvmTarget()
    }
}
