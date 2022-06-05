package dev.arbjerg.reddisplay

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File

@RestController
class PantryService {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("pantry.json")

    @PostMapping("/pantry/increment/{item}")
    fun increment(@PathVariable item: String): Map<String, PantryEntry> {
        update(item, 1)
        return read()
    }

    @PostMapping("/pantry/decrement/{item}")
    fun decrement(@PathVariable item: String): Map<String, PantryEntry> {
        update(item, -1)
        return read()
    }

    private fun update(item: String, delta: Int) {
        write {
            val entry = this[item] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            entry.quantity = (entry.quantity + delta).coerceAtLeast(0)
        }
    }

    @GetMapping("/pantry")
    fun read(): Map<String, PantryEntry> {
        val type = object : TypeToken<HashMap<String, PantryEntry>>() {}.type
        return gson.fromJson(file.reader(), type)
    }

    @Synchronized
    private fun write(transform: Map<String, PantryEntry>.() -> Unit) {
        val json = gson.toJson(read().apply(transform))
        file.writeText(json)
    }

}