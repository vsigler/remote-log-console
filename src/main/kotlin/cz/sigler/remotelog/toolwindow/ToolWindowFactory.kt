package cz.sigler.remotelog.toolwindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import cz.sigler.remotelog.services.LogProjectService

class ToolWindowFactory : ToolWindowFactory, DumbAware {
    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ToolWindow(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)

        val consoleView = myToolWindow.consoleView
        val service = project.getService(LogProjectService::class.java)
        Disposer.register(service, consoleView)

        service.console = consoleView
    }
}