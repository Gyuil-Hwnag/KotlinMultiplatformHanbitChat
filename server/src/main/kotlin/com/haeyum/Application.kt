package com.haeyum

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/hanbit") {
            call.respondText { "[GET] HANBIT\n${call.receiveText()}" }
        }

        post("hanbit") {
            call.respondText { "[POST] HANBIT\n${call.receiveText()}" }
        }

        val clients = mutableSetOf<DefaultWebSocketSession>()
        // incoming : Client -> Server
        // outcoming : Server -> Client
        webSocket("/chat") {
            val client = this
            clients.add(client)
            incoming.receiveAsFlow()
                .onCompletion { clients.remove(this@webSocket) }
                .collect { received ->
                    when (received) {
                        is Frame.Text -> {
                            clients.forEach { client ->
                                val receivedText = received.readText()
                                val sendFrame = Frame.Text(receivedText)
                                client.send(sendFrame)
                            }
                        }

                        else -> {
                            print("Other Frame.Text")
                        }
                    }
            }
        }
    }
}
