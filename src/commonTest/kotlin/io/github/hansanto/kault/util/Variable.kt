package io.github.hansanto.kault.util

import io.kotest.mpp.syspropOrEnv

interface Variable {
    /**
     * Value of the variable.
     */
    val value: String
}

typealias VariableFallback = () -> String?

/**
 * Load a value from the system properties or environment variables.
 * If the value is not found, the fallback will be used to retrieve a valid value.
 * @property name Name of the environment variable.
 * @property fallback Fallback to use if the environment variable is not set.
 */
data class EnvVariable(
    val name: String,
    val fallback: VariableFallback = { null }
) : Variable {
    override val value: String by lazy {
        syspropOrEnv(name) ?: fallback() ?: throw IllegalStateException("Environment variable $name is not set")
    }
}

/**
 * Load a value from the content of a file from the resources test folder or through the system file by walking through the parent folders.
 * @property pathFile Path of the file to load.
 */
data class FileVariable(
    val pathFile: String
) : Variable {
    override val value: String by lazy {
        readFileFromResources()
            ?: readFileFromSystem()
            ?: throw IllegalStateException("File $pathFile is not findable in the system or resources folder")
    }

    /**
     * Read the file from the system file.
     * From the current path, walk through the parent folders to find the file.
     * @return Content of the file if readable, null otherwise.
     */
    private fun readFileFromSystem(): String? {
        val (folderToFind, childrenPath) = pathFile.split('/', limit = 2)
        val folderPath = findFolderInParent(folderToFind)
        if (folderPath == null || !folderPath.exists()) {
            return null
        }

        val targetedFilePath = folderPath.resolve(childrenPath)
        if (!targetedFilePath.exists()) {
            return null
        }

        return targetedFilePath.readLines()
    }

    /**
     * Read the file from the resources' folder.
     * @return Content of the file if readable, null otherwise.
     */
    private fun readFileFromResources(): String? {
        return runCatching { pathFile.asFileResource().readText() }.getOrNull()
    }
}
