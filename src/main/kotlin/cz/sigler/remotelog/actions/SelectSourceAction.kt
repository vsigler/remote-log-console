package cz.sigler.remotelog.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.content.ContentManager
import cz.sigler.remotelog.MyBundle
import cz.sigler.remotelog.config.RemoteLogConfigurable
import cz.sigler.remotelog.config.SettingsService
import javax.swing.JComponent

class SelectSourceAction(contentManager: ContentManager) : DumbAware, ComboBoxAction() {

    override fun createPopupActionGroup(button: JComponent, context: DataContext): DefaultActionGroup {
        val group = createPopupActionGroup(button)

        val project = context.getData(CommonDataKeys.PROJECT) ?: return group
        val settingsComponent = project.getService(SettingsService::class.java)

        settingsComponent.state.sources.forEach {
            group.add(object: DumbAwareAction(it.toString()) {
                override fun actionPerformed(e: AnActionEvent) {
                    project.getService(SettingsService::class.java)?.state?.activeSource = it.id
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

        return group
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val project = e.project
        if (project != null) {
            val settingsComponent = project.getService(SettingsService::class.java)
            e.presentation.text = settingsComponent.getActiveSource()?.toString()?:"<No source selected>"
        }
    }

    override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
        return DefaultActionGroup()
    }
}