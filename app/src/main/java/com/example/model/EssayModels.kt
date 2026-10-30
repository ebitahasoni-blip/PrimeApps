package com.example.model

enum class CorrectionCategory(val label: String) {
    GRAMMAR("Grammar & Syntax"),
    LEXICAL("Lexical & Collocation"),
    COHERENCE("Cohesion & Flow"),
    TASK_RESPONSE("Task Response"),
    PUNCTUATION("Punctuation")
}

data class CorrectionItem(
    val id: String,
    val originalText: String,
    val suggestedText: String,
    val explanation: String,
    val startIndex: Int,
    val endIndex: Int,
    val category: CorrectionCategory,
    val rubricDescriptorId: String? = null
)

data class RubricHighlightLink(
    val id: String,
    val criterion: IeltsCriterion,
    val bandLevel: Int,
    val descriptorText: String,
    val essaySnippet: String,
    val startIndex: Int,
    val endIndex: Int,
    val isPositive: Boolean, // true = good evidence, false = error/area for improvement
    val examinerCommentary: String
)

data class CriterionScoreDetail(
    val criterion: IeltsCriterion,
    val bandScore: Float,
    val justification: String,
    val officialDescriptorSummary: String,
    val keyStrengths: List<String>,
    val areasToImprove: List<String>,
    val nextBandActionPlan: String
)

data class AssessmentReport(
    val id: String,
    val topic: String,
    val essayText: String,
    val wordCount: Int,
    val paragraphCount: Int,
    val createdAtTimestamp: Long,
    val overallBandScore: Float,
    val taskResponse: CriterionScoreDetail,
    val coherenceCohesion: CriterionScoreDetail,
    val lexicalResource: CriterionScoreDetail,
    val grammaticalRangeAccuracy: CriterionScoreDetail,
    val corrections: List<CorrectionItem>,
    val rubricLinks: List<RubricHighlightLink>,
    val generalFeedback: String,
    val isSynced: Boolean = false,
    val docxFilePath: String? = null
) {
    companion object {
        /**
         * Official IELTS overall band score calculation:
         * Arithmetic mean of the 4 criteria, rounded to nearest half or whole band:
         * If the average ends in .25, it rounds UP to the next half band (e.g. 6.25 -> 6.5).
         * If it ends in .75, it rounds UP to the next whole band (e.g. 6.75 -> 7.0).
         * If it ends in .125, it rounds down to whole band (e.g. 6.125 -> 6.0).
         * If it ends in .375 or .625, standard half-band rounding applies.
         */
        fun calculateIeltsOverallBand(tr: Float, cc: Float, lr: Float, gra: Float): Float {
            val average = (tr + cc + lr + gra) / 4.0f
            val whole = average.toInt()
            val fractional = average - whole
            return when {
                fractional < 0.25f -> whole.toFloat()
                fractional < 0.75f -> whole + 0.5f
                else -> (whole + 1).toFloat()
            }
        }
    }
}
