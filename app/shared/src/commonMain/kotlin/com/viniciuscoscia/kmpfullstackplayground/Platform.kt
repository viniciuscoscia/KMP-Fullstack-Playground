package com.viniciuscoscia.kmpfullstackplayground

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform