package io.github.hansanto.kault.util

import io.kotest.mpp.syspropOrEnv

interface Valuable {
    val value: String
}

typealias ValueFallback = () -> String?

/**
 * Load a value from the system properties or environment variables.
 * If the value is not found, the fallback will be used to retrieve a valid value.
 * @property name Name of the environment variable.
 * @property fallback Fallback to use if the environment variable is not set.
 */
data class EnvValue(
    val name: String,
    val fallback: ValueFallback = { null }
) : Valuable {
    override val value: String by lazy {
        syspropOrEnv(name) ?: fallback() ?: throw IllegalStateException("Environment variable [$name] is not set")
    }
}

/**
 * Load a value from a file in the system file.
 * @property pathFile Path of the file to load.
 * @property fallback Fallback to use if the file is not found.
 */
data class SystemFileValue(
    val pathFile: String,
    val fallback: ValueFallback = { null }
) : Valuable {
    override val value: String by lazy {
        readFileFromSystem()
            ?: fallback()
            ?: throw IllegalStateException("File [$pathFile] is not findable in the file system")
    }

    /**
     * Read the file from the system file.
     * From the current path, walk through the parent folders to find the file.
     * @return Content of the file if readable, null otherwise.
     */
    private fun readFileFromSystem(): String? {
        val filePath = findFileInParentDirectories(pathFile)
        if (filePath == null || !filePath.exists()) {
            return null
        }

        return filePath.readLines()
    }
}

/**
 * Load a value from a file in the "resources" folder.
 * @property pathFile Path of the file to load.
 * @property fallback Fallback to use if the file is not found.
 */
data class ResourceValue(
    val pathFile: String,
    val fallback: ValueFallback = { null }
) : Valuable {
    override val value: String by lazy {
        readFileFromResources()
            ?: fallback()
            ?: throw IllegalStateException("File [$pathFile] is not findable in the resources folder")
    }

    /**
     * Read the file from the "resources" folder.
     * @return Content of the file if readable, null otherwise.
     */
    private fun readFileFromResources(): String? {
        return runCatching { pathFile.asResourceFile().readLines() }.getOrNull()
    }
}
