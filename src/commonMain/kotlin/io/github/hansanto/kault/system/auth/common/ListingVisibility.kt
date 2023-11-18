package io.github.hansanto.kault.system.auth.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ListingVisibilitySerializer::class)
public enum class ListingVisibility(public val value: String) {

    /**
     * Mount is not visible.
     */
    HIDDEN("hidden"),

    /**
     * Mount is marked as internal-only.
     */
    UNAUTH("unauth");
}

/**
 * Serializer for [ListingVisibility].
 * Use the name of the enum as the serialized value after converting it to lowercase.
 */
public object ListingVisibilitySerializer : EnumSerializer<ListingVisibility>("listingVisibility", ListingVisibility.entries, { it.value })
