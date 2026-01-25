package com.haeyum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haeyum.componenets.ChatListArea
import com.haeyum.componenets.FieldArea
import com.haeyum.componenets.Header
import com.haeyum.data.client
import com.haeyum.theme.AppColors
import com.haeyum.theme.AppTypography
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

@Composable
@Preview
fun App() {
    MaterialTheme(typography = AppTypography()) {
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val textFieldState = rememberTextFieldState()

        val username by remember { mutableStateOf(getPlatform().name + Random.nextInt(1, 1000)) }
        val messages = remember { mutableStateListOf<Message>() }
        val session = produceState<WebSocketSession?>(null) {
            client.webSocket("/chat") {
                value = this

                incoming.receiveAsFlow().collect { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            val message = Json.decodeFromString<Message>(receivedText)
                            messages.add(message)
                        }

                        else -> {
                            println("Something Wrong")
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.White)
                .safeDrawingPadding()
        ) {
            Header(title = "Hanbit KMP Chat")

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
            ) {
                ChatListArea(
                    state = lazyListState,
                    ownerUsername = username,
                    messages = messages,
                    modifier = Modifier.weight(1f),
                )

                FieldArea(
                    textFieldState = textFieldState,
                    enabled = session.value != null && textFieldState.text.isNotEmpty(),
                    onSend = { messageText ->
                        coroutineScope.launch {
                            val message = Message(username = username, content = messageText)
                            val messageJson = Json.encodeToString(message)
                            val frame = Frame.Text(messageJson)
                            session.value?.send(frame)
                            textFieldState.clearText()
                            lazyListState.scrollToBottom()
                        }
                    },
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }
    }
}

private suspend fun LazyListState.scrollToBottom() = scrollToItem(Int.MAX_VALUE)
