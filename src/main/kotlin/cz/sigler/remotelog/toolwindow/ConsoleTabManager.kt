package cz.sigler.remotelog.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.ui.content.*
import cz.sigler.remotelog.config.LogSource
import cz.sigler.remotelog.config.Settings
import cz.sigler.remotelog.config.SettingsService
import cz.sigler.remotelog.services.DataKeys
import cz.sigler.remotelog.services.LogService

class ConsoleTabManager(
    private val project: Project,
    private val contentManager: ContentManager) : ContentManagerListener, LogSourceStateListener, Disposable {

    companion object {
        private val LOG_SOURCE_KEY = Key.create<String>(DataKeys.LOG_SOURCE_ID.name)
        private val ICON_STATE_KEY = Key.create<IconState>("RemoteLogConsole_IconState")
    }

    private val logService = project.getService(LogService::class.java)
    private val settingsService = project.getService(SettingsService::class.java)
    private val contentFactory = ContentFactory.SERVICE.getInstance()

    private val emptyContent: Content = contentFactory.createContent(NoActiveTabsView(project).content, "", false)

    init {
        emptyContent.isCloseable = false
        val activeSources = settingsService.getActiveSources()
        activeSources.forEach { addTab(it) }

        maybeDisplayEmptyContent()

        contentManager.addContentManagerListener(this)
    }

    fun addTab(logSource: LogSource) {
        if (contentManager.contentCount == 1) {
            // remove empty content placeholder if needed
            contentManager.removeContent(emptyContent, false)
        }

        val toolWindow = ToolWindow(project, logSource.id)
        Disposer.register(this, toolWindow)
        val content = contentFactory.createContent(toolWindow.content, logSource.name, false).apply {
            putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, true)
            putUserData(LOG_SOURCE_KEY, logSource.id)
            putUserData(ICON_STATE_KEY, IconState())
            isCloseable = true
            icon = TabIconUtil.getTabIcon(running = false, newContent = false)
        }
        settingsService.activateSource(logSource.id)
        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
    }

    fun configurationChanged(settings: Settings) {
        contentManager.contents.forEach { content ->
            content.getUserData(LOG_SOURCE_KEY)?.let { srcId ->
                settings.sources.firstOrNull { it.id == srcId }?.let { src ->
                    content.displayName = src.name
                }
            }
        }
    }

    override fun contentRemoved(event: ContentManagerEvent) {
        event.content.getUserData(LOG_SOURCE_KEY)?.let {
            settingsService.deactivateSource(it)
            logService.stop(it)
            maybeDisplayEmptyContent()
        }
    }

    override fun selectionChanged(event: ContentManagerEvent) {
        if (event.operation == ContentManagerEvent.ContentOperation.add) {
            // refresh icon
            withContentIconState(event.content) {}
        }
    }

    override fun dispose() {
        // disposable just to register tabs as children into the disposer
    }

    override fun newContentAdded(sourceId: String) {
        withContentIconState(sourceId) {
            newContent = true
        }
    }

    override fun sourceStarted(sourceId: String) {
        withContentIconState(sourceId) {
            running = true
        }
    }

    override fun sourceStopped(sourceId: String) {
        withContentIconState(sourceId) {
            running = false
        }
    }

    private fun maybeDisplayEmptyContent() {
        if (contentManager.contentCount == 0) {
            contentManager.addContent(emptyContent)
        }
    }

    private fun withContentIconState(sourceId: String, action: IconState.() -> Unit) {
        contentManager.contents
            .firstOrNull { it.getUserData(LOG_SOURCE_KEY) == sourceId}
            ?.let { withContentIconState(it, action) }

    }

    private fun withContentIconState(content: Content, action: IconState.() -> Unit) {
        content.getUserData(ICON_STATE_KEY)?.let { state ->
            action(state)
            updateContentIcon(content, state)
        }
    }

    private fun updateContentIcon(content: Content, state: IconState) {
        if (contentManager.isSelected(content)) {
            // reset new content flag for currently selected tab
            state.newContent = false
        }

        content.icon = TabIconUtil.getTabIcon(running = state.running, newContent = state.newContent)
    }

    private data class IconState(var running: Boolean = false, var newContent: Boolean = false)

}