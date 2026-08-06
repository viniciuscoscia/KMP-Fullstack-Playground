package com.akira.todoist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodoistSyncBridgeApplication

fun main(args: Array<String>) {
    runApplication<TodoistSyncBridgeApplication>(*args)
}
