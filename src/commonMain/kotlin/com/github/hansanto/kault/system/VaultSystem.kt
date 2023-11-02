package com.github.hansanto.kault.system

import com.github.hansanto.kault.system.auth.VaultSystemAuth
import com.github.hansanto.kault.system.auth.VaultSystemAuthImpl
import io.ktor.client.HttpClient

public class VaultSystem(
    private val client: HttpClient,
    public val path: String = "sys"
) {

    public val auth: VaultSystemAuth = VaultSystemAuthImpl(client, "$path/auth")
}
