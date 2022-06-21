package cz.sigler.remotelog

import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.charset.StandardCharsets
import java.util.*

class TestLogServer {
    val messages = loadMessages()
    val random = Random()

    fun loadMessages() : List<String> {
        val text = this.javaClass.getResource("/lorem-ipsum.txt").readText(StandardCharsets.UTF_8)
        return text.split(".")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { "$it.\n" }
    }

    fun start() {
        ServerSocket(41234).use {
            while (true) {
                Thread(ServerThread(it.accept())).start()
            }
        }
    }

    fun randomMessage() = messages[random.nextInt(messages.size)]

    inner class ServerThread(private val socket: Socket) : Runnable {
        override fun run() {
            println("Client connected.")
            socket.use {
                it.getOutputStream().use { stream ->
                    try {
                        while (!socket.isClosed) {
                            stream.write(randomMessage().toByteArray())
                            Thread.sleep(random.nextInt(4000).toLong())
                        }
                    } catch (e: SocketException) {
                        println("Connection closed")
                    }
                }
            }
        }
    }
}

object Server {

    @JvmStatic
    fun main(args: Array<String>) {
        TestLogServer().start()
    }
}