package dev.arbjerg.reddisplay.jda

import dev.arbjerg.reddisplay.rest.PantryService
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JdaListener(private val slashCommands: List<SlashCommand>, private val pantry: PantryService) : ListenerAdapter() {

    val commands = slashCommands.associateBy { it.commandData.name }
    private val log: Logger = LoggerFactory.getLogger(JdaListener::class.java)

    override fun onGuildReady(event: GuildReadyEvent) {
        event.guild.updateCommands().addCommands(slashCommands.map { it.commandData }).queue {
            log.info("Registered ${it.size} commands in " + event.guild.toString())
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        log.info("Invocation: /${event.commandPath.replace("/", " ")}")
        try {
            commands[event.name]!!.handler(event)
        } catch (e: Exception) {
            event.reply("Error: " + e.message)
            throw e
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        log.info(event.toString())
        if (event.interaction.name == "pantry" && event.interaction.focusedOption.name === "name") {
            val choices = pantry.read().keys.filter { it.startsWith(event.interaction.focusedOption.value) }
            event.replyChoiceStrings(choices).queue()
        } else error("Can't autocomplete for ${event.interaction.name}")
    }

}