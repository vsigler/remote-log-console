package cz.sigler.remotelog.config

import java.io.Serializable

data class Settings(
    var activeSources: MutableSet<String> = LinkedHashSet(),
    var sources: List<LogSource> = mutableListOf()
) : Serializable

