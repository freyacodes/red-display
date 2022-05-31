import { createApp, reactive } from "https://unpkg.com/petite-vue@0.2.2/dist/petite-vue.es.js"

const store = reactive({
    clock: "",
    date: "",
    setClock(clock) {
        this.clock = clock
    },
    setDate(date) {
        this.date = date
    }
})

createApp({
    store
}).mount("#mount")

function updateClock() {
    const date = new Date()

    function pad(n) {
        if(n.toString().length === 1) return "0" + n
        else return n
    }

    store.setClock(pad(date.getHours()) + ":" + pad(date.getMinutes()))
    const dateOfMonth = date.getDate()

    let ordinalIndicator
    if (dateOfMonth % 10 === 1) ordinalIndicator = "ˢᵗ"
    else if (dateOfMonth % 10 === 2) ordinalIndicator = "ⁿᵈ"
    else if (dateOfMonth % 10 === 3) ordinalIndicator = "ʳᵈ"
    else ordinalIndicator = "ᵗʰ"

    const months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "November", "December"]

    store.setDate(months[date.getMonth()] + " " + date.getDate() + ordinalIndicator + " " + date.getFullYear())
}

setInterval(updateClock, 1000)