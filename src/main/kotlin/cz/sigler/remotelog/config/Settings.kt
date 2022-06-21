package cz.sigler.remotelog.config

import java.io.Serializable

data class Settings(
    var activeSource: String? = null,
    var sources: List<LogSource> = mutableListOf()
) : Serializable

