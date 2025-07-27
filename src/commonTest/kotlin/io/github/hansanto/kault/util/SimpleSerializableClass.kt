package io.github.hansanto.kault.util

import kotlinx.serialization.Serializable

@Serializable
data class SimpleSerializableClass(val a: String, val b: Int)

@Serializable
data class ComplexSerializableClass(val a: String, val b: Int? = null, val c: SimpleSerializableClass)
