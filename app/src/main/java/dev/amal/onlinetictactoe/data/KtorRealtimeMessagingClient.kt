package dev.amal.onlinetictactoe.data

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KtorRealtimeMessagingClient(
    private val client: HttpClient
) : RealtimeMessagingClient {

    private var session: WebSocketSession? = null

    override fun getGameStateStream(): Flow<GameState> = flow {
        session = client.webSocketSession {
            url("ws://192.168.1.3:8080/play")
        }
        val gameStates = session!!
            .incoming
            .consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .mapNotNull { Json.decodeFromString<GameState>(it.readText()) }

        emitAll(gameStates)
    }

    override suspend fun sendAction(action: MakeTurn) {
        session?.outgoing?.send(
            Frame.Text("make_turn#${Json.encodeToString(action)}")
        )
    }

    override suspend fun close() {
        session?.close()
        session = null
    }
}