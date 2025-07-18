package io.github.hansanto.kault.util

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

fun workingDirectory(): Path {
    return SystemFileSystem.resolve(Path("."))
}

fun Path.resolve(path: String): Path {
    return Path(this, path)
}

fun Path.exists(): Boolean {
    return SystemFileSystem.exists(this)
}

fun Path.readLines(): String {
    return SystemFileSystem.source(this).buffered().use { it.readString() }
}

/**
 * Search the file in the working directory and its parent folders.
 * @param fileName File name to find in directory (can be a simple file name or a path).
 * @param startFolder Folder to start the search from.
 * @return Path of the file if found, null otherwise.
 */
tailrec fun findFileInParentDirectories(fileName: String, startFolder: Path = workingDirectory()): Path? {
    val filePath = Path(startFolder, fileName)
    if (filePath.exists()) {
        return filePath
    }

    val parent = startFolder.parent ?: return null
    return findFileInParentDirectories(fileName, parent)
}
