package com.github.hansanto.kault.auth

import com.github.hansanto.kault.auth.approle.VaultAuthAppRole
import com.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.ktor.client.HttpClient

public class VaultAuth(client: HttpClient) {

    public var token: String? = null

    public val appRole: VaultAuthAppRole = VaultAuthAppRoleImpl(client)
}
