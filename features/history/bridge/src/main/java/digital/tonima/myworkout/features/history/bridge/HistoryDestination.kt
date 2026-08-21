package digital.tonima.myworkout.features.history.bridge

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface HistoryDestination : NavKey {
    @Serializable
    data object History : HistoryDestination
}
