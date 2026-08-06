package com.viniciuscoscia.kmpfullstackplayground

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun defaultServerBaseUrl(): String = "http://127.0.0.1:8080"
