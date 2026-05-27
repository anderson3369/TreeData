package com.orchardlog.treedata.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A wrapper for StateFlow that is easily consumable from Swift.
 */
class CommonStateFlow<T>(private val flow: StateFlow<T>) : StateFlow<T> by flow {
    fun subscribe(
        onCollect: (T) -> Unit
    ): Disposable {
        val job = flow.onEach { onCollect(it) }.launchIn(CoroutineScope(Dispatchers.Main))
        return Disposable { job.cancel() }
    }
}

fun interface Disposable {
    fun dispose()
}

fun <T> StateFlow<T>.asCommonStateFlow(): CommonStateFlow<T> = CommonStateFlow(this)
