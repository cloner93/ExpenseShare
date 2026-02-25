plugins {
    `kotlin-dsl`
}

group = "org.milad.expense_share.build_logic.convention"

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(embeddedKotlinVersion)
        }
    }
}

dependencies {
    compileOnly(libs.plugins.androidApplication.toDep())
    compileOnly(libs.plugins.androidMultiplatformLibrary.toDep())
    compileOnly(libs.plugins.kotlinAndroid.toDep())
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    compileOnly(libs.plugins.kotlinJvm.toDep())
    compileOnly(libs.plugins.kotlinSerialization.toDep())
    compileOnly(libs.plugins.composeMultiplatform.toDep())
    compileOnly(libs.plugins.composeCompiler.toDep())
    compileOnly(libs.plugins.ktor.toDep())
}

fun Provider<PluginDependency>.toDep() = map {plugin->
    "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version}".also {
        println("plugin: ${plugin.pluginId} ${plugin.version}")
    }
}

gradlePlugin {
    plugins {

        // ── Android ──────────────────────────────────────────────────────────
        register("androidApplication") {
            id = "expenseshare.android.application"
            implementationClass = "org.milad.expense_share.plugins.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "expenseshare.android.library"
            implementationClass = "plugins.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "expenseshare.android.compose"
            implementationClass = "plugins.AndroidComposeConventionPlugin"
        }

        // ── Kotlin Multiplatform ──────────────────────────────────────────────
        register("kotlinMultiplatform") {
            id = "expenseshare.kotlin.multiplatform"
            implementationClass = "org.milad.expense_share.plugins.KotlinMultiplatformConventionPlugin"
        }
        register("composeMultiplatform") {
            id = "expenseshare.compose.multiplatform"
            implementationClass = "org.milad.expense_share.plugins.ComposeMultiplatformConventionPlugin"
        }

        // ── JVM ───────────────────────────────────────────────────────────────
        register("kotlinJvm") {
            id = "expenseshare.kotlin.jvm"
            implementationClass = "org.milad.expense_share.plugins.KotlinJvmConventionPlugin"
        }

        // ── Server ────────────────────────────────────────────────────────────
        register("ktorServer") {
            id = "expenseshare.ktor.server"
            implementationClass = "org.milad.expense_share.plugins.KtorServerConventionPlugin"
        }

        // ── Cross-cutting ─────────────────────────────────────────────────────
        register("kotlinSerialization") {
            id = "expenseshare.kotlin.serialization"
            implementationClass = "org.milad.expense_share.plugins.KotlinSerializationConventionPlugin"
        }
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}
