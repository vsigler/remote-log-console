package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.config.LogSource
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.toolwindow.LogSourceStateListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Service
class LogService(private val project: Project) : Disposable {

    companion object {
        val log = Logger.getInstance(LogService::class.java)
    }

    private val threadPool = Executors.newCachedThreadPool()
    private val retrieverRegistry = ConcurrentHashMap<String, RemoteLogRetriever>()

    private val listeners = mutableListOf<LogSourceStateListener>()

    fun addListener(listener: LogSourceStateListener) {
        listeners.add(listener)
    }

    fun start(sourceId: String, console: ConsoleView) {
        getSettingsService().getSource(sourceId)?.let { start(it, console) }
    }

    private fun start(source: LogSource, console: ConsoleView) {
        if (retrieverRegistry.containsKey(source.id)) {
            log.warn("Could not start retriever, already running.")
        } else {
            log.info("Starting log retriever.")

            val reconnectAttempts = if (source.reconnect) source.reconnectAttempts else 0
            val retriever = RemoteLogRetriever(source.toAddress(), reconnectAttempts) { s, t ->
                console.print(s, t)
                ApplicationManager.getApplication().invokeLater {
                    listeners.forEach {
                        it.newContentAdded(source.id)
                    }
                }
            }
            retrieverRegistry[source.id] = retriever
            threadPool.submit {
                try {
                    ApplicationManager.getApplication().invokeLater {
                        listeners.forEach {
                            it.sourceStarted(source.id)
                        }
                    }
                    retriever.run()
                } finally {
                    retrieverRegistry.remove(source.id)
                    ApplicationManager.getApplication().invokeLater {
                        listeners.forEach {
                            it.sourceStopped(source.id)
                        }
                    }
                }
            }
        }
    }

    fun restart(sourceId: String, console: ConsoleView) {
        stop(sourceId)

        getSettingsService().getSource(sourceId)?.let {
            start(it, console)
        }
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

    private fun getSettingsService() = project.getService(SettingsService::class.java)

}
