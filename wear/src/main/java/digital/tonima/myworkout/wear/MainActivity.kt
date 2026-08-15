package digital.tonima.myworkout.wear

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.ambient.AmbientLifecycleObserver.AmbientDetails
import androidx.wear.ambient.AmbientLifecycleObserver.AmbientLifecycleCallback
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
    private val ambientCallback =
        object : AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientDetails) {
                isAmbientMode = true
            }

            override fun onExitAmbient() {
                isAmbientMode = false
            }
        }

    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)
    private var isAmbientMode by mutableStateOf(false)
    private var workoutIdFromIntent by mutableLongStateOf(-1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        workoutIdFromIntent = intent.getLongExtra("workout_id", -1L)

        lifecycle.addObserver(ambientObserver)
        setContent {
            WearApp(isAmbientMode, workoutIdFromIntent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val id = intent.getLongExtra("workout_id", -1L)
        if (id != -1L) {
            workoutIdFromIntent = id
        }
    }
}

@Composable
fun WearApp(
    isAmbientMode: Boolean,
    initialWorkoutId: Long,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { _ -> }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    MaterialTheme {
        AppScaffold {
            val backStack =
                remember {
                    val list = mutableStateListOf<Screen>(WorkoutList)
                    if (initialWorkoutId != -1L) {
                        list.add(WorkoutExecution(initialWorkoutId))
                    }
                    list
                }

            LaunchedEffect(initialWorkoutId) {
                if (initialWorkoutId != -1L &&
                    backStack.none {
                        it is WorkoutExecution && it.workoutId == initialWorkoutId
                    }
                ) {
                    backStack.add(WorkoutExecution(initialWorkoutId))
                }
            }

            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                sceneStrategies = listOf(SwipeDismissableSceneStrategy()),
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
                                },
                            )
                        }
                        is WorkoutExecution -> {
                            val workout by viewModel.currentWorkout.collectAsState()
                            val activeSession by viewModel.activeSession.collectAsState()
                            val restTime by viewModel.restTimeRemaining.collectAsState()
                            val totalRestTime by viewModel.totalRestTime.collectAsState()
                            val isResting by viewModel.isResting.collectAsState()
                            val lastXpGained by viewModel.lastXpGained.collectAsState()

                            LaunchedEffect(key.workoutId) {
                                viewModel.loadWorkout(key.workoutId)
                                viewModel.startSession(key.workoutId)
                            }

                            workout?.let {
                                WorkoutExecutionScreen(
                                    workout = it,
                                    activeSession = activeSession,
                                    restTimeRemaining = restTime,
                                    totalRestTime = totalRestTime,
                                    isResting = isResting,
                                    isAmbientMode = isAmbientMode,
                                    xpGained = lastXpGained,
                                    onCompleteSet = { exerciseId, setId, weight, reps, rest ->
                                        viewModel.completeSet(exerciseId, setId, weight, reps, rest)
                                    },
                                    onSkipRest = { viewModel.skipRest() },
                                    onFinishSession = {
                                        viewModel.finishSession()
                                        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
