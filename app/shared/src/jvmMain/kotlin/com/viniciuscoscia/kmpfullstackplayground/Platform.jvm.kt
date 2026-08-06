package com.viniciuscoscia.kmpfullstackplayground

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun defaultServerBaseUrl(): String = "http://127.0.0.1:8080"
