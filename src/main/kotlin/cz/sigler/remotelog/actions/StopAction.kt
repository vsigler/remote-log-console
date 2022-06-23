package cz.sigler.remotelog.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import cz.sigler.remotelog.services.LogService

class StopAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {
        val service = e.project!!.getService(LogService::class.java)
        e.presentation.isEnabled = service.isRunning()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val service = e.project!!.getService(LogService::class.java)
        service.stop()
    }
}