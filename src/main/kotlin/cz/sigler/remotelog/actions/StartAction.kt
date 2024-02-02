package cz.sigler.remotelog.actions

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import cz.sigler.remotelog.services.DataKeys
import cz.sigler.remotelog.services.LogRetrieverService

class StartAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let {
            val service = it.getService(LogRetrieverService::class.java)

            val consoleView = PlatformDataKeys.CONTEXT_COMPONENT.getData(e.dataContext)

            if (consoleView is ConsoleView) {
                DataKeys.LOG_SOURCE_ID.getData(e.dataContext)?.let { src ->
                    service.start(src, consoleView)
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.project?.let {
            val service = it.getService(LogRetrieverService::class.java)

            DataKeys.LOG_SOURCE_ID.getData(e.dataContext)?.let { src ->
                e.presentation.isVisible = !service.isRunning(src)
                e.presentation.isEnabled = !service.isPending(src)
            }
        }
    }

}