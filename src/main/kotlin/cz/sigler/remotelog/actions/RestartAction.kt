package cz.sigler.remotelog.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.services.LogProjectService

class RestartAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val service = e.project!!.getService(LogProjectService::class.java)
        service.restart()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return

        val service = project.getService(LogProjectService::class.java)
        val settingsService = project.getService(SettingsService::class.java)
        e.presentation.isVisible = service.isRunning()
        e.presentation.isEnabled = settingsService.getActiveSource() != null
    }
}