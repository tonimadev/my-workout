package digital.tonima.myworkout.ui.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack(): Boolean {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return false

        if (currentStack.size <= 1) {
            if (state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
                return true
            }
            return false
        } else {
            currentStack.removeLastOrNull()
            return true
        }
    }
}
