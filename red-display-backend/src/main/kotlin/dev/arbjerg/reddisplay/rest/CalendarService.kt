package dev.arbjerg.reddisplay.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CalendarService {

    @GetMapping("/calendar")
    fun get() = listOf(
        CalendarEntry("Event  1", "Today", "#ff8484")
    )

}