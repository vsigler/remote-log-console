package cz.sigler.remotelog.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class RemoteLogConfigurable(project: Project): Configurable {

    private val settingsService = project.getService(SettingsService::class.java)

    private val initialList : List<LogSource> = settingsService.state.sources
    private lateinit var form : LogSourcesForm

    override fun createComponent(): JComponent? {
        form = LogSourcesForm(initialList)

        return form.rootComponent
    }

    override fun isModified(): Boolean {
        return form.items != initialList
    }

    override fun apply() {
        val oldActiveSources = settingsService.state.activeSources
        val newSources = form.items

        val newSettings = Settings(
            activeSources = newSources.map { it.id }
                .filter { oldActiveSources.contains(it) }
                .toMutableSet(),
            sources = newSources
        )

        settingsService.loadState(newSettings)
    }

    override fun getDisplayName() = "Remote Log Console"
}