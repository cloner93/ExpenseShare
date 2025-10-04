plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotest) apply false
    id("io.mockative") version "3.0.1" apply false
    alias(libs.plugins.jacoco)
}


tasks.register<JacocoReport>("jacocoFullReport") {
    group = "verification"
    description = "Aggregate JaCoCo coverage report for all modules."

    dependsOn(subprojects.map { it.tasks.matching { t -> t.name == "testDebugUnitTest" } })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val excludes = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "android/**/*.*"
    )

    val executionDataFiles = files(subprojects.map {
        fileTree("${it.buildDir}") {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }
    })
    executionData.setFrom(executionDataFiles)

    subprojects.forEach { subproject ->
        val javaSrc = "${subproject.projectDir}/src/main/java"
        val kotlinSrc = "${subproject.projectDir}/src/main/kotlin"
        sourceDirectories.from(files(javaSrc, kotlinSrc))

        val javaTree = fileTree("${subproject.buildDir}/intermediates/javac/debug/classes") {
            exclude(excludes)
        }
        val kotlinTree = fileTree("${subproject.buildDir}/tmp/kotlin-classes/debug") {
            exclude(excludes)
        }

        classDirectories.from(javaTree, kotlinTree)
    }
}
