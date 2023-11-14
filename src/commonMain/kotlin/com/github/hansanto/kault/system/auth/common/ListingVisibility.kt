package com.github.hansanto.kault.system.auth.common

import com.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ListingVisibilitySerializer::class)
public enum class ListingVisibility {

    /**
     * Mount is not visible.
     */
    HIDDEN,

    /**
     * Mount is marked as internal-only.
     */
    UNAUTH;
}

/**
 * Serializer for [ListingVisibility].
 * Use the name of the enum as the serialized value after converting it to lowercase.
 */
public object ListingVisibilitySerializer : EnumSerializer<ListingVisibility>("listingVisibility", ListingVisibility.entries, { it.name.lowercase() })
