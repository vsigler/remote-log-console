package cz.sigler.remotelog.config

import com.intellij.openapi.components.*

@Service
@State(name = "RemoteLogConsole", storages = [Storage("RemoteLogConsole.xml", roamingType = RoamingType.DISABLED)])
class SettingsService: PersistentStateComponent<Settings> {

    var currentSettings : Settings = Settings()

    override fun loadState(state: Settings) {
        state.activeSources = state.activeSources
            .filter { activeSource ->
                state.sources.any { it.id == activeSource }
            }
            .toMutableList()

        this.currentSettings = state
    }

    override fun getState(): Settings = currentSettings

    fun activateSource(id: String) {
        state.activeSources.add(id)
    }

    fun deactivateSource(id: String) {
        state.activeSources.remove(id)
    }

    fun getSource(id: String) : LogSource? {
        return state.sources
            .firstOrNull { it.id == id }
    }

}