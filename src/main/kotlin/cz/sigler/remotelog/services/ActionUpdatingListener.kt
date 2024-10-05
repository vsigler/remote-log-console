package cz.sigler.remotelog.services

import com.intellij.ide.ActivityTracker
import cz.sigler.remotelog.toolwindow.LogSourceStateListener

class ActionUpdatingListener: LogSourceStateListener {

    override fun sourceStarted(sourceId: String) {
        updateActions()
    }

    override fun sourceStopped(sourceId: String) {
        updateActions()
    }

    private fun updateActions() {
        ActivityTracker.getInstance().inc()
    }
}