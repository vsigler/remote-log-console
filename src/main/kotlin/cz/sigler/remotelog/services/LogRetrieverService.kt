package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.config.LogSource
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.toolwindow.LogSourceStateListener
import kotlinx.coroutines.*

@Service
class LogRetrieverService(private val project: Project) : Disposable {

    companion object {
        val log = Logger.getInstance(LogRetrieverService::class.java)
    }

    private val scope = MainScope()
    private val retrieverRegistry: MutableMap<String, Job> = mutableMapOf()

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
            val retriever = LogRetriever(source.toAddress(), reconnectAttempts) { s, t ->
                withContext(Dispatchers.Main) {
                    console.print(s, t)
                    listeners.forEach {
                        it.newContentAdded(source.id)
                    }
                }
            }

            retrieverRegistry[source.id] = scope.launch {
                listeners.forEach {
                    it.sourceStarted(source.id)
                }
                retriever.run()
            }.also {
                it.invokeOnCompletion {
                    retrieverRegistry.remove(source.id)?.let {
                        onSourceStopped(source.id)
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
        retrieverRegistry.remove(sourceId)?.let{
            it.cancel("Requested retriever stop")
            onSourceStopped(sourceId)
        }
    }

    fun isRunning(sourceId: String) : Boolean {
        return retrieverRegistry.containsKey(sourceId)
    }

    override fun dispose() {
        retrieverRegistry.values.forEach { it.cancel() }
        scope.cancel("Service disposing")
    }

    private fun onSourceStopped(sourceId: String) {
        listeners.forEach {
            it.sourceStopped(sourceId)
        }
    }

    private fun getSettingsService() = project.getService(SettingsService::class.java)

}
