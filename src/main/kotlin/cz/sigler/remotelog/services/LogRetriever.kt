package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.Logger
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiConsumer

private const val BUF_SIZE = 1024
private const val TIMEOUT = 5000

class LogRetriever(
    private val address: InetSocketAddress,
    private val reconnectAttempts: Int,
    consumer: BiConsumer<String, ConsoleViewContentType>
    ) : Runnable {

    companion object {
        val log = Logger.getInstance(LogRetriever::class.java)
    }

    private val consumerRef = AtomicReference(consumer)

    private val buffer = ByteArray(BUF_SIZE)
    private var stopLatch = CountDownLatch(1)
    private var reconnectCounter = 0

    @Volatile
    private var socket: Socket? = null

    override fun run() {
        if (address.isUnresolved) {
            consumerRef.get()?.accept("Could not resolve host ${address.hostName}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            return
        }

        while (shouldRun()) {
            runInternal()
            if (shouldRun()) {
                consumerRef.get()?.accept("Reconnecting... Attempt ${reconnectCounter + 1} of $reconnectAttempts after 10 seconds...\n", ConsoleViewContentType.SYSTEM_OUTPUT)
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
            socket = Socket()
            socket?.use {
                it.keepAlive = true
                it.connect(address, TIMEOUT)
                log.info("Connected to $address")
                consumerRef.get()?.accept("Log receiver connected to $address\n", ConsoleViewContentType.SYSTEM_OUTPUT)
                it.getInputStream()?.use {
                    reconnectCounter = 0
                    var connected = true
                    while (connected) {
                        connected = readAndOutput(it)
                    }
                }
            }
        } catch (e: IOException) {
            log.warn("Connection error", e)
            consumerRef.get()?.accept("Connection error: $e\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    fun stop() {
        consumerRef.set(null)
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
                consumerRef.get()?.accept(String(buffer, 0, bytesRead), ConsoleViewContentType.NORMAL_OUTPUT)
            }
        } catch (e: SocketException) {
            log.info("Connection terminated", e)
            outputDisconnected()
            return false
        }

        return true
    }

    private fun outputDisconnected() {
        consumerRef.get()?.accept("Remote log disconnected.\n", ConsoleViewContentType.SYSTEM_OUTPUT)
    }
}