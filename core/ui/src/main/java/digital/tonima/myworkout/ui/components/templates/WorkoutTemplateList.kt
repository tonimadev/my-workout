package digital.tonima.myworkout.ui.components.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.tonima.myworkout.data.catalog.WorkoutTemplate

/**
 * Presentational list of ready-made [WorkoutTemplate]s. Purely visual so it can be reused from
 * modules that don't depend on each other (e.g. workout and onboarding features).
 *
 * @param selectedTemplates when non-null, each card shows a checkbox reflecting membership in this
 * set (multi-select, used by onboarding); when null, cards are single-tap-to-apply (used by the
 * "use a template" flow on the workout list).
 */
@Composable
fun WorkoutTemplateList(
    templates: List<WorkoutTemplate>,
    onTemplateClick: (WorkoutTemplate) -> Unit,
    modifier: Modifier = Modifier,
    selectedTemplates: Set<WorkoutTemplate>? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        templates.forEach { template ->
            WorkoutTemplateCard(
                template = template,
                isSelected = selectedTemplates?.contains(template),
                onClick = { onTemplateClick(template) },
            )
        }
    }
}

@Composable
private fun WorkoutTemplateCard(
    template: WorkoutTemplate,
    isSelected: Boolean?,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
                if (template.description.isNotBlank()) {
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = template.exercises.joinToString { it.name },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
            }
            if (isSelected != null) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
            }
        }
    }
}
