package com.haeyum.data

import com.haeyum.API_BASE_URL
import com.haeyum.SERVER_PORT
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*

actual val client: HttpClient = HttpClient(Darwin) {
    install(WebSockets)

    defaultRequest {
        host = API_BASE_URL
        port = SERVER_PORT
    }
}

