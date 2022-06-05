package dev.arbjerg.reddisplay.rest

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.arbjerg.reddisplay.jda.SlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER
import net.dv8tion.jda.api.interactions.commands.OptionType.STRING
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
    fun write(transform: MutableMap<String, PantryEntry>.() -> Unit): Map<String, PantryEntry> {
        val data = read().toMutableMap().apply(transform)
        val json = gson.toJson(data)
        file.writeText(json)
        return data
    }

    @Bean
    fun pantryCommand() = SlashCommand(
        CommandDataImpl("pantry", "Manage Rød Stue's pantry database").addSubcommands(
            SubcommandData("list", "List what's in the pantry"),
            SubcommandData("add", "Add a new type of item to the pantry")
                .addOption(STRING, "name", "Name of the new item", true)
                .addOption(INTEGER, "wanted", "How many units to keep stocked", true),
            SubcommandData("remove", "Remove a type of item")
                .addOption(STRING, "name", "Name of the item to remove", true),
            SubcommandData("set", "Set item quantity")
                .addOption(STRING, "name", "The item to set the quantity of", true)
                .addOption(INTEGER, "quantity", "The new quantity of the item", true)
                .addOption(INTEGER, "wanted", "The desired quantity", false)

        )
    ) { event ->
        when (event.subcommandName) {
            "list" -> pantryList(event)
            "add" -> pantryAdd(event)
            "remove" -> pantryRemove(event)
            "set" -> pantrySet(event)
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

    private fun pantryAdd(event: SlashCommandInteraction) {
        val name = event.getOption("name")!!.asString.parseName()
            ?: return event.reply("Invalid name").setEphemeral(true).queue()

        val pantry = read()
        if (pantry.containsKey(name)) {
            event.reply("`$name` already exists").setEphemeral(true).queue()
            return
        }

        val wanted = event.getOption("wanted")!!.asInt
        if (wanted < 1) {
            event.reply("Desired quantity must be at least 1").setEphemeral(true).queue()
            return
        }

        write {
            this[name] = PantryEntry(0, wanted)
        }
        event.reply("Added `$name` with desired quantity `$wanted`").queue()
    }

    private fun pantryRemove(event: SlashCommandInteraction) {
        val name = parseAndCheckExists(event, event.getOption("name")!!.asString) ?: return
        write {
            remove(name)
        }
        event.reply("Removed `$name`").queue()
    }

    private fun pantrySet(event: SlashCommandInteraction) {
        val name = parseAndCheckExists(event, event.getOption("name")!!.asString) ?: return
        val quantity = event.getOption("quantity")!!.asInt
        val wanted = event.getOption("wanted")?.asInt

        if (quantity < 0) {
            event.reply("Quantity must be at least 0").setEphemeral(true).queue()
            return
        }

        if (wanted != null && wanted < 1) {
            event.reply("Desired quantity must be at least 1").setEphemeral(true).queue()
            return
        }

        val storedItem = write {
            this[name]!!.apply {
                this.quantity = quantity
                this.wanted = wanted ?: this.wanted
            }
        }[name]!!

        event.reply("`$name` set to `${storedItem.quantity}/${storedItem.wanted}`").queue()
    }

    private fun parseAndCheckExists(event: SlashCommandInteraction, name: String): String? {
        val parsed = name.parseName()
        if (parsed == null) {
            event.reply("Invalid name").setEphemeral(true).queue()
            return null
        }

        val pantry = read()
        if(!pantry.containsKey(parsed)) {
            event.reply("`$parsed` does not exist").setEphemeral(true).queue()
            return null
        }
        return parsed
    }

    private fun String.parseName(): String? {
        val name = this.lowercase().replace(" ", "-")

        if (name.contains(Regex.fromLiteral("^[\\W-]")) || name.startsWith("-") || name.endsWith("-")) {
            return null
        }
        return name
    }
}
