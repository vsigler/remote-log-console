package cz.sigler.remotelog.toolwindow

import com.intellij.execution.runners.ExecutionUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
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

        val settingsService = project.getService(SettingsService::class.java)
        val toolWindowEx = toolWindow as ToolWindowEx

        val myToolWindow = ToolWindow(project, settingsService.getActiveSource()!!.id)
        val contentFactory = ContentFactory.SERVICE.getInstance()

        toolWindow.addContentManagerListener(object: ContentManagerListener {
            override fun contentRemoved(event: ContentManagerEvent) {
                if (toolWindow.contentManager.contentCount == 0) {
                    val noActiveTabsView = NoActiveTabsView(project)
                    val content = contentFactory.createContent(noActiveTabsView.content, "", false)
                    content.isCloseable = false
                    toolWindow.contentManager.addContent(content)
                }
            }
        })

        val icon = toolWindow.icon
        toolWindowEx.setTabActions(object: DumbAwareAction(AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {

            }
        }, SelectSourceAction())
        toolWindow.setIcon(ExecutionUtil.getLiveIndicator(icon))
        toolWindow.title = "Remote Log"


        val content = contentFactory.createContent(myToolWindow.content, "asfs", false)
//        content.isCloseable = false

        content.icon = ExecutionUtil.getLiveIndicator(icon)
        toolWindow.contentManager.addContent(content)

//        val myToolWindow2 = ToolWindow(project)
//        val content2 = contentFactory.createContent(myToolWindow2.content, "bbbbbb", false)
//        toolWindow.contentManager.addContent(content2)

        val consoleView = myToolWindow.consoleView

        Disposer.register(service, myToolWindow)

        service.console = consoleView
    }
}