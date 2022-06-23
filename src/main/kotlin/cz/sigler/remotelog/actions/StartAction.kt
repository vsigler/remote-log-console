package cz.sigler.remotelog.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.services.LogService

class StartAction : DumbAwareAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return

        val service = project.getService(LogService::class.java)
        val settingsService = project.getService(SettingsService::class.java)
        e.presentation.isVisible = !service.isRunning()
        e.presentation.isEnabled = settingsService.getActiveSource() != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val service = e.project!!.getService(LogService::class.java)
        service.start()
    }
}