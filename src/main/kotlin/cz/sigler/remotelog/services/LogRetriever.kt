package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.Logger
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.time.Duration
import kotlin.coroutines.coroutineContext

private const val BUF_SIZE = 1024
private const val TIMEOUT = 5000

class LogRetriever(
    private val address: InetSocketAddress,
    private val reconnectAttempts: Int,
    private val consumer: suspend (String, ConsoleViewContentType) -> Unit
    ) {

    companion object {
        val log = Logger.getInstance(LogRetriever::class.java)
    }

    @Volatile
    private var reconnectCounter = 0

    suspend fun run() {
        if (address.isUnresolved) {
            consumeMessage("Could not resolve host ${address.hostName}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            return
        }

        withContext(Dispatchers.IO) {
            try {
                while (shouldRun()) {
                    runInternal()
                    if (shouldRun()) {
                        consumeMessage("Reconnecting... Attempt ${reconnectCounter + 1} of $reconnectAttempts after 10 seconds...\n", ConsoleViewContentType.SYSTEM_OUTPUT)
                        delay(Duration.ofSeconds(10))
                        reconnectCounter++
                    }
                }
            } catch (ce: CancellationException) {
                if (log.isDebugEnabled) {
                    log.debug("Retriever cancelled.", ce)
                } else {
                    log.info("Retriever cancelled.")
                }
            }
            catch (e: Exception) {
                log.error("Unexpected exception", e)
            }

            log.info("Remote log receiver terminated.")
        }
    }

    private suspend fun shouldRun() = reconnectCounter < reconnectAttempts && coroutineContext.isActive

    private suspend fun runInternal() {
        try {
            Socket().use {
                it.keepAlive = true
                it.soTimeout = TIMEOUT*2
                it.connect(address, TIMEOUT)
                log.info("Connected to $address")
                consumeMessage("Log receiver connected to $address\n", ConsoleViewContentType.SYSTEM_OUTPUT)
                it.getInputStream()?.use { stream ->
                    reconnectCounter = 0
                    var connected = true
                    while (connected) {
                        connected = readAndOutput(stream)
                    }
                }
            }
        } catch (e: IOException) {
            log.warn("Connection error", e)
            consumeMessage("Connection error: $e\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private suspend fun readAndOutput(it: InputStream) : Boolean {
        try {
            val (bytesRead, buffer) = withContext(Dispatchers.IO) {
                val buffer = ByteArray(BUF_SIZE)
                val bytesRead = it.read(buffer, 0, 1024)
                Pair(bytesRead, buffer)
            }

            if (bytesRead == -1) {
                outputDisconnected()
            } else if (bytesRead > 0) {
                consumeMessage(String(buffer, 0, bytesRead))
                return true
            }
        } catch (ste: SocketTimeoutException) {
            log.debug("Read timed out on socket", ste)
            // no data does not mean having to reconnect
            return true
        } catch (e: Exception) {
            when (e) {
                is SocketException -> log.info("Connection terminated", e)
                is CancellationException -> log.info("Connection cancelled", e)
                else -> log.error("Unexpected exception", e)
            }
            outputDisconnected()
        }

        return false
    }
    
    private suspend fun consumeMessage(msg: String, contentType: ConsoleViewContentType = ConsoleViewContentType.NORMAL_OUTPUT) {
        consumer(msg, contentType)
    }

    private suspend fun outputDisconnected() {
        consumeMessage("Remote log disconnected.\n", ConsoleViewContentType.SYSTEM_OUTPUT)
    }
}