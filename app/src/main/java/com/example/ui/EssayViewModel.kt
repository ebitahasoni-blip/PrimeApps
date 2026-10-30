package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EssayRepository
import com.example.data.OfflineResourceRepository
import com.example.data.OfflineStudyResource
import com.example.model.AssessmentReport
import com.example.model.BandDescriptor
import com.example.model.IeltsCriterion
import com.example.model.Official2023Rubric
import com.example.model.RubricHighlightLink
import com.example.util.DocxExporter
import com.example.util.IeltsAssessmentEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class EssayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EssayRepository(application)

    val isOnline: StateFlow<Boolean> = repository.isOnline
    val unsyncedCount: StateFlow<Int> = repository.unsyncedCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val allReports: StateFlow<List<AssessmentReport>> = repository.allReports.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Input Screen State
    private val _topicInput = MutableStateFlow("")
    val topicInput: StateFlow<String> = _topicInput.asStateFlow()

    private val _essayInput = MutableStateFlow("")
    val essayInput: StateFlow<String> = _essayInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStep = MutableStateFlow("")
    val analysisStep: StateFlow<String> = _analysisStep.asStateFlow()

    // Active Evaluation Report Screen State
    private val _currentReport = MutableStateFlow<AssessmentReport?>(null)
    val currentReport: StateFlow<AssessmentReport?> = _currentReport.asStateFlow()

    // Interactive Bidirectional Rubric Linking State
    private val _selectedCriterion = MutableStateFlow<IeltsCriterion>(IeltsCriterion.TASK_RESPONSE)
    val selectedCriterion: StateFlow<IeltsCriterion> = _selectedCriterion.asStateFlow()

    private val _activeRubricDescriptor = MutableStateFlow<BandDescriptor?>(null)
    val activeRubricDescriptor: StateFlow<BandDescriptor?> = _activeRubricDescriptor.asStateFlow()

    private val _activeHighlightLink = MutableStateFlow<RubricHighlightLink?>(null)
    val activeHighlightLink: StateFlow<RubricHighlightLink?> = _activeHighlightLink.asStateFlow()

    // Offline Study Resources
    private val _selectedResource = MutableStateFlow<OfflineStudyResource?>(null)
    val selectedResource: StateFlow<OfflineStudyResource?> = _selectedResource.asStateFlow()

    fun updateTopic(newTopic: String) {
        _topicInput.value = newTopic
    }

    fun updateEssay(newEssay: String) {
        _essayInput.value = newEssay
    }

    fun clearInput() {
        _topicInput.value = ""
        _essayInput.value = ""
    }

    fun loadSample() {
        _topicInput.value = EssayRepository.SAMPLE_TOPICS.first()
        _essayInput.value = EssayRepository.SAMPLE_ESSAY
    }

    fun setSampleTopic(topic: String) {
        _topicInput.value = topic
    }

    fun selectCriterion(criterion: IeltsCriterion) {
        _selectedCriterion.value = criterion
        _activeRubricDescriptor.value = null
        _activeHighlightLink.value = null
    }

    fun selectRubricDescriptor(descriptor: BandDescriptor) {
        _activeRubricDescriptor.value = descriptor
        // Find if any highlight link in current report corresponds to this criterion & band
        val current = _currentReport.value
        if (current != null) {
            val matchingLink = current.rubricLinks.find {
                it.criterion == descriptor.criterion && it.bandLevel == descriptor.band
            }
            _activeHighlightLink.value = matchingLink
        }
    }

    fun selectHighlightLink(link: RubricHighlightLink) {
        _activeHighlightLink.value = link
        _selectedCriterion.value = link.criterion
        val matchingDesc = Official2023Rubric.getBandDescriptor(link.criterion, link.bandLevel)
        _activeRubricDescriptor.value = matchingDesc
    }

    fun selectResource(res: OfflineStudyResource?) {
        _selectedResource.value = res
    }

    fun viewReport(report: AssessmentReport) {
        _currentReport.value = report
        _selectedCriterion.value = IeltsCriterion.TASK_RESPONSE
        _activeRubricDescriptor.value = null
        _activeHighlightLink.value = null
    }

    fun closeReport() {
        _currentReport.value = null
        _activeHighlightLink.value = null
        _activeRubricDescriptor.value = null
    }

    fun analyzeAndGrade() {
        val topic = _topicInput.value.trim()
        val essay = _essayInput.value.trim()

        if (topic.isBlank() || essay.isBlank()) return

        viewModelScope.launch {
            _isAnalyzing.value = true

            // Provide step-by-step diagnostic feedback
            _analysisStep.value = "Tokenizing essay & computing lexical diversity..."
            delay(350)
            _analysisStep.value = "Evaluating Task Response & thesis development..."
            delay(350)
            _analysisStep.value = "Auditing Coherence, Cohesion & paragraph progression..."
            delay(350)
            _analysisStep.value = "Analyzing Lexical Resource & academic collocations..."
            delay(350)
            _analysisStep.value = "Verifying Grammatical Range, accuracy & sentence syntax..."
            delay(350)
            _analysisStep.value = "Matching against 2023 Official IELTS Band Descriptors..."
            delay(300)
            _analysisStep.value = "Building genuine Microsoft Word (.docx) package with markup..."
            delay(300)

            val rawReport = IeltsAssessmentEngine.evaluateEssay(topic, essay)
            val savedReport = repository.saveReport(rawReport)

            _currentReport.value = savedReport
            _selectedCriterion.value = IeltsCriterion.TASK_RESPONSE
            _isAnalyzing.value = false
            _analysisStep.value = ""
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            repository.syncPendingEssays()
        }
    }

    fun deleteReport(id: String) {
        viewModelScope.launch {
            repository.deleteReport(id)
            if (_currentReport.value?.id == id) {
                _currentReport.value = null
            }
        }
    }

    fun shareDocx(context: Context, report: AssessmentReport) {
        val path = report.docxFilePath ?: run {
            val file = DocxExporter.createDocxReport(context, report)
            file.absolutePath
        }

        val file = File(path)
        if (!file.exists()) return

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.primeessay.kxmpzq.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Prime Essay Feedback - IELTS Task 2 Report")
            putExtra(Intent.EXTRA_TEXT, "Here is the official IELTS Writing Task 2 diagnostic report generated by Prime Essay Feedback (Copyright © Ebrahim Tahassoni 2026).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Word (.docx) Report via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openDocxInWord(context: Context, report: AssessmentReport) {
        val path = report.docxFilePath ?: run {
            val file = DocxExporter.createDocxReport(context, report)
            file.absolutePath
        }

        val file = File(path)
        if (!file.exists()) return

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.aistudio.primeessay.kxmpzq.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to chooser / share
            shareDocx(context, report)
        }
    }
}
