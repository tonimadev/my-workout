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
import androidx.wear.compose.navigation3.SwipeDismissableSceneStrategy
import dagger.hilt.android.AndroidEntryPoint
import digital.tonima.myworkout.wear.ui.WorkoutEffect
import digital.tonima.myworkout.wear.ui.WorkoutExecutionScreen
import digital.tonima.myworkout.wear.ui.WorkoutIntent
import digital.tonima.myworkout.wear.ui.WorkoutListScreen
import digital.tonima.myworkout.wear.ui.WorkoutViewModel
import digital.tonima.myworkout.wear.ui.navigation.Screen
import digital.tonima.myworkout.wear.ui.navigation.Screen.WorkoutExecution
import digital.tonima.myworkout.wear.ui.navigation.Screen.WorkoutList
import digital.tonima.myworkout.wear.ui.theme.WearAppTheme

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

    WearAppTheme {
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
                    val state by viewModel.state.collectAsState()
                    val onIntent = remember(viewModel) { { intent: WorkoutIntent -> viewModel.onIntent(intent) } }

                    LaunchedEffect(viewModel) {
                        viewModel.effects.collect { effect ->
                            when (effect) {
                                WorkoutEffect.NavigateBack -> {
                                    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                                }
                            }
                        }
                    }

                    when (key) {
                        is WorkoutList -> {
                            WorkoutListScreen(
                                state = state,
                                onWorkoutClick = { workoutId ->
                                    backStack.add(WorkoutExecution(workoutId))
                                },
                            )
                        }
                        is WorkoutExecution -> {
                            LaunchedEffect(key.workoutId) {
                                viewModel.onIntent(WorkoutIntent.LoadWorkout(key.workoutId))
                                viewModel.onIntent(WorkoutIntent.StartSession(key.workoutId))
                            }

                            WorkoutExecutionScreen(
                                state = state,
                                isAmbientMode = isAmbientMode,
                                onIntent = onIntent,
                            )
                        }
                    }
                }
            }
        }
    }
}
