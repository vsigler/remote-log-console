package cz.sigler.remotelog.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.NlsActions
import java.util.function.Supplier
import javax.swing.Icon

abstract class BaseAction: DumbAwareAction {

    constructor() : super()

    constructor(
        text: @NlsActions.ActionText String?,
        description: @NlsActions.ActionDescription String?,
        icon: Icon?
    ) : super(text, description, icon)

    constructor(dynamicText: Supplier<@NlsActions.ActionText String>, icon: Icon?) : super(dynamicText, icon)


    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

}