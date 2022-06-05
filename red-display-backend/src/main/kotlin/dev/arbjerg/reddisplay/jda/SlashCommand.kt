package dev.arbjerg.reddisplay.jda

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class SlashCommand(val commandData: SlashCommandData, val handler: (SlashCommandInteraction) -> Unit)
