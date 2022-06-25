package cz.sigler.remotelog.toolwindow

interface LogSourceStateListener {

    fun newContentAdded(sourceId: String) {}

    fun sourceStarted(sourceId: String) {}

    fun sourceStopped(sourceId: String) {}

}