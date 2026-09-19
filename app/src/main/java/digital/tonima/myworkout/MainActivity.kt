package digital.tonima.myworkout

import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy.Companion.detailPane
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import digital.tonima.myworkout.features.stats.impl.StatsScreen
import digital.tonima.myworkout.features.stats.impl.StatsViewModel
import digital.tonima.myworkout.features.workout.bridge.WorkoutDestination.WorkoutEdit
import digital.tonima.myworkout.features.workout.bridge.WorkoutDestination.WorkoutList
import digital.tonima.myworkout.features.workout.bridge.WorkoutDestination.WorkoutTracking
import digital.tonima.myworkout.features.workout.impl.WorkoutEditScreen
import digital.tonima.myworkout.features.workout.impl.WorkoutIntent.LoadWorkout
import digital.tonima.myworkout.features.workout.impl.WorkoutIntent.ResetNavigation
import digital.tonima.myworkout.features.workout.impl.WorkoutIntent.StartWorkout
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
    // Needed on Android 13+ so the rest-timer completion alert can post a notification when the
    // app is backgrounded mid-rest.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
        LaunchedEffect(Unit) {
            permissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsStateWithLifecycle()

    if (!onboardingCompleted) {
        OnboardingScreen(
            onComplete = { onboardingViewModel.completeOnboarding() },
            onApplyTemplates = { templates -> onboardingViewModel.applyTemplates(templates) },
        )
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
        entryProvider {
            entry<WorkoutList>(
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { EmptyDetailPane() }),
            ) {
                val viewModel: WorkoutViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                WorkoutListScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
                    onWorkoutClick = { id -> navigator.navigate(WorkoutEdit(id)) },
                )
            }

            entry<WorkoutEdit>(
                metadata = detailPane(),
            ) { key ->
                val viewModel: WorkoutViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key.workoutId) {
                    key.workoutId?.let { viewModel.onIntent(LoadWorkout(it)) }
                }

                WorkoutEditScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
                    onBack = { navigator.goBack() },
                    onStartWorkout = { id -> navigator.navigate(WorkoutTracking(id)) },
                )
            }

            entry<WorkoutTracking> { key ->
                val viewModel: WorkoutViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key.workoutId) {
                    viewModel.onIntent(LoadWorkout(key.workoutId))
                    viewModel.onIntent(StartWorkout(key.workoutId))
                }

                LaunchedEffect(state.shouldNavigateBack) {
                    if (state.shouldNavigateBack) {
                        navigator.goBack()
                        viewModel.onIntent(ResetNavigation)
                    }
                }

                WorkoutTrackingScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
                    onCancel = { navigator.goBack() },
                )
            }

            entry<History> {
                val viewModel: HistoryViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                HistoryScreen(state = state)
            }

            entry<Stats> {
                val viewModel: StatsViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()

                StatsScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
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

// Shown in the detail pane of the list-detail layout (wide/expanded windows) when no workout
// is selected yet, so the pane never renders as dead empty space next to the list.
@Composable
private fun EmptyDetailPane() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                                    ),
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.select_workout_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.select_workout_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
