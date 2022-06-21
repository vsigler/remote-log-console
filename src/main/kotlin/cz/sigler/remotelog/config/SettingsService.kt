package cz.sigler.remotelog.config

import com.intellij.openapi.components.*

@Service
@State(name = "RemoteLogConsole", storages = [Storage("RemoteLogConsole.xml", roamingType = RoamingType.DISABLED)])
class SettingsService: PersistentStateComponent<Settings> {

    var currentSettings : Settings = Settings()

    override fun loadState(state: Settings) {
        this.currentSettings = state
    }

    override fun getState(): Settings = currentSettings

    fun getActiveSource() : LogSource? {
        val localState = state
        val active = localState.activeSource?:localState.sources.firstOrNull()?.id
        localState.activeSource = active

        return localState.sources
            .firstOrNull { it.id == active }
    }

}