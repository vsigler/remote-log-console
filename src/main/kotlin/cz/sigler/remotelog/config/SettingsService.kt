package cz.sigler.remotelog.config

import com.intellij.openapi.components.*

@Service(Service.Level.PROJECT)
@State(name = "RemoteLogConsole", storages = [Storage("RemoteLogConsole.xml", roamingType = RoamingType.DISABLED)])
class SettingsService: PersistentStateComponent<Settings> {

    private var currentSettings : Settings = Settings()

    private val listeners = mutableListOf<(Settings) -> Unit>()

    override fun loadState(state: Settings) {
        state.activeSources = state.activeSources
            .filter { activeSource ->
                state.sources.any { it.id == activeSource }
            }
            .toMutableSet()

        this.currentSettings = state
        listeners.forEach { it.invoke(state) }
    }

    override fun getState(): Settings = currentSettings

    fun getActiveSources() = state.activeSources.mapNotNull { getSource(it) }

    fun activateSource(id: String) {
        state.activeSources.add(id)
    }

    fun deactivateSource(id: String) {
        state.activeSources.removeAll { it == id }
    }

    fun getSource(id: String) : LogSource? {
        return state.sources
            .firstOrNull { it.id == id }
    }

    fun addChangeListener(callback: (Settings) -> Unit) {
        listeners.add(callback)
    }

}