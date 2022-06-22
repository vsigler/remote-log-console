package cz.sigler.remotelog.toolwindow

import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.config.RemoteLogConfigurable
import javax.swing.JButton
import javax.swing.JPanel

class NoActiveTabsView(private val project: Project) {
    lateinit var content: JPanel
    lateinit var editLogSourcesButton: JButton

    init {
        editLogSourcesButton.addActionListener {
            val configurable = RemoteLogConfigurable(project)
            ShowSettingsUtil.getInstance().editConfigurable(project, configurable)
        }
    }

}