import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springBoot)
}

group = "com.akira.todoist"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter.web)
    implementation(kotlin("reflect"))

    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
