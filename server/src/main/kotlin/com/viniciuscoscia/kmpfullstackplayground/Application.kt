package com.viniciuscoscia.kmpfullstackplayground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class SubstanceAtlasApplication

fun main(args: Array<String>) {
    runApplication<SubstanceAtlasApplication>(*args)
}
