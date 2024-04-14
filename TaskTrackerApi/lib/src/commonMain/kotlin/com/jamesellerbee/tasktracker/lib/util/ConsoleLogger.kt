package com.jamesellerbee.tasktracker.lib.util

import com.jamesellerbee.tasktracker.lib.interfaces.MultiplatformLogger

class ConsoleLogger: MultiplatformLogger {
    override fun info(tag: String, message: String) {
        println("INFO $tag: $message")
    }
}