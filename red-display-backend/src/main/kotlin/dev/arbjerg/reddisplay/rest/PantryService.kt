package dev.arbjerg.reddisplay.rest

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.arbjerg.reddisplay.jda.SlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import org.springframework.context.annotation.Bean
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
    fun write(transform: Map<String, PantryEntry>.() -> Unit) {
        val json = gson.toJson(read().apply(transform))
        file.writeText(json)
    }

    @Bean
    fun pantryCommand() = SlashCommand(
        CommandDataImpl("pantry", "Manage Rød Stue's pantry database")
            .addSubcommands(
                SubcommandData("list", "List what's in the pantry")
            )
    ) { event ->
        when(event.subcommandName) {
            "list" -> pantryList(event)
            else -> error("Unknown command ${event.commandPath}")
        }
    }

    private fun pantryList(event: SlashCommandInteraction) {
        val pantry = read()
        val embed = EmbedBuilder().apply {
            setTitle("Pantry")
            val left = mutableListOf<String>()
            val right = mutableListOf<String>()
            pantry.forEach { (name, pantry) ->
                left.add(name)
                right.add(pantry.quantity.toString() + "/" + pantry.wanted)
            }
            addField("Item", left.joinToString("\n"), true)
            addField("Quantity", right.joinToString("\n"), true)
        }.build()

        event.replyEmbeds(embed).queue()
    }

}