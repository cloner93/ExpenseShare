plugins {
    alias(libs.plugins.expenseshare.ktor.server)

}

group = "org.milad.expense_share"
version = "1.0.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.core.currency)
}