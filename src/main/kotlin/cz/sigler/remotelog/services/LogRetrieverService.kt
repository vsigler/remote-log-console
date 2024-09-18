package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.config.LogSource
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.toolwindow.LogSourceStateListener
import kotlinx.coroutines.*

@Service(Service.Level.PROJECT)
class LogRetrieverService(private val project: Project) : Disposable {

    companion object {
        val log = Logger.getInstance(LogRetrieverService::class.java)
    }

    private val scope = MainScope()
    private val retrieverRegistry: MutableMap<String, JobWrapper> = mutableMapOf()

    private val listeners = mutableListOf<LogSourceStateListener>()

    fun addListener(listener: LogSourceStateListener) {
        listeners.add(listener)
    }

    fun start(sourceId: String, console: ConsoleView) {
        getSettingsService().getSource(sourceId)?.let {
            scope.launch {
                startInternal(it, console)
            }
        }
    }

    fun restart(sourceId: String, console: ConsoleView) {
        val source = getSettingsService().getSource(sourceId)

        scope.launch {
            stopInternal(sourceId)
            source?.let {
                startInternal(it, console)
            }
        }
    }

    fun stop(sourceId: String) {
        scope.launch { stopInternal(sourceId) }
    }

    fun isRunning(sourceId: String) : Boolean {
        return retrieverRegistry.containsKey(sourceId)
    }

    /**
     * Retriever for given source is starting or stopping.
     */
    fun isPending(sourceId: String) : Boolean {
        return !(retrieverRegistry[sourceId]?.running ?: true)
    }

    override fun dispose() {
        retrieverRegistry.values.forEach { it.job.cancel() }
        scope.cancel("Service disposing")
    }

    private fun startInternal(source: LogSource, console: ConsoleView) {
        if (retrieverRegistry.containsKey(source.id)) {
            log.warn("Could not start retriever, already running.")
        } else {
            log.info("Starting log retriever.")

            val reconnectAttempts = if (source.reconnect) source.reconnectAttempts else 0
            val retriever = LogRetriever(source.toAddress(), reconnectAttempts) { s, t ->
                printToConsole(source, console, s, t)
            }

            val job = scope.launch {
                listeners.forEach {
                    onSourceStarted(source.id)
                }
                retriever.run()
            }.also {
                it.invokeOnCompletion {
                    retrieverRegistry.remove(source.id)?.let {
                        onSourceStopped(source.id)
                    }
                }
            }

            retrieverRegistry[source.id] = JobWrapper(job)
        }
    }

    private suspend fun printToConsole(source: LogSource, console: ConsoleView, text: String, contentType: ConsoleViewContentType) {
        withContext(Dispatchers.Main) {
            console.print(text, contentType)
            listeners.forEach {
                it.newContentAdded(source.id)
            }
        }
    }

    private suspend fun stopInternal(sourceId: String) {
        retrieverRegistry[sourceId]?.let {
            it.running = false
            try {
                it.job.cancelAndJoin()
            } catch (e: Exception) {
                log.error("Could not cancel retriever job, already cancelled or finished.")
            }
            onSourceStopped(sourceId)
            retrieverRegistry.remove(sourceId)
        }
    }

    private fun onSourceStarted(sourceId: String) {
        listeners.forEach {
            retrieverRegistry[sourceId]?.running = true
            it.sourceStarted(sourceId)
        }
    }

    private fun onSourceStopped(sourceId: String) {
        listeners.forEach {
            it.sourceStopped(sourceId)
        }
    }

    private fun getSettingsService() = project.getService(SettingsService::class.java)

}

data class JobWrapper (
    val job: Job,
    var running: Boolean = false,
)
