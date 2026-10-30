package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EssayRepository
import com.example.model.IeltsCriterion
import com.example.model.Official2023Rubric
import com.example.ui.EssayViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputScreen(
    viewModel: EssayViewModel,
    modifier: Modifier = Modifier
) {
    val topic by viewModel.topicInput.collectAsState()
    val essay by viewModel.essayInput.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisStep by viewModel.analysisStep.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    val rawWords = remember(essay) {
        essay.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    val wordCount = rawWords.size
    val paragraphs = remember(essay) {
        essay.trim().split(Regex("\\n{2,}")).filter { it.isNotBlank() }
    }
    val paragraphCount = paragraphs.size

    if (isAnalyzing) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Assessing IELTS Task 2 Essay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = analysisStep,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Applying 2023 Official Band Descriptors...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 768.dp

        if (isWideScreen) {
            // Tablet / Desktop side-by-side comparative layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: User Input
                Box(modifier = Modifier.weight(1.1f)) {
                    InputForm(
                        topic = topic,
                        essay = essay,
                        wordCount = wordCount,
                        paragraphCount = paragraphCount,
                        onTopicChange = { viewModel.updateTopic(it) },
                        onEssayChange = { viewModel.updateEssay(it) },
                        onPasteEssay = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                viewModel.updateEssay(clip)
                            }
                        },
                        onClear = { viewModel.clearInput() },
                        onLoadSample = { viewModel.loadSample() },
                        onSubmit = { viewModel.analyzeAndGrade() }
                    )
                }

                // Right Column: Side-by-side Live Rubric Scoring Preview Pane
                Box(modifier = Modifier.weight(0.9f)) {
                    RubricComparativePreviewPane(
                        wordCount = wordCount,
                        paragraphCount = paragraphCount,
                        essay = essay,
                        topic = topic
                    )
                }
            }
        } else {
            // Mobile: Tabbed or compact view with Quick Rubric toggle
            var mobileTab by remember { mutableStateOf(0) }

            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = mobileTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = mobileTab == 0,
                        onClick = { mobileTab = 0 },
                        text = { Text("Essay Input") },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
                    )
                    Tab(
                        selected = mobileTab == 1,
                        onClick = { mobileTab = 1 },
                        text = { Text("2023 Rubric Criteria") },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
                    )
                }

                if (mobileTab == 0) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        InputForm(
                            topic = topic,
                            essay = essay,
                            wordCount = wordCount,
                            paragraphCount = paragraphCount,
                            onTopicChange = { viewModel.updateTopic(it) },
                            onEssayChange = { viewModel.updateEssay(it) },
                            onPasteEssay = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    viewModel.updateEssay(clip)
                                }
                            },
                            onClear = { viewModel.clearInput() },
                            onLoadSample = { viewModel.loadSample() },
                            onSubmit = { viewModel.analyzeAndGrade() }
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        RubricComparativePreviewPane(
                            wordCount = wordCount,
                            paragraphCount = paragraphCount,
                            essay = essay,
                            topic = topic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputForm(
    topic: String,
    essay: String,
    wordCount: Int,
    paragraphCount: Int,
    onTopicChange: (String) -> Unit,
    onEssayChange: (String) -> Unit,
    onPasteEssay: () -> Unit,
    onClear: () -> Unit,
    onLoadSample: () -> Unit,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IELTS Writing Task 2",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onLoadSample,
                    modifier = Modifier.testTag("load_sample_button")
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sample", fontSize = 12.sp)
                }
                if (topic.isNotBlank() || essay.isNotBlank()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.testTag("clear_input_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear fields")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Topic Text Area
        OutlinedTextField(
            value = topic,
            onValueChange = onTopicChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("topic_input_field"),
            label = { Text("Task 2 Topic / Prompt") },
            placeholder = { Text("Paste or type the official IELTS Writing prompt here...") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Essay Header & Quick Paste Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Candidate Essay",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onPasteEssay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.testTag("paste_essay_button")
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paste Essay", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Essay Text Area
        OutlinedTextField(
            value = essay,
            onValueChange = onEssayChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("essay_input_field"),
            label = { Text("Essay Response (Min 250 words)") },
            placeholder = { Text("Paste your completed Task 2 essay here for instant 2023 band descriptor feedback...") },
            minLines = 8,
            maxLines = 14,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time Metrics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (wordCount >= 250) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$wordCount words",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (wordCount >= 250) "(Valid Length)" else "(Under 250 words: -0.5 penalty)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (wordCount >= 250) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "$paragraphCount paragraphs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Submit Button
        Button(
            onClick = onSubmit,
            enabled = topic.isNotBlank() && essay.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_essay_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Analyze & Grade Essay (2023 Rubric)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RubricComparativePreviewPane(
    wordCount: Int,
    paragraphCount: Int,
    essay: String,
    topic: String,
    modifier: Modifier = Modifier
) {
    var selectedCriterion by remember { mutableStateOf(IeltsCriterion.TASK_RESPONSE) }
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Official 2023 Rubric Comparative Analysis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Real-time evaluation against Cambridge/IDP 2023 standards",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Criteria Selector Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IeltsCriterion.values().forEach { criterion ->
                    FilterChip(
                        selected = selectedCriterion == criterion,
                        onClick = { selectedCriterion = criterion },
                        label = { Text(criterion.code, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live comparative assessment card for current text
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Current Draft Audit: ${selectedCriterion.title}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    when (selectedCriterion) {
                        IeltsCriterion.TASK_RESPONSE -> {
                            val wordStatus = if (wordCount >= 250) "Meets Band 7+ length requirement" else "Underlength (<250 words): automatic TR penalty"
                            val paraStatus = if (paragraphCount in 4..5) "Structure aligns with 4-5 paragraph academic standard" else "Structure needs 4 distinct paragraphs (Intro, 2 Bodies, Conclusion)"
                            Text("• Word Count: $wordCount words ($wordStatus)", style = MaterialTheme.typography.bodySmall)
                            Text("• Layout: $paragraphCount paragraphs ($paraStatus)", style = MaterialTheme.typography.bodySmall)
                        }
                        IeltsCriterion.COHERENCE_COHESION -> {
                            val hasTrans = essay.contains(Regex("(?i)\\b(However|Furthermore|Therefore|Consequently)\\b"))
                            Text("• Paragraphing: ${if (paragraphCount >= 4) "Well-segmented" else "Lacks clear paragraph separation"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Cohesion: ${if (hasTrans) "Transitional markers detected" else "Add linking devices for progression"}", style = MaterialTheme.typography.bodySmall)
                        }
                        IeltsCriterion.LEXICAL_RESOURCE -> {
                            val sampleCheck = essay.contains(Regex("(?i)\\b(substantial|crucial|phenomenon|imperative)\\b"))
                            Text("• Academic Register: ${if (sampleCheck) "Academic Word List vocabulary detected" else "Standard vocabulary. Upgrade general words"}", style = MaterialTheme.typography.bodySmall)
                        }
                        IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY -> {
                            val complexCheck = essay.contains(Regex("(?i)\\b(although|whereas|while|because|if)\\b"))
                            Text("• Sentence Variety: ${if (complexCheck) "Subordinate complex structures detected" else "Simple sentences predominate; add complex clauses"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Official Descriptors (2023 Edition):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Official Band Descriptors list for selected criterion
            val descriptors = Official2023Rubric.getDescriptorsFor(selectedCriterion)
            descriptors.forEach { bd ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Band ${bd.band}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bd.keySummary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        bd.descriptors.forEach { desc ->
                            Text(
                                text = "• $desc",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
