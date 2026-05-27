package com.orchardlog.treedata.shared

import kotlin.time.Clock
import kotlin.time.Instant

object TemporalUtils {
    /**
     * Returns the current [Instant] using the system clock.
     */
    fun now(): Instant = Clock.System.now()
    
    // 9999-12-31T23:59:59.999Z
    val INFINITY = Instant.fromEpochMilliseconds(253402300799999L)

    fun randomUUID(): String = com.benasher44.uuid.uuid4().toString()
}
