package com.github.hansanto.kault.extension

public fun String.addChildPath(path: String, separator: String = "/"): String {
    return if (this.endsWith(separator)) {
        this + path
    } else {
        "$this$separator$path"
    }
}
