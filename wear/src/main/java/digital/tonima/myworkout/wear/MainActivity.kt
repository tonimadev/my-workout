package digital.tonima.myworkout.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.wear.ui.WorkoutExecutionScreen
import digital.tonima.myworkout.wear.ui.WorkoutListScreen
import digital.tonima.myworkout.wear.ui.WorkoutViewModel
import digital.tonima.myworkout.wear.ui.navigation.Screen
import digital.tonima.myworkout.wear.ui.navigation.Screen.WorkoutExecution
import digital.tonima.myworkout.wear.ui.navigation.Screen.WorkoutList

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    MaterialTheme {
        AppScaffold {
            val backStack = remember { mutableStateListOf<Screen>(WorkoutList) }

            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                sceneStrategies = listOf(SwipeDismissableSceneStrategy())
            ) { key ->
                NavEntry(key) {
                    val viewModel: WorkoutViewModel = hiltViewModel()
                    when (key) {
                        is WorkoutList -> {
                            val workouts by viewModel.workouts.collectAsState()
                            WorkoutListScreen(
                                workouts = workouts,
                                onWorkoutClick = { workoutId ->
                                    backStack.add(WorkoutExecution(workoutId))
                                }
                            )
                        }
                        is WorkoutExecution -> {
                            val workout by viewModel.currentWorkout.collectAsState()
                            val activeSession by viewModel.activeSession.collectAsState()
                            val restTime by viewModel.restTimeRemaining.collectAsState()
                            
                            LaunchedEffect(key.workoutId) {
                                viewModel.loadWorkout(key.workoutId)
                                viewModel.startSession(key.workoutId)
                            }
                            
                            workout?.let {
                                WorkoutExecutionScreen(
                                    workout = it,
                                    activeSession = activeSession,
                                    restTimeRemaining = restTime,
                                    onCompleteSet = { exerciseId, setId, weight, reps, rest ->
                                        viewModel.completeSet(exerciseId, setId, weight, reps, rest)
                                    },
                                    onFinishSession = {
                                        viewModel.finishSession()
                                        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
