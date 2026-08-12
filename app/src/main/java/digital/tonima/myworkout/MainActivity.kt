package digital.tonima.myworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.ui.history.HistoryScreen
import digital.tonima.myworkout.ui.history.HistoryViewModel
import digital.tonima.myworkout.ui.navigation.*
import digital.tonima.myworkout.ui.navigation.Destination.WorkoutEdit
import digital.tonima.myworkout.ui.theme.MyWorkoutTheme
import digital.tonima.myworkout.ui.workout.WorkoutEditScreen
import digital.tonima.myworkout.ui.workout.WorkoutListScreen
import digital.tonima.myworkout.ui.workout.WorkoutTrackingScreen
import digital.tonima.myworkout.ui.workout.WorkoutViewModel

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
    ExperimentalMaterial3Api::class
)
@Composable
fun AppNavigation() {
    val topLevelRoutes = remember { setOf(Destination.WorkoutList as NavKey, Destination.History as NavKey) }
    val navigationState = rememberNavigationState(
        startRoute = Destination.WorkoutList,
        topLevelRoutes = topLevelRoutes
    )
    val navigator = remember { Navigator(navigationState) }

    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    val entryProvider = entryProvider<NavKey> {
        entry<Destination.WorkoutList>(
            metadata = ListDetailSceneStrategy.listPane()
        ) {
            val viewModel: WorkoutViewModel = hiltViewModel()
            val workouts by viewModel.workouts.collectAsState()
            WorkoutListScreen(
                workouts = workouts,
                onWorkoutClick = { id -> navigator.navigate(WorkoutEdit(id)) },
                onAddWorkout = { name -> viewModel.addWorkout(name) },
                onDeleteWorkout = { workout -> viewModel.deleteWorkout(workout.workout) }
            )
        }

        entry<WorkoutEdit>(
            metadata = ListDetailSceneStrategy.detailPane()
        ) { key ->
            val viewModel: WorkoutViewModel = hiltViewModel()
            val workout by viewModel.getWorkout(key.workoutId ?: -1).collectAsState(null)
            WorkoutEditScreen(
                workout = workout,
                onBack = { navigator.goBack() },
                onStartWorkout = { id -> navigator.navigate(Destination.WorkoutTracking(id)) },
                onAddExercise = { id, name -> viewModel.addExercise(id, name) },
                onAddSet = { workoutId, exerciseId -> viewModel.addSet(workoutId, exerciseId) },
                onUpdateSet = { wId, eId, sId, weight, reps, rest ->
                    viewModel.updateSet(wId, eId, sId, weight, reps, rest)
                }
            )
        }

        entry<Destination.WorkoutTracking> { key ->
            val viewModel: WorkoutViewModel = hiltViewModel()
            val workout by viewModel.getWorkout(key.workoutId).collectAsState(null)
            val activeSession by viewModel.activeSession.collectAsState()

            // Start workout if not already started
            LaunchedEffect(key.workoutId) {
                viewModel.startWorkout(key.workoutId)
            }

            WorkoutTrackingScreen(
                workout = workout,
                activeSession = activeSession,
                onLogSet = { sessionId, exerciseId, setId, weight, reps ->
                    viewModel.logSet(sessionId, exerciseId, setId, weight, reps)
                },
                onFinish = {
                    viewModel.finishWorkout()
                    navigator.goBack()
                },
                onCancel = {
                    navigator.goBack()
                }
            )
        }

        entry<Destination.History> {
            val viewModel: HistoryViewModel = hiltViewModel()
            val sessions by viewModel.sessions.collectAsState()
            HistoryScreen(sessions = sessions)
        }
    }

    val entries = navigationState.toEntries(entryProvider)
    
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = navigationState.topLevelRoute == Destination.WorkoutList,
                onClick = { navigator.navigate(Destination.WorkoutList) },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.nav_workouts)) },
                label = { Text(stringResource(R.string.nav_workouts)) }
            )
            item(
                selected = navigationState.topLevelRoute == Destination.History,
                onClick = { navigator.navigate(Destination.History) },
                icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.nav_history)) },
                label = { Text(stringResource(R.string.nav_history)) }
            )
        }
    ) {
        NavDisplay(
            entries = entries,
            onBack = { navigator.goBack() },
            sceneStrategy = listDetailStrategy
        )
    }
}
