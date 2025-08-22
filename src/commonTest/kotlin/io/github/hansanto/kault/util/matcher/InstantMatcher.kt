package io.github.hansanto.kault.util.matcher

import io.kotest.matchers.comparables.between
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

infix fun Instant.shouldBeBetween(range: ClosedRange<Instant>) {
    this.toEpochMilliseconds() shouldBe between(
        range.start.toEpochMilliseconds(),
        range.endInclusive.toEpochMilliseconds()
    )
}
