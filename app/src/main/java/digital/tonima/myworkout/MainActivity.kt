package digital.tonima.myworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.features.history.bridge.HistoryDestination.History
import digital.tonima.myworkout.features.history.impl.HistoryScreen
import digital.tonima.myworkout.features.history.impl.HistoryViewModel
import digital.tonima.myworkout.features.onboarding.impl.OnboardingScreen
import digital.tonima.myworkout.features.onboarding.impl.OnboardingViewModel
import digital.tonima.myworkout.features.stats.bridge.StatsDestination.Stats
import digital.tonima.myworkout.features.stats.impl.StatsIntent
import digital.tonima.myworkout.features.stats.impl.StatsScreen
import digital.tonima.myworkout.features.stats.impl.StatsViewModel
import digital.tonima.myworkout.features.workout.bridge.WorkoutDestination.WorkoutEdit
import digital.tonima.myworkout.features.workout.bridge.WorkoutDestination.WorkoutList
import digital.tonima.myworkout.features.workout.bridge.WorkoutDestination.WorkoutTracking
import digital.tonima.myworkout.features.workout.impl.WorkoutEditScreen
import digital.tonima.myworkout.features.workout.impl.WorkoutIntent
import digital.tonima.myworkout.features.workout.impl.WorkoutListScreen
import digital.tonima.myworkout.features.workout.impl.WorkoutTrackingScreen
import digital.tonima.myworkout.features.workout.impl.WorkoutViewModel
import digital.tonima.myworkout.ui.navigation.Navigator
import digital.tonima.myworkout.ui.navigation.rememberNavigationState
import digital.tonima.myworkout.ui.navigation.toEntries
import digital.tonima.myworkout.ui.theme.MyWorkoutTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWorkoutTheme {
                AppNavigation()
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun AppNavigation() {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsState()

    if (!onboardingCompleted) {
        OnboardingScreen(onComplete = { onboardingViewModel.completeOnboarding() })
    } else {
        MainAppContent()
    }
}

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun MainAppContent() {
    val topLevelRoutes = remember { setOf(WorkoutList as NavKey, History as NavKey, Stats as NavKey) }
    val navigationState =
        rememberNavigationState(
            startRoute = WorkoutList as NavKey,
            topLevelRoutes = topLevelRoutes,
        )
    val navigator = remember { Navigator(navigationState) }

    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    val entryProvider =
        entryProvider<NavKey> {
            entry<WorkoutList>(
                metadata = ListDetailSceneStrategy.listPane(),
            ) {
                val viewModel: WorkoutViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val onIntent = remember(viewModel) { { intent: WorkoutIntent -> viewModel.onIntent(intent) } }
                val onWorkoutClick = remember { { id: Long -> navigator.navigate(WorkoutEdit(id)) } }

                WorkoutListScreen(
                    state = state,
                    onIntent = onIntent,
                    onWorkoutClick = onWorkoutClick,
                )
            }

            entry<WorkoutEdit>(
                metadata = detailPane(),
            ) { key ->
                val viewModel: WorkoutViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val onIntent = remember(viewModel) { { intent: WorkoutIntent -> viewModel.onIntent(intent) } }
                val onBack = remember { { navigator.goBack() } }
                val onStartWorkout = remember { { id: Long -> navigator.navigate(WorkoutTracking(id)) } }

                LaunchedEffect(key.workoutId) {
                    key.workoutId?.let { viewModel.onIntent(WorkoutIntent.LoadWorkout(it)) }
                }

                WorkoutEditScreen(
                    state = state,
                    onIntent = onIntent,
                    onBack = onBack,
                    onStartWorkout = onStartWorkout,
                )
            }

            entry<WorkoutTracking> { key ->
                val viewModel: WorkoutViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val onIntent = remember(viewModel) { { intent: WorkoutIntent -> viewModel.onIntent(intent) } }
                val onCancel = remember { { navigator.goBack() } }

                LaunchedEffect(key.workoutId) {
                    viewModel.onIntent(WorkoutIntent.LoadWorkout(key.workoutId))
                    viewModel.onIntent(WorkoutIntent.StartWorkout(key.workoutId))
                }

                LaunchedEffect(state.shouldNavigateBack) {
                    if (state.shouldNavigateBack) {
                        navigator.goBack()
                        viewModel.onIntent(WorkoutIntent.ResetNavigation)
                    }
                }

                WorkoutTrackingScreen(
                    state = state,
                    onIntent = onIntent,
                    onCancel = onCancel,
                )
            }

            entry<History> {
                val viewModel: HistoryViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                HistoryScreen(state = state)
            }

            entry<Stats> {
                val viewModel: StatsViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val onIntent = remember(viewModel) { { intent: StatsIntent -> viewModel.onIntent(intent) } }

                StatsScreen(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }

    val entries = navigationState.toEntries(entryProvider)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = navigationState.topLevelRoute == WorkoutList,
                onClick = { navigator.navigate(WorkoutList) },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(R.string.nav_workouts),
                    )
                },
                label = { Text(stringResource(R.string.nav_workouts)) },
            )
            item(
                selected = navigationState.topLevelRoute == History,
                onClick = { navigator.navigate(History) },
                icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.nav_history)) },
                label = { Text(stringResource(R.string.nav_history)) },
            )
            item(
                selected = navigationState.topLevelRoute == Stats,
                onClick = { navigator.navigate(Stats) },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = stringResource(R.string.nav_stats),
                    )
                },
                label = { Text(stringResource(R.string.nav_stats)) },
            )
        },
    ) {
        NavDisplay(
            entries = entries,
            onBack = { navigator.goBack() },
            sceneStrategies = listOf(listDetailStrategy),
        )
    }
}
