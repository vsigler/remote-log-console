package cz.sigler.remotelog.config

import java.io.Serializable

data class Settings(
    var activeSources: MutableList<String> = mutableListOf(),
    var sources: List<LogSource> = mutableListOf()
) : Serializable

