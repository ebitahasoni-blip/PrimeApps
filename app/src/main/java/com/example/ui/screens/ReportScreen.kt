package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentReport
import com.example.model.BandDescriptor
import com.example.model.CorrectionItem
import com.example.model.IeltsCriterion
import com.example.model.Official2023Rubric
import com.example.model.RubricHighlightLink
import com.example.ui.EssayViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    report: AssessmentReport,
    viewModel: EssayViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val selectedCriterion by viewModel.selectedCriterion.collectAsState()
    val activeDescriptor by viewModel.activeRubricDescriptor.collectAsState()
    val activeLink by viewModel.activeHighlightLink.collectAsState()

    val formattedDate = remember(report.createdAtTimestamp) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(Date(report.createdAtTimestamp))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("report_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Input")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "IELTS Diagnostic Report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Word .docx Export & Share Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.openDocxInWord(context, report) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("export_docx_button")
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Word (.docx)", fontSize = 12.sp)
                }
                IconButton(
                    onClick = { viewModel.shareDocx(context, report) },
                    modifier = Modifier.testTag("share_docx_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Word Report")
                }
            }
        }

        HorizontalDivider()

        // Responsive Body
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 840.dp

            if (isWide) {
                // Wide Screen / Tablet Layout: Left = Graded Essay & Corrections, Right = Official 2023 Rubric
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column
                    Box(modifier = Modifier.weight(1.15f)) {
                        ReportLeftPane(
                            report = report,
                            activeLink = activeLink,
                            onLinkSelect = { viewModel.selectHighlightLink(it) },
                            onOpenDocx = { viewModel.openDocxInWord(context, report) }
                        )
                    }

                    // Right Column: Official 2023 Rubric Side-by-Side Pane
                    Box(modifier = Modifier.weight(0.85f)) {
                        RubricSideBySidePane(
                            selectedCriterion = selectedCriterion,
                            activeDescriptor = activeDescriptor,
                            activeLink = activeLink,
                            onCriterionSelect = { viewModel.selectCriterion(it) },
                            onDescriptorSelect = { viewModel.selectRubricDescriptor(it) }
                        )
                    }
                }
            } else {
                // Mobile Layout: Tabs for [Essay & Feedback], [Side-by-Side Rubric], [Official Table]
                var mobileSubTab by remember { mutableStateOf(0) }

                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = mobileSubTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = mobileSubTab == 0,
                            onClick = { mobileSubTab = 0 },
                            text = { Text("Essay & Markup", fontSize = 13.sp) }
                        )
                        Tab(
                            selected = mobileSubTab == 1,
                            onClick = { mobileSubTab = 1 },
                            text = { Text("2023 Rubric Link", fontSize = 13.sp) }
                        )
                        Tab(
                            selected = mobileSubTab == 2,
                            onClick = { mobileSubTab = 2 },
                            text = { Text("Table & Scores", fontSize = 13.sp) }
                        )
                    }

                    when (mobileSubTab) {
                        0 -> {
                            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                ReportLeftPane(
                                    report = report,
                                    activeLink = activeLink,
                                    onLinkSelect = {
                                        viewModel.selectHighlightLink(it)
                                        mobileSubTab = 1 // Switch to rubric view
                                    },
                                    onOpenDocx = { viewModel.openDocxInWord(context, report) }
                                )
                            }
                        }
                        1 -> {
                            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                RubricSideBySidePane(
                                    selectedCriterion = selectedCriterion,
                                    activeDescriptor = activeDescriptor,
                                    activeLink = activeLink,
                                    onCriterionSelect = { viewModel.selectCriterion(it) },
                                    onDescriptorSelect = { viewModel.selectRubricDescriptor(it) }
                                )
                            }
                        }
                        2 -> {
                            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                TableAndSynthesisView(report = report)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportLeftPane(
    report: AssessmentReport,
    activeLink: RubricHighlightLink?,
    onLinkSelect: (RubricHighlightLink) -> Unit,
    onOpenDocx: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Summary Band Score Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OVERALL BAND ESTIMATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Band ${report.overallBandScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${report.wordCount} words • ${report.paragraphCount} paragraphs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 4 Sub-score Chips
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SubScoreBadge("TR", report.taskResponse.bandScore)
                        SubScoreBadge("CC", report.coherenceCohesion.bandScore)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SubScoreBadge("LR", report.lexicalResource.bandScore)
                        SubScoreBadge("GRA", report.grammaticalRangeAccuracy.bandScore)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Topic Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "TASK TOPIC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.topic,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Rubric Evidence Links (Tapping links to rubric descriptors)
        if (report.rubricLinks.isNotEmpty()) {
            Text(
                text = "Interactive Rubric Evidence Links:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any item below to highlight its exact descriptor in the 2023 rubric panel:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            report.rubricLinks.forEach { link ->
                val isSelected = activeLink?.id == link.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onLinkSelect(link) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (link.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (link.isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${link.criterion.title} • Band ${link.bandLevel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "\"${link.descriptorText}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = link.examinerCommentary,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Essay with Inline Annotations & Correction Markup
        Text(
            text = "Essay with Correction Markup",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Red strikethrough denotes errors; bold green denotes examiner corrections.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val paragraphs = report.essayText.split("\n\n").filter { it.isNotBlank() }
                paragraphs.forEachIndexed { pIndex, para ->
                    AnnotatedEssayParagraph(paragraph = para, corrections = report.corrections)
                    if (pIndex < paragraphs.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Corrections & Explanatory Comments
        if (report.corrections.isNotEmpty()) {
            Text(
                text = "Detailed Corrections & Explanations (${report.corrections.size}):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            report.corrections.forEach { corr ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = corr.category.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = corr.originalText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text("  ➔  ", fontWeight = FontWeight.Bold)
                            Text(
                                text = corr.suggestedText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = corr.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Export prompt
        Button(
            onClick = onOpenDocx,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Full Word (.docx) Report")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AnnotatedEssayParagraph(paragraph: String, corrections: List<CorrectionItem>) {
    val relevant = corrections.filter { paragraph.contains(it.originalText) }

    val annotated = buildAnnotatedString {
        if (relevant.isEmpty()) {
            append(paragraph)
        } else {
            var currentIdx = 0
            val sorted = relevant.sortedBy { paragraph.indexOf(it.originalText) }
            for (c in sorted) {
                val start = paragraph.indexOf(c.originalText, currentIdx)
                if (start == -1) continue

                if (start > currentIdx) {
                    append(paragraph.substring(currentIdx, start))
                }

                // Error text in strikethrough red
                withStyle(
                    SpanStyle(
                        color = Color(0xFFDC2626),
                        textDecoration = TextDecoration.LineThrough,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(c.originalText)
                }

                // Corrected text in bold green
                withStyle(
                    SpanStyle(
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(" [${c.suggestedText}] ")
                }

                currentIdx = start + c.originalText.length
            }

            if (currentIdx < paragraph.length) {
                append(paragraph.substring(currentIdx))
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RubricSideBySidePane(
    selectedCriterion: IeltsCriterion,
    activeDescriptor: BandDescriptor?,
    activeLink: RubricHighlightLink?,
    onCriterionSelect: (IeltsCriterion) -> Unit,
    onDescriptorSelect: (BandDescriptor) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Official 2023 IELTS Band Descriptors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tap a descriptor to see how the student's essay correlates with this band level:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Criteria Selector Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IeltsCriterion.values().forEach { criterion ->
                    FilterChip(
                        selected = selectedCriterion == criterion,
                        onClick = { onCriterionSelect(criterion) },
                        label = { Text(criterion.code, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Notification Bar if a rubric link is active
            activeLink?.let { link ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Matched Essay Evidence (${link.criterion.title}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = link.essaySnippet,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Examiner Insight: ${link.examinerCommentary}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Descriptors List for this criterion
            val descriptors = Official2023Rubric.getDescriptorsFor(selectedCriterion)
            descriptors.forEach { bd ->
                val isSelected = activeDescriptor?.band == bd.band && activeDescriptor?.criterion == bd.criterion
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onDescriptorSelect(bd) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Band ${bd.band}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isSelected) {
                                Text(
                                    text = "ACTIVE FOCUS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bd.keySummary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        bd.descriptors.forEach { bullet ->
                            Text(
                                text = "• $bullet",
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

@Composable
fun TableAndSynthesisView(report: AssessmentReport) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Official Band Score Assessment Table",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Structured criterion explanations as formatted in your Word (.docx) export:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        val rows = listOf(
            report.taskResponse,
            report.coherenceCohesion,
            report.lexicalResource,
            report.grammaticalRangeAccuracy
        )

        rows.forEach { detail ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detail.criterion.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Band ${detail.bandScore}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Official Descriptors Alignment: ${detail.officialDescriptorSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detail.justification,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Next Band Target: ${detail.nextBandActionPlan}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Examiner Synthesis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Overall Examiner Synthesis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = report.generalFeedback,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SubScoreBadge(title: String, score: Float) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title: ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
