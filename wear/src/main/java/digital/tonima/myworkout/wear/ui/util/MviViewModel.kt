package digital.tonima.myworkout.wear.ui.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class MviViewModel<S, I>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected var currentState: S
        get() = _state.value
        set(value) {
            _state.value = value
        }

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun updateState(reduce: S.() -> S) {
        val newState = currentState.reduce()
        _state.value = newState
    }
}
