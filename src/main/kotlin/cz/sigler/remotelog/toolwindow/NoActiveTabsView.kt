package cz.sigler.remotelog.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.LayeredIcon
import cz.sigler.remotelog.MyBundle
import cz.sigler.remotelog.config.RemoteLogConfigurable
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet

class NoActiveTabsView(private val project: Project) {
    lateinit var content: JPanel
    lateinit var editLogSourcesButton: JButton
    lateinit var addTabIcon: JLabel
    lateinit var textPane: JTextPane

    init {
        editLogSourcesButton.addActionListener {
            val configurable = RemoteLogConfigurable(project)
            ShowSettingsUtil.getInstance().editConfigurable(project, configurable)
        }
        editLogSourcesButton.icon = AllIcons.General.Settings

        val document = textPane.document
        val attrs = SimpleAttributeSet()
        document.insertString(document.length, MyBundle.message("noTabViewTextBeforeIcon"), attrs)
        document.insertString(document.length, " ", attrs)
        textPane.insertIcon(LayeredIcon(AllIcons.General.Add, AllIcons.General.Dropdown))
        document.insertString(document.length, " ", attrs)
        document.insertString(document.length, MyBundle.message("noTabViewTextAfterIcon"), attrs)
        textPane.isEditable = false
    }

}