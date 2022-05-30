package dev.arbjerg.reddisplay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedDisplayBackendApplication

fun main(args: Array<String>) {
    runApplication<RedDisplayBackendApplication>(*args)
}
