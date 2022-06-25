package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.Logger
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

private const val BUF_SIZE = 1024

class RemoteLogRetriever(
    private val address: InetSocketAddress,
    private val reconnectAttempts: Int,
    private val consumer: BiConsumer<String, ConsoleViewContentType>
    ) : Runnable {

    companion object {
        val log = Logger.getInstance(RemoteLogRetriever::class.java)
    }

    private val buffer = ByteArray(BUF_SIZE)
    private var stopLatch = CountDownLatch(1)
    private var reconnectCounter = 0

    @Volatile
    private var socket: Socket? = null

    override fun run() {
        if (address.isUnresolved) {
            consumer.accept("Could not resolve host ${address.hostName}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            return
        }

        while (shouldRun()) {
            runInternal()
            if (shouldRun()) {
                consumer.accept("Reconnecting... Attempt ${reconnectCounter + 1} of $reconnectAttempts after 10 seconds...\n", ConsoleViewContentType.SYSTEM_OUTPUT)
                stopLatch.await(10, TimeUnit.SECONDS)
                reconnectCounter++
            }
        }

        socket = null
        log.info("Remote log receiver terminated.")
    }

    private fun shouldRun() = reconnectCounter < reconnectAttempts && stopLatch.count > 0

    private fun runInternal() {
        try {
            socket = Socket(address.address, address.port)
            log.info("Connected to $address")
            consumer.accept("Log receiver connected to $address\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            socket?.use {
                it.getInputStream()?.use {
                    reconnectCounter = 0
                    var connected = true
                    while (connected) {
                        connected = readAndOutput(it)
                    }
                }
            }
        } catch (e: SocketException) {
            log.info("Could not connect to remote service", e)
            consumer.accept("Connection error: $e\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    fun stop() {
        stopLatch.countDown()
        socket?.close()
    }

    private fun readAndOutput(it: InputStream) : Boolean {
        try {
            val bytesRead = it.read(buffer, 0, 1024)

            if (bytesRead == -1) {
                outputDisconnected()
                return false
            } else if (bytesRead > 0) {
                consumer.accept(String(buffer, 0, bytesRead), ConsoleViewContentType.NORMAL_OUTPUT)
            }
        } catch (e: SocketException) {
            log.info("Connection terminated", e)
            outputDisconnected()
            return false
        }

        return true
    }

    private fun outputDisconnected() {
        consumer.accept("Remote log disconnected.\n", ConsoleViewContentType.SYSTEM_OUTPUT)
    }
}