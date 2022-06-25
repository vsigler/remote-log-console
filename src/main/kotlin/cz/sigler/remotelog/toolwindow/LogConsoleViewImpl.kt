package cz.sigler.remotelog.toolwindow

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.openapi.project.Project
import cz.sigler.remotelog.services.DataKeys

class LogConsoleViewImpl(project: Project, private val sourceId: String) : ConsoleViewImpl(project, false) {

    override fun getData(dataId: String): Any? {
        if (DataKeys.LOG_SOURCE_ID.`is`(dataId)) {
            return sourceId
        }
        return super.getData(dataId)
    }
}