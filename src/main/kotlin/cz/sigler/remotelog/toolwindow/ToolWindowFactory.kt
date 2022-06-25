package cz.sigler.remotelog.toolwindow

import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import cz.sigler.remotelog.actions.SelectSourceAction
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.services.LogService

class ToolWindowFactory : ToolWindowFactory, DumbAware {

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.getService(LogService::class.java)
        val toolWindowEx = toolWindow as ToolWindowEx
        val tabManager = ConsoleTabManager(project, toolWindow.contentManager)
        Disposer.register(service, tabManager)

        toolWindowEx.setTabActions(SelectSourceAction(project, tabManager))

        val settingsService = project.getService(SettingsService::class.java)
        settingsService.addChangeListener {
            tabManager.configurationChanged(it)
        }
        service.addListener(tabManager)
        service.addListener(object: LogSourceStateListener {
            private var runningCount = 0

            override fun sourceStarted(sourceId: String) {
                runningCount++
                toolWindow.setIcon(ExecutionUtil.getLiveIndicator(AllIcons.Debugger.Console))
            }

            override fun sourceStopped(sourceId: String) {
                runningCount--
                if (runningCount == 0) {
                    toolWindow.setIcon(AllIcons.Debugger.Console)
                }
            }
        })
    }
}