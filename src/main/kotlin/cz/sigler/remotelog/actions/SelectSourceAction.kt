package cz.sigler.remotelog.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionHolder
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import cz.sigler.remotelog.MyBundle
import cz.sigler.remotelog.config.RemoteLogConfigurable
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.toolwindow.ConsoleTabManager
import java.awt.Point
import java.awt.event.MouseEvent

class SelectSourceAction(
    private val project: Project,
    private val tabManager: ConsoleTabManager) : DumbAwareAction(AllIcons.General.Add) {

    private val settingsService = project.getService(SettingsService::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val popupPoint: RelativePoint? = getPreferredPopupPoint(e)
        val popup: ListPopup = createPopup(e.dataContext)
        if (popupPoint != null) {
            popup.show(popupPoint)
        } else {
            popup.showInFocusCenter()
        }
    }

    private fun getPreferredPopupPoint(e: AnActionEvent): RelativePoint? {
        val inputEvent = e.inputEvent
        if (inputEvent is MouseEvent) {
            val comp = inputEvent.getComponent()
            if (comp is AnActionHolder) {
                return RelativePoint(comp.parent, Point(comp.x + JBUI.scale(3), comp.y + comp.height + JBUI.scale(3)))
            }
        }
        return null
    }

    private fun createPopup(dataContext: DataContext): ListPopup {
        val group = DefaultActionGroup()
        val activeSources = settingsService.state.activeSources

        settingsService.state.sources.forEach {
            group.add(object: DumbAwareAction(it.toString()) {
                override fun actionPerformed(e: AnActionEvent) {
                    tabManager.addTab(it)
                }

                override fun update(e: AnActionEvent) {
                    if (activeSources.contains(it.id)) {
                        e.presentation.isEnabled = false
                    }
                }
            })
        }
        group.addSeparator()
        group.add(object: DumbAwareAction(MyBundle.message("editLogSources")) {
            override fun actionPerformed(e: AnActionEvent) {
                val configurable = RemoteLogConfigurable(project)
                ShowSettingsUtil.getInstance().editConfigurable(project, configurable)
            }
        })
        return JBPopupFactory.getInstance().createActionGroupPopup(
            null, group, dataContext,
            false, true, true, null, -1, null
        )
    }
}