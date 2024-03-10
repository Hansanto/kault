package io.github.hansanto.kault.util

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

fun workingDirectory(): Path {
    return SystemFileSystem.resolve(Path("."))
}

fun Path.resolve(vararg paths: String): Path {
    return Path(this, *paths)
}

fun Path.exists(): Boolean {
    return SystemFileSystem.exists(this)
}

fun Path.readLines(): String {
    return SystemFileSystem.source(this).buffered().use { it.readString() }
}

tailrec fun findFolderInParent(folderName: String, startFolder: Path = workingDirectory()): Path? {
    val folderPath = Path(startFolder, folderName)
    if (folderPath.exists()) {
        return folderPath
    }

    val parent = Path(startFolder).parent ?: return null
    return findFolderInParent(folderName, parent)
}