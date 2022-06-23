package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.config.LogSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Service
class LogService(val project: Project) : Disposable {

    companion object {
        val log = Logger.getInstance(LogService::class.java)
    }

    private val threadPool = Executors.newCachedThreadPool()
    private val retrieverRegistry = ConcurrentHashMap<String, RemoteLogRetriever>()

    fun start(source: LogSource, console: ConsoleView) {
        if (retrieverRegistry.containsKey(source.id)) {
            log.warn("Could not start retriever, already running.")
        } else {
            log.info("Starting log retriever.")

            val reconnectAttempts = if (source.reconnect) source.reconnectAttempts else 0
            val retriever = RemoteLogRetriever(source.toAddress(), reconnectAttempts) { s, t ->
                console.print(s, t)
            }
            retrieverRegistry[source.id] = retriever
            threadPool.submit {
                try {
                    retriever.run()
                } finally {
                    retrieverRegistry.remove(source.id)
                }
            }
        }
    }

    fun restart(source: LogSource, console: ConsoleView) {
        stop(source.id)
        start(source, console)
    }

    fun stop(sourceId: String) {
        retrieverRegistry[sourceId]?.stop()
    }

    fun isRunning(sourceId: String) : Boolean {
        return retrieverRegistry.containsKey(sourceId)
    }

    override fun dispose() {
        retrieverRegistry.values.forEach { it.stop() }
        threadPool.shutdown()
    }

}
