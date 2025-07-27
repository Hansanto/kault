package io.github.hansanto.kault.util

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

fun workingDirectory(): Path = SystemFileSystem.resolve(Path("."))

fun Path.exists(): Boolean = SystemFileSystem.exists(this)

fun Path.readLines(): String = SystemFileSystem.source(this).buffered().use { it.readString() }

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
