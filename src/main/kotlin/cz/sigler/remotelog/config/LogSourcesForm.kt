package cz.sigler.remotelog.config

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.CollectionListModel
import com.intellij.util.ui.JBEmptyBorder
import cz.sigler.remotelog.MyBundle
import cz.sigler.remotelog.actions.BaseAction
import java.awt.BorderLayout
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LogSourcesForm(initialItems: List<LogSource>) {
    var rootComponent: JPanel? = null
    private lateinit var toolbar: JPanel
    private lateinit var sourceList: JList<LogSource>
    private lateinit var itemEditor: JPanel
    private lateinit var fieldName: JTextField
    private lateinit var fieldHost: JTextField
    private lateinit var portSpinner: JSpinner
    private lateinit var autoReconnectCheckBox: JCheckBox
    private lateinit var retriesSpinner: JSpinner

    private val model : CollectionListModel<LogSource>
    private var selectedItem : LogSource? = null

    // Create a deep copy of the original list, so that we do not change
    // the original and can compare for differences.
    val items = initialItems.map { it.copy() }.toMutableList()

    init {
        model = CollectionListModel(items, true)
        sourceList.model = model
        itemEditor.isVisible = false
        portSpinner.model = SpinnerNumberModel(1, 1, 65535, 1)
        retriesSpinner.model = SpinnerNumberModel(20, 0, null, 1)

        sourceList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val index = sourceList.selectedIndex

                if (index > -1) {
                    val newlySelected = model.getElementAt(index)
                    selectedItem = null
                    loadToEditor(newlySelected!!)
                    selectedItem = newlySelected
                    itemEditor.isVisible = true
                } else {
                    selectedItem = null
                    itemEditor.isVisible = false
                }
            }
        }
        
        val listener = object : DocumentListener {
            override fun insertUpdate(evt: DocumentEvent) = updateFromEditor()

            override fun removeUpdate(evt: DocumentEvent) = updateFromEditor()

            override fun changedUpdate(evt: DocumentEvent) = updateFromEditor()
        }

        fieldName.document.addDocumentListener(listener)
        fieldHost.document.addDocumentListener(listener)
        portSpinner.model.addChangeListener { updateFromEditor() }
        autoReconnectCheckBox.model.addChangeListener { updateFromEditor() }
        retriesSpinner.model.addChangeListener { updateFromEditor() }

        val toolbarActions = DefaultActionGroup()
        toolbarActions.add(object: BaseAction(MyBundle.message("addLogSource"), null, AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                model.add(LogSource(name = MyBundle.message("newLogSource")))
                sourceList.selectedIndex = items.size - 1
            }
        })

        toolbarActions.add(object: BaseAction(MyBundle.message("discardLogSource"), null, AllIcons.General.Remove) {
            override fun actionPerformed(e: AnActionEvent) {
                model.remove(sourceList.selectedIndex)
            }

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = selectedItem != null
            }
        })

        toolbarActions.add(object: BaseAction(MyBundle.message("copyLogSource"), null, AllIcons.Actions.Copy) {
            override fun actionPerformed(e: AnActionEvent) {
                selectedItem?.let {
                    model.add(it.copy(id = UUID.randomUUID().toString(), name = "${it.name} Copy"))
                    sourceList.selectedIndex = items.size - 1
                }
            }

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = selectedItem != null
            }
        })

        val actionManager = ActionManager.getInstance()
        val actionToolbar = actionManager.createActionToolbar("ConsolePanel", toolbarActions, true)
        with(actionToolbar.component) {
            isOpaque = false
            border = JBEmptyBorder(0, 0, 0, 0)
        }
        actionToolbar.setReservePlaceAutoPopupIcon(false)
        actionToolbar.targetComponent = sourceList
        actionToolbar.layoutPolicy = ActionToolbar.AUTO_LAYOUT_POLICY

        toolbar.add(actionToolbar.component, BorderLayout.CENTER)

        if (items.isNotEmpty()) {
            sourceList.selectedIndex = 0
        }
    }

    private fun loadToEditor(src: LogSource) {
        fieldName.text = src.name
        fieldHost.text = src.host
        portSpinner.model.value = src.port
        autoReconnectCheckBox.model.isSelected = src.reconnect
        retriesSpinner.model.value = src.reconnectAttempts
    }

    private fun updateFromEditor(target: LogSource? = selectedItem) {
        target?.let {
            it.name = fieldName.text
            it.host = fieldHost.text
            it.port = portSpinner.model.value as Int
            it.reconnect = autoReconnectCheckBox.model.isSelected
            it.reconnectAttempts = retriesSpinner.model.value as Int
            model.contentsChanged(it)
        }
    }
}