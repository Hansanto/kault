package io.github.hansanto.kault.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

inline fun runBlockingTest(crossinline block: suspend () -> Unit): TestResult {
    return runTest {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            block()
        }
    }
}
