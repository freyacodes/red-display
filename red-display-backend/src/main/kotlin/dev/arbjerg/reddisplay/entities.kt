package dev.arbjerg.reddisplay

data class CalendarEntry(
    val name: String,
    val timeString: String,
    val color: String
)

data class PantryEntry(
    var quantity: Int,
    val wanted: Int
)

data class DepartureBoard(
    val a: Departures,
    val e: Departures,
    val f: Departures,
    val a1: Departures,
    val a4: Departures
)

data class Departures(
    val name: String,
    val color: String,
    val departuresLeft: List<Long>,
    val departuresRight: List<Long>
)