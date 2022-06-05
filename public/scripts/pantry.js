import {createApp, reactive} from "https://unpkg.com/petite-vue@0.2.2/dist/petite-vue.es.js"

// noinspection JSUnresolvedVariable
const http = axios.create({
    baseURL: "http://localhost:8080"
})

const store = reactive({
    data: {}
})

// noinspection JSUnusedGlobalSymbols
createApp({
    store,
    increment: function (name, item) {
        item.quantity++
        incrementDecrement(true, name).then()
    },
    decrement: function (name, item) {
        item.quantity--
        incrementDecrement(false, name).then()
    },
    formatName: function (name) {
        return name.replaceAll("-", " ")
            .split(" ")
            .map(word => {
                return word[0].toUpperCase() + word.substring(1)
            })
            .join(" ")
    }
}).mount("#list")

async function incrementDecrement(isIncrement, name) {
    let path = isIncrement ? "/pantry/increment/" : "/pantry/decrement/"
    path += name
    const response = await http.post(path)
    console.log(response)
    store.data = response.data
}

async function fetchData() {
    const response = await http.get("/pantry")
    console.log(response)
    store.data = response.data
}

fetchData().then()