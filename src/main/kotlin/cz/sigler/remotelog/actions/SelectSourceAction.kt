package cz.sigler.remotelog.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.ui.LayeredIcon
import com.intellij.ui.popup.PopupState
import com.intellij.util.ui.JBUI
import cz.sigler.remotelog.MyBundle
import cz.sigler.remotelog.config.RemoteLogConfigurable
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.toolwindow.ConsoleTabManager
import cz.sigler.remotelog.toolwindow.TabIconUtil
import java.awt.event.MouseEvent

class SelectSourceAction(
    private val project: Project,
    private val tabManager: ConsoleTabManager) : DumbAwareAction() {

    private val myPopupState = PopupState.forPopupMenu()

    init {
        with (templatePresentation) {
            icon = LayeredIcon(AllIcons.General.Add, AllIcons.General.Dropdown)
            text = MyBundle.message("addConsole")
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (myPopupState.isRecentlyHidden) {
            return
        }

        val inputEvent = e.inputEvent
        val popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, createActionGroup())
        var x = 0
        var y = 0
        if (inputEvent is MouseEvent) {
            val component = inputEvent.component
            x = component.x + JBUI.scale(2)
            y = component.y + component.height
        }
        myPopupState.prepareToShow(popupMenu.component)
        popupMenu.component.show(inputEvent.component, x, y)
    }

    private fun createActionGroup(): DefaultActionGroup {
        val group = DefaultActionGroup()
        val settingsService = project.getService(SettingsService::class.java)
        val activeSources = settingsService.state.activeSources

        settingsService.state.sources.forEach {
            group.add(object: DumbAwareAction(it.toString(), null, TabIconUtil.getTabIcon(running = false, newContent = false)) {
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
        group.add(object: DumbAwareAction(MyBundle.messagePointer("editLogSources"), AllIcons.General.Settings) {
            override fun actionPerformed(e: AnActionEvent) {
                val configurable = RemoteLogConfigurable(project)
                ShowSettingsUtil.getInstance().editConfigurable(project, configurable)
            }
        })

        return group
    }
}