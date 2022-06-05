package dev.arbjerg.reddisplay.config

import dev.arbjerg.reddisplay.jda.JdaListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JdaConfig {

    @Bean
    fun jda(appProperties: AppProperties, jdaListener: JdaListener): JDA {
        return JDABuilder.create(emptyList())
            .setToken(appProperties.botToken)
            .addEventListeners(jdaListener)
            .disableCache(ACTIVITY, VOICE_STATE, EMOTE, CLIENT_STATUS, ONLINE_STATUS)
            .build()
    }

}