package digital.tonima.myworkout.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base class for MVI ViewModels.
 * @param S State
 * @param I Intent
 * @param E Effect
 */
abstract class MviViewModel<S, I, E>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    protected var currentState: S
        get() = _state.value
        set(value) {
            _state.value = value
        }

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    protected fun updateState(reduce: S.() -> S) {
        val newState = currentState.reduce()
        _state.value = newState
    }
}
