package cz.sigler.remotelog.toolwindow

import com.intellij.execution.actions.ClearConsoleAction
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JPanel


class ToolWindow(val project: Project) : Disposable {
    lateinit var content: JPanel
    private lateinit var toolbar: JPanel
    private lateinit var console: JPanel
    var consoleView: ConsoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console

    init {
        val actionManager = ActionManager.getInstance()

        val newGroup = DefaultActionGroup()
        newGroup.add(actionManager.getAction("toolbar.console"))
        newGroup.addSeparator()

        newGroup.addAll(createConsoleActions(consoleView))

        val actionToolbar = actionManager.createActionToolbar("ConsolePanel", newGroup, true)
        val editorToolbar = actionToolbar as ActionToolbarImpl
        editorToolbar.setReservePlaceAutoPopupIcon(false)
        editorToolbar.isOpaque = false
        editorToolbar.border = JBEmptyBorder(0, 0, 0, 0)
        editorToolbar.targetComponent = consoleView.component
        actionToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY)

        toolbar.add(editorToolbar.component, BorderLayout.CENTER)
        console.add(consoleView.component, BorderLayout.CENTER)
    }

    override fun dispose() {
        console.removeAll()
        consoleView.dispose()
    }

    private fun createConsoleActions(console: ConsoleView) : List<AnAction> {
        // to enforce initialization
        console.component

        // The default console has some extra actions that are unnecessary here.
        return console.createConsoleActions()
            .filter {
                it is ToggleUseSoftWrapsToolbarAction ||
                        it is ClearConsoleAction ||
                        it is ScrollToTheEndToolbarAction
            }
    }
}