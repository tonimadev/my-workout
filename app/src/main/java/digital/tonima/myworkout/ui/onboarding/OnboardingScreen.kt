package digital.tonima.myworkout.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.R
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        val color =
                            if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = color,
                        ) {}
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                ) {
                    Text(
                        if (pagerState.currentPage < 2) {
                            stringResource(R.string.action_next)
                        } else {
                            stringResource(R.string.action_start_using)
                        },
                    )
                }
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) { page ->
            when (page) {
                0 ->
                    OnboardingPage(
                        title = stringResource(R.string.onboarding_welcome_title),
                        description = stringResource(R.string.onboarding_welcome_desc),
                        icon = Icons.Default.FitnessCenter,
                    )
                1 ->
                    OnboardingPage(
                        title = stringResource(R.string.onboarding_stats_title),
                        description = stringResource(R.string.onboarding_stats_desc),
                        icon = Icons.Default.ShowChart,
                    )
                2 ->
                    OnboardingPage(
                        title = stringResource(R.string.onboarding_wear_title),
                        description = stringResource(R.string.onboarding_wear_desc),
                        icon = Icons.Default.Watch,
                    )
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    icon: ImageVector,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
