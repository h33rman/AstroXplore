package com.example.astroxplore.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationSignal @Inject constructor() {
    private val _scrollToTop = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTop = _scrollToTop.asSharedFlow()

    fun onScrollToTop() {
        _scrollToTop.tryEmit(Unit)
    }
}
