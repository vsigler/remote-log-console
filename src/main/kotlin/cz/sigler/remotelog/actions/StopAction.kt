package cz.sigler.remotelog.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import cz.sigler.remotelog.services.DataKeys
import cz.sigler.remotelog.services.LogRetrieverService

class StopAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let {
            val service = it.getService(LogRetrieverService::class.java)

            DataKeys.LOG_SOURCE_ID.getData(e.dataContext)?.let { src ->
                service.stop(src)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.project?.let {
            val service = it.getService(LogRetrieverService::class.java)

            DataKeys.LOG_SOURCE_ID.getData(e.dataContext)?.let { src ->
                e.presentation.isEnabled = service.isRunning(src) && !service.isPending(src)
            }
        }
    }

}