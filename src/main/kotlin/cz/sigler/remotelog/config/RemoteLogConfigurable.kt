package cz.sigler.remotelog.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class RemoteLogConfigurable(private val project: Project): Configurable {

    private val settingsService = project.getService(SettingsService::class.java)

    private val initialList : List<LogSource> = settingsService.state.sources
    lateinit var form : LogSourcesForm

    init {
        print("asdas")
    }

    override fun createComponent(): JComponent? {
        form = LogSourcesForm(initialList)

        return form.rootComponent
    }

    override fun isModified(): Boolean {
        return form.items != initialList
    }

    override fun apply() {
        val oldActiveSource = settingsService.state.activeSource
        val newSources = form.items

        val newSettings = Settings(
            activeSource = if (newSources.map { it.id }.contains(oldActiveSource)) oldActiveSource else null,
            sources = newSources
        )

        settingsService.loadState(newSettings)
    }

    override fun getDisplayName() = "Remote Log Console"
}