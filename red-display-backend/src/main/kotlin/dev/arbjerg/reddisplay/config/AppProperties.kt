package dev.arbjerg.reddisplay.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("reddisplay")
class AppProperties(
    var botToken: String = ""
)