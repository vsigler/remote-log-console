package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.diagnostic.Logger
import cz.sigler.remotelog.MyBundle
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
private const val CONNECT_TIMEOUT = 5000
private const val READ_TIMEOUT = 1000

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
            consumeMessage("${ MyBundle.message("cannotReslveHost", address.hostName)}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            return
        }

        withContext(Dispatchers.IO) {
            try {
                while (shouldRun()) {
                    runInternal()
                    if (shouldRun()) {
                        consumeMessage("${ MyBundle.message("reconnecting", reconnectCounter + 1, reconnectAttempts, 10) }\n",
                            ConsoleViewContentType.SYSTEM_OUTPUT)
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
                it.soTimeout = READ_TIMEOUT
                it.connect(address, CONNECT_TIMEOUT)
                log.info("Connected to $address")
                consumeMessage("${ MyBundle.message("logRetrieverConnected", address) }\n", ConsoleViewContentType.SYSTEM_OUTPUT)
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
            consumeMessage("${ MyBundle.message("connectionError", e)}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private suspend fun readAndOutput(it: InputStream) : Boolean {
        try {
            val (bytesRead, buffer) = withContext(Dispatchers.IO) {
                val buffer = ByteArray(BUF_SIZE)
                val bytesRead = it.read(buffer, 0, 1024)
                Pair(bytesRead, buffer)
            }

            if (bytesRead > 0) {
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
        }

        return false
    }
    
    private suspend fun consumeMessage(msg: String, contentType: ConsoleViewContentType = ConsoleViewContentType.NORMAL_OUTPUT) {
        consumer(msg, contentType)
    }

}