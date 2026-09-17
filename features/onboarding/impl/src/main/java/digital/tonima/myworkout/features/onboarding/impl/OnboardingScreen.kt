package digital.tonima.myworkout.features.onboarding.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.tonima.myworkout.data.catalog.WorkoutTemplate
import digital.tonima.myworkout.data.catalog.WorkoutTemplateCatalog
import digital.tonima.myworkout.ui.components.templates.WorkoutTemplateList
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4
private const val TEMPLATES_PAGE = 3

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onApplyTemplates: (Set<WorkoutTemplate>) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    var selectedTemplates by remember { mutableStateOf(emptySet<WorkoutTemplate>()) }

    fun finish() {
        onApplyTemplates(selectedTemplates)
        onComplete()
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(PAGE_COUNT) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier =
                                Modifier
                                    .size(width = if (isSelected) 24.dp else 8.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    ),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pagerState.currentPage == TEMPLATES_PAGE) {
                        TextButton(onClick = ::finish) {
                            Text(stringResource(R.string.action_skip).uppercase(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            if (pagerState.currentPage < PAGE_COUNT - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                finish()
                            }
                        },
                        modifier = Modifier.height(56.dp).width(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text(
                            text =
                                if (pagerState.currentPage < PAGE_COUNT - 1) {
                                    stringResource(R.string.action_next).uppercase()
                                } else {
                                    stringResource(R.string.action_start_using).uppercase()
                                },
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
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
                        color = MaterialTheme.colorScheme.primary,
                    )
                1 ->
                    OnboardingPage(
                        title = stringResource(R.string.onboarding_stats_title),
                        description = stringResource(R.string.onboarding_stats_desc),
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                2 ->
                    OnboardingPage(
                        title = stringResource(R.string.onboarding_wear_title),
                        description = stringResource(R.string.onboarding_wear_desc),
                        icon = Icons.Default.Watch,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                TEMPLATES_PAGE ->
                    OnboardingTemplatesPage(
                        selectedTemplates = selectedTemplates,
                        onToggleTemplate = { template ->
                            selectedTemplates =
                                if (selectedTemplates.contains(template)) {
                                    selectedTemplates - template
                                } else {
                                    selectedTemplates + template
                                }
                        },
                    )
            }
        }
    }
}

@Composable
private fun OnboardingTemplatesPage(
    selectedTemplates: Set<WorkoutTemplate>,
    onToggleTemplate: (WorkoutTemplate) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_templates_title).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_templates_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        WorkoutTemplateList(
            templates = WorkoutTemplateCatalog.all,
            selectedTemplates = selectedTemplates,
            onTemplateClick = onToggleTemplate,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(240.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = color,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier =
                Modifier
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            letterSpacing = (-1).sp,
            lineHeight = 36.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 24.sp,
        )
    }
}
