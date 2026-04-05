package io.github.hansanto.kault.util

import io.github.hansanto.kault.system.mounts.payload.MountsEnableSecretsEnginePayload
import io.github.hansanto.kault.system.mounts.response.MountsGetConfigurationOfSecretEngineResponse

fun MountsGetConfigurationOfSecretEngineResponse.toMountsEnableSecretsEnginePayload() =
    MountsEnableSecretsEnginePayload(
        type = type,
        description = description,
        config = MountsEnableSecretsEnginePayload.Config(
            defaultLeaseTTL = config.defaultLeaseTTL,
            maxLeaseTTL = config.maxLeaseTTL,
            forceNoCache = config.forceNoCache,
            auditNonHmacRequestKeys = config.auditNonHmacRequestKeys,
            auditNonHmacResponseKeys = config.auditNonHmacResponseKeys,
            listingVisibility = config.listingVisibility,
            passthroughRequestHeaders = config.passthroughRequestHeaders,
            allowedResponseHeaders = config.allowedResponseHeaders,
            pluginVersion = pluginVersion,
            allowedManagedKeys = config.allowedManagedKeys,
            delegatedAuthAccessors = null,
            identityTokenKey = null
        ),
        options = options,
        local = local,
        sealWrap = sealWrap,
        externalEntropyAccess = externalEntropyAccess
    )
