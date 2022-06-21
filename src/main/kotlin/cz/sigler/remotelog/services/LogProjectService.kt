package cz.sigler.remotelog.services

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.config.SettingsService

@Service
class LogProjectService(val project: Project) : Disposable {

    companion object {
        val log = Logger.getInstance(LogProjectService::class.java)
    }

    @Volatile
    var console: ConsoleView? = null

    var retriever: RemoteLogRetriever? = null

    @Synchronized
    fun start() {
        val source = project.getService(SettingsService::class.java).getActiveSource()

        if (source != null) {
            if (retriever == null) {
                log.info("Starting log retriever.")

                val reconnectAttempts = if (source.reconnect) source.reconnectAttempts else 0
                retriever = RemoteLogRetriever(source.toAddress(), reconnectAttempts) { s, t ->
                    console?.print(s, t)
                }
                Thread {
                    val retrieverLocal = retriever
                    try {
                        retrieverLocal?.run()
                    } finally {
                        // only cleanup if needed
                        if (retriever === retrieverLocal) {
                            stop()
                        }
                    }
                }
                    .start()
            } else {
                log.warn("Could not start retriever, already running.")
            }
        }
    }

    @Synchronized
    fun restart() {
        stop()
        start()
    }

    @Synchronized
    fun stop() {
        retriever?.stop()
        retriever = null
    }

    @Synchronized
    fun isRunning() : Boolean {
        return retriever != null
    }

    override fun dispose() {
        stop()
    }
}
