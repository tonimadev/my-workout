package digital.tonima.myworkout.features.stats.bridge

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface StatsDestination : NavKey {
    @Serializable
    data object Stats : StatsDestination
}
