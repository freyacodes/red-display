package dev.arbjerg.reddisplay

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurerComposite

@Configuration
class WebfluxConfig {
    @Bean
    fun corsConfigurer() = object : WebFluxConfigurerComposite() {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
        }
    }
}
