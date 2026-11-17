plugins {
    alias(libs.plugins.expenseshare.ktor.server)

}

group = "org.milad.expense_share"
version = "1.0.4"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    
    val isDevelopment: Boolean = project.findProperty("development")?.toString()?.toBoolean() ?: true
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.core.currency)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    testImplementation("io.mockk:mockk:1.13.12")
}

tasks.test {
    useJUnitPlatform()
}