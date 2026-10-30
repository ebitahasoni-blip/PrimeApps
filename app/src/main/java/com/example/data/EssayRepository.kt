package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.EssayDao
import com.example.data.local.EssayDatabase
import com.example.data.local.EssayEntity
import com.example.model.AssessmentReport
import com.example.model.CorrectionCategory
import com.example.model.CorrectionItem
import com.example.model.CriterionScoreDetail
import com.example.model.IeltsCriterion
import com.example.model.Official2023Rubric
import com.example.model.RubricHighlightLink
import com.example.util.DocxExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class EssayRepository(private val context: Context) {

    private val essayDao: EssayDao = EssayDatabase.getDatabase(context).essayDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        monitorNetworkConnectivity()
    }

    private fun checkInitialConnectivity(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun monitorNetworkConnectivity() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val builder = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        cm.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                // Auto sync offline queue
                scope.launch {
                    syncPendingEssays()
                }
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        })
    }

    val allReports: Flow<List<AssessmentReport>> = essayDao.getAllEssays().map { list ->
        list.map { entityToReport(it) }
    }

    val unsyncedCount: Flow<Int> = essayDao.getUnsyncedCount()

    suspend fun saveReport(report: AssessmentReport): AssessmentReport {
        // Generate Word file automatically and save path
        val docxFile = try {
            DocxExporter.createDocxReport(context, report)
        } catch (e: Exception) {
            null
        }

        val updatedReport = report.copy(
            docxFilePath = docxFile?.absolutePath,
            isSynced = _isOnline.value
        )

        essayDao.insertEssay(reportToEntity(updatedReport))
        return updatedReport
    }

    suspend fun syncPendingEssays() {
        if (_isOnline.value) {
            essayDao.markAllAsSynced()
        }
    }

    suspend fun deleteReport(id: String) {
        essayDao.deleteEssay(id)
    }

    suspend fun getReportById(id: String): AssessmentReport? {
        val entity = essayDao.getEssayById(id) ?: return null
        return entityToReport(entity)
    }

    private fun reportToEntity(report: AssessmentReport): EssayEntity {
        val corrJsonArray = JSONArray()
        for (c in report.corrections) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("originalText", c.originalText)
            obj.put("suggestedText", c.suggestedText)
            obj.put("explanation", c.explanation)
            obj.put("startIndex", c.startIndex)
            obj.put("endIndex", c.endIndex)
            obj.put("category", c.category.name)
            corrJsonArray.put(obj)
        }

        val rubricJsonArray = JSONArray()
        for (r in report.rubricLinks) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("criterion", r.criterion.name)
            obj.put("bandLevel", r.bandLevel)
            obj.put("descriptorText", r.descriptorText)
            obj.put("essaySnippet", r.essaySnippet)
            obj.put("startIndex", r.startIndex)
            obj.put("endIndex", r.endIndex)
            obj.put("isPositive", r.isPositive)
            obj.put("examinerCommentary", r.examinerCommentary)
            rubricJsonArray.put(obj)
        }

        return EssayEntity(
            id = report.id,
            topic = report.topic,
            essayText = report.essayText,
            wordCount = report.wordCount,
            paragraphCount = report.paragraphCount,
            createdAtTimestamp = report.createdAtTimestamp,
            overallBandScore = report.overallBandScore,
            taskResponseBand = report.taskResponse.bandScore,
            coherenceBand = report.coherenceCohesion.bandScore,
            lexicalBand = report.lexicalResource.bandScore,
            grammarBand = report.grammaticalRangeAccuracy.bandScore,
            trJustification = report.taskResponse.justification,
            ccJustification = report.coherenceCohesion.justification,
            lrJustification = report.lexicalResource.justification,
            graJustification = report.grammaticalRangeAccuracy.justification,
            generalFeedback = report.generalFeedback,
            correctionsJson = corrJsonArray.toString(),
            rubricLinksJson = rubricJsonArray.toString(),
            isSynced = report.isSynced,
            docxFilePath = report.docxFilePath
        )
    }

    private fun entityToReport(entity: EssayEntity): AssessmentReport {
        val corrections = mutableListOf<CorrectionItem>()
        if (entity.correctionsJson.isNotBlank()) {
            try {
                val array = JSONArray(entity.correctionsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    corrections.add(
                        CorrectionItem(
                            id = obj.optString("id"),
                            originalText = obj.optString("originalText"),
                            suggestedText = obj.optString("suggestedText"),
                            explanation = obj.optString("explanation"),
                            startIndex = obj.optInt("startIndex"),
                            endIndex = obj.optInt("endIndex"),
                            category = try {
                                CorrectionCategory.valueOf(obj.optString("category"))
                            } catch (_: Exception) {
                                CorrectionCategory.GRAMMAR
                            }
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        val rubricLinks = mutableListOf<RubricHighlightLink>()
        if (entity.rubricLinksJson.isNotBlank()) {
            try {
                val array = JSONArray(entity.rubricLinksJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    rubricLinks.add(
                        RubricHighlightLink(
                            id = obj.optString("id"),
                            criterion = try {
                                IeltsCriterion.valueOf(obj.optString("criterion"))
                            } catch (_: Exception) {
                                IeltsCriterion.TASK_RESPONSE
                            },
                            bandLevel = obj.optInt("bandLevel", 7),
                            descriptorText = obj.optString("descriptorText"),
                            essaySnippet = obj.optString("essaySnippet"),
                            startIndex = obj.optInt("startIndex"),
                            endIndex = obj.optInt("endIndex"),
                            isPositive = obj.optBoolean("isPositive", true),
                            examinerCommentary = obj.optString("examinerCommentary")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        val trDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.TASK_RESPONSE, entity.taskResponseBand.toInt())?.keySummary ?: ""
        val ccDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.COHERENCE_COHESION, entity.coherenceBand.toInt())?.keySummary ?: ""
        val lrDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.LEXICAL_RESOURCE, entity.lexicalBand.toInt())?.keySummary ?: ""
        val graDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY, entity.grammarBand.toInt())?.keySummary ?: ""

        return AssessmentReport(
            id = entity.id,
            topic = entity.topic,
            essayText = entity.essayText,
            wordCount = entity.wordCount,
            paragraphCount = entity.paragraphCount,
            createdAtTimestamp = entity.createdAtTimestamp,
            overallBandScore = entity.overallBandScore,
            taskResponse = CriterionScoreDetail(
                criterion = IeltsCriterion.TASK_RESPONSE,
                bandScore = entity.taskResponseBand,
                justification = entity.trJustification,
                officialDescriptorSummary = trDescriptor,
                keyStrengths = listOf("Formal layout and length requirements addressed."),
                areasToImprove = listOf("Ensure supporting ideas are expanded with real-world examples."),
                nextBandActionPlan = "Develop body paragraphs more fully with specific examples."
            ),
            coherenceCohesion = CriterionScoreDetail(
                criterion = IeltsCriterion.COHERENCE_COHESION,
                bandScore = entity.coherenceBand,
                justification = entity.ccJustification,
                officialDescriptorSummary = ccDescriptor,
                keyStrengths = listOf("Clear division between introduction, body, and conclusion."),
                areasToImprove = listOf("Avoid repetitive transitional words."),
                nextBandActionPlan = "Employ thematic referencing and pronoun substitution."
            ),
            lexicalResource = CriterionScoreDetail(
                criterion = IeltsCriterion.LEXICAL_RESOURCE,
                bandScore = entity.lexicalBand,
                justification = entity.lrJustification,
                officialDescriptorSummary = lrDescriptor,
                keyStrengths = listOf("Sufficient vocabulary for the academic task."),
                areasToImprove = listOf("Replace common words with precise academic collocations."),
                nextBandActionPlan = "Integrate Academic Word List (AWL) terms naturally."
            ),
            grammaticalRangeAccuracy = CriterionScoreDetail(
                criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
                bandScore = entity.grammarBand,
                justification = entity.graJustification,
                officialDescriptorSummary = graDescriptor,
                keyStrengths = listOf("Mix of simple and compound sentences."),
                areasToImprove = listOf("Carefully proofread for subject-verb agreement."),
                nextBandActionPlan = "Demonstrate conditional clauses and inversion structures."
            ),
            corrections = corrections,
            rubricLinks = rubricLinks,
            generalFeedback = entity.generalFeedback,
            isSynced = entity.isSynced,
            docxFilePath = entity.docxFilePath
        )
    }

    companion object {
        val SAMPLE_TOPICS = listOf(
            "Some people believe that artificial intelligence will transform higher education positively, while others argue it poses serious risks to critical thinking. Discuss both views and give your opinion.",
            "In many modern cities, traffic congestion is reaching alarming levels. What are the primary causes of this phenomenon, and what pragmatic measures can municipal authorities adopt to resolve it?",
            "With the widespread adoption of remote working, some corporations have closed physical office spaces completely. Do the advantages of this development outweigh the disadvantages?",
            "Governments should spend more money on public healthcare and education rather than investing in space exploration programs. To what extent do you agree or disagree?"
        )

        val SAMPLE_ESSAY = """
The rapid evolution of artificial intelligence has precipitated intense debate regarding its role in modern university education. While some commentators argue that automated tools undermine critical thinking, I firmly believe that artificial intelligence acts as an invaluable catalyst for intellectual empowerment when deployed responsibly by academic institutions.

Proponents of integrating automated platforms into academia emphasize the unprecedented access to personalized instruction. Traditional lecture halls frequently fail to accommodate varying learning paces, whereas adaptive algorithms can tailor complex concepts to individual student requirements. A case in point is automated language tutor applications, which provide instantaneous syntax diagnostics, allowing learners to rectify misconceptions before submitting formal dissertations. Furthermore, such tools liberate faculty from repetitive grading tasks, enabling them to dedicate more time to mentorship and advanced research seminars. Consequently, educational productivity is significantly enhanced across multidisciplinary faculties.

Conversely, detractors contend that excessive reliance on generative software may erode analytical diligence among undergraduates. When solutions can be synthesized with minimal effort, students may fail to develop rigorous research methodologies, deep critical inquiry, or independent problem-solving capabilities. In my point of view, this risk is substantial if universities remain passive. However, rather than imposing futile technological bans, institutions should reform assessment frameworks by prioritizing in-person oral defense, handwritten analytical tasks, and live case studies.

In conclusion, although the proliferation of artificial intelligence presents legitimate challenges to academic integrity, its pedagogical benefits are undeniably immense. By implementing balanced regulatory guidelines, higher education institutions can foster critical thinking while fully harnessing the technological advantages of this revolutionary era.
        """.trimIndent()
    }
}
