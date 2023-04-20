package com.example.hikerview.ui.ext

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * 作者：By 15968
 * 日期：On 2022/10/11
 * 时间：At 16:06
 */
internal class BootstrapDns(
    private val dnsHostname: String,
    private val dnsServers: List<InetAddress>
) : Dns {
    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        if (this.dnsHostname != hostname) {
            throw UnknownHostException(
                "BootstrapDns called for $hostname instead of $dnsHostname"
            )
        }

        return dnsServers
    }
}