package cz.sigler.remotelog.config

import java.net.InetSocketAddress
import java.util.*

data class LogSource(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "localhost",
    var host: String = "localhost",
    var port: Int = 19001,
    var reconnect: Boolean = true,
    var reconnectAttempts: Int = 20) {

    override fun toString() = name

    fun toAddress() : InetSocketAddress {
        return InetSocketAddress(host, port)
    }
}