import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springBoot)
}

group = "com.viniciuscoscia.kmpfullstackplayground"
version = "1.0.0"
kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.ai.bom))

    api(projects.core)
    implementation(projects.features.substanceCatalog.contract)
    implementation(kotlin("reflect"))
    implementation(libs.apache.poi.ooxml)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.spring.ai.mcp.server.webmvc)
    implementation(libs.spring.boot.flyway)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.modulith.starter.core)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotlin.testJunit)
    testRuntimeOnly(libs.h2)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
