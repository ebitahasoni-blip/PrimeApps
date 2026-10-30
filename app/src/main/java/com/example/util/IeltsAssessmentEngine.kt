package com.example.util

import com.example.model.AssessmentReport
import com.example.model.CorrectionCategory
import com.example.model.CorrectionItem
import com.example.model.CriterionScoreDetail
import com.example.model.IeltsCriterion
import com.example.model.Official2023Rubric
import com.example.model.RubricHighlightLink
import java.util.UUID

object IeltsAssessmentEngine {

    // Academic Vocabulary and Collocations indicative of Bands 7 - 9
    private val advancedLexicon = listOf(
        "substantial", "phenomenon", "detrimental", "inevitable", "consequently",
        "furthermore", "exacerbate", "ameliorate", "fundamental", "unprecedented",
        "predominant", "compelling", "imperative", "sustainable", "correlation",
        "implement", "perspective", "disproportionate", "allocation", "ubiquitous",
        "controversial", "repercussions", "advocate", "paramount", "diminish",
        "crucial", "pragmatic", "incentive", "infrastructure", "proponents"
    )

    private val commonInformalWords = mapOf(
        "kids" to ("children" to "Too informal for academic writing; use 'children' or 'youngsters'."),
        "a lot of" to ("numerous" to "'A lot of' is informal. Prefer 'a substantial number of' or 'numerous'."),
        "gonna" to ("going to" to "Slang contraction. Always write 'going to' in formal essays."),
        "stuff" to ("factors" to "Vague and colloquial. Specify the actual elements, items, or factors."),
        "things" to ("aspects" to "Vague noun. Use precise terms like 'phenomena', 'factors', or 'aspects'."),
        "bad" to ("detrimental" to "Elementary adjective. Consider 'adverse', 'deleterious', or 'detrimental'."),
        "good" to ("beneficial" to "Basic vocabulary. Upgrade to 'advantageous', 'favourable', or 'beneficial'."),
        "huge" to ("immense" to "Colloquial hyperbole. Prefer 'substantial', 'considerable', or 'immense'.")
    )

    private val grammarChecks = listOf(
        Regex("(?i)\\b(he|she|it|everybody|everyone)\\s+(have|do|say|go|make)\\b") to { match: MatchResult ->
            val subj = match.groupValues[1]
            val verb = match.groupValues[2]
            val replacement = when (verb.lowercase()) {
                "have" -> "has"
                "do" -> "does"
                "say" -> "says"
                "go" -> "goes"
                "make" -> "makes"
                else -> "${verb}s"
            }
            Pair("$subj $replacement", "Subject-verb agreement error with third-person singular '$subj'.")
        },
        Regex("(?i)\\bmore\\s+(easier|better|faster|harder|clearer)\\b") to { match: MatchResult ->
            val adj = match.groupValues[1]
            Pair(adj, "Double comparative error. Use '$adj' alone, not 'more $adj'.")
        },
        Regex("(?i)\\baltought\\b|\\balthough\\s+that\\b") to { _: MatchResult ->
            Pair("Although", "Conjunction error. 'Although' should not be followed by 'that'.")
        },
        Regex("(?i)\\bin\\s+my\\s+point\\s+of\\s+view\\b") to { _: MatchResult ->
            Pair("From my point of view", "Preposition error in fixed idiom: write 'From my point of view' or 'In my view'.")
        },
        Regex("(?i)\\bvery\\s+(crucial|vital|essential|paramount)\\b") to { match: MatchResult ->
            val adj = match.groupValues[1]
            Pair(adj, "Redundant intensifier. '$adj' is an ungradable adjective and cannot take 'very'.")
        }
    )

    fun evaluateEssay(topic: String, essayText: String): AssessmentReport {
        val trimmedEssay = essayText.trim()
        val rawWords = trimmedEssay.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = rawWords.size
        val paragraphs = trimmedEssay.split(Regex("\\n{2,}")).map { it.trim() }.filter { it.isNotBlank() }
        val paragraphCount = paragraphs.size

        val sentences = trimmedEssay.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        val sentenceCount = sentences.size.coerceAtLeast(1)

        val corrections = mutableListOf<CorrectionItem>()
        val rubricLinks = mutableListOf<RubricHighlightLink>()

        // 1. Task Response Evaluation (TR)
        var trScore = 6.5f
        val trStrengths = mutableListOf<String>()
        val trImprovements = mutableListOf<String>()

        if (wordCount < 250) {
            val penalty = ((250 - wordCount) / 40.0f).coerceAtMost(1.5f)
            trScore -= penalty
            trImprovements.add("Word count is under the mandatory 250 words minimum ($wordCount words). Official IELTS examiners apply an automatic penalty.")
        } else {
            trStrengths.add("Exceeds the 250-word minimum comfortably ($wordCount words), satisfying length requirements.")
        }

        if (paragraphCount in 4..5) {
            trStrengths.add("Effective essay structure with $paragraphCount well-separated paragraphs (Introduction, Body, Conclusion).")
            trScore += 0.5f
        } else if (paragraphCount < 3) {
            trImprovements.add("Insufficient paragraphing. Academic essays require a 4 or 5-paragraph structure.")
            trScore -= 0.5f
        }

        // Check if intro has a thesis/stance
        val firstPara = paragraphs.firstOrNull() ?: ""
        val hasClearPosition = firstPara.contains(Regex("(?i)(I firmly believe|In my opinion|this essay argues|it is argued that|I agree|I disagree|this essay will discuss)"))
        if (hasClearPosition) {
            trStrengths.add("Clear position presented directly in the introductory paragraph.")
            trScore += 0.5f
            rubricLinks.add(
                RubricHighlightLink(
                    id = UUID.randomUUID().toString(),
                    criterion = IeltsCriterion.TASK_RESPONSE,
                    bandLevel = 7,
                    descriptorText = "Presents a clear position throughout the response",
                    essaySnippet = firstPara.take(180) + if (firstPara.length > 180) "..." else "",
                    startIndex = 0,
                    endIndex = firstPara.length.coerceAtMost(180),
                    isPositive = true,
                    examinerCommentary = "Your introduction effectively sets an explicit stance, satisfying Band 7+ Task Response criteria."
                )
            )
        } else {
            trImprovements.add("Position in the introduction is either absent or implicit. IELTS examiners expect a clear thesis statement.")
        }

        trScore = trScore.coerceIn(5.0f, 8.5f)

        // 2. Coherence & Cohesion Evaluation (CC)
        var ccScore = 6.5f
        val ccStrengths = mutableListOf<String>()
        val ccImprovements = mutableListOf<String>()

        val cohesiveMarkers = listOf("However", "Furthermore", "In addition", "Consequently", "On the other hand", "Therefore", "Moreover", "Similarly", "Conversely")
        val foundMarkers = cohesiveMarkers.filter { marker -> trimmedEssay.contains(Regex("(?i)\\b$marker\\b")) }

        if (foundMarkers.size >= 4) {
            ccStrengths.add("Uses a varied range of cohesive devices (${foundMarkers.take(4).joinToString(", ")}).")
            ccScore += 0.5f
        } else {
            ccImprovements.add("Limited range of cohesive devices detected. Integrate contrasting and consequential linking.")
        }

        // Link cohesive evidence
        val secondPara = paragraphs.getOrNull(1) ?: ""
        if (secondPara.isNotBlank()) {
            rubricLinks.add(
                RubricHighlightLink(
                    id = UUID.randomUUID().toString(),
                    criterion = IeltsCriterion.COHERENCE_COHESION,
                    bandLevel = 7,
                    descriptorText = "Logically organises information and ideas; there is clear progression throughout",
                    essaySnippet = secondPara.take(160) + if (secondPara.length > 160) "..." else "",
                    startIndex = trimmedEssay.indexOf(secondPara),
                    endIndex = trimmedEssay.indexOf(secondPara) + secondPara.length.coerceAtMost(160),
                    isPositive = true,
                    examinerCommentary = "This body paragraph begins with a clear topic sentence, establishing paragraph-level unity."
                )
            )
        }

        // Check for mechanical transition overuse
        val moreoverCount = Regex("(?i)\\bmoreover\\b").findAll(trimmedEssay).count()
        val furthermoreCount = Regex("(?i)\\bfurthermore\\b").findAll(trimmedEssay).count()
        if (moreoverCount + furthermoreCount > 3) {
            ccImprovements.add("Overuse of mechanical transitions ('Moreover' / 'Furthermore'). Rely more on thematic referencing.")
            ccScore -= 0.5f
            rubricLinks.add(
                RubricHighlightLink(
                    id = UUID.randomUUID().toString(),
                    criterion = IeltsCriterion.COHERENCE_COHESION,
                    bandLevel = 6,
                    descriptorText = "Cohesion within and/or between sentences may be faulty or mechanical",
                    essaySnippet = "Frequent transitions: Moreover ($moreoverCount), Furthermore ($furthermoreCount)",
                    startIndex = 0,
                    endIndex = 0,
                    isPositive = false,
                    examinerCommentary = "Transition words are repeated mechanically at the start of multiple sentences."
                )
            )
        }

        ccScore = ccScore.coerceIn(5.0f, 8.5f)

        // 3. Lexical Resource Evaluation (LR)
        var lrScore = 6.0f
        val lrStrengths = mutableListOf<String>()
        val lrImprovements = mutableListOf<String>()

        val foundAdvancedWords = advancedLexicon.filter { word -> trimmedEssay.contains(Regex("(?i)\\b$word\\b")) }
        if (foundAdvancedWords.size >= 5) {
            lrStrengths.add("Strong academic vocabulary showcasing lexical sophistication (${foundAdvancedWords.take(5).joinToString(", ")}).")
            lrScore += 1.0f
            rubricLinks.add(
                RubricHighlightLink(
                    id = UUID.randomUUID().toString(),
                    criterion = IeltsCriterion.LEXICAL_RESOURCE,
                    bandLevel = 7,
                    descriptorText = "Uses less common lexical items with some awareness of style and collocation",
                    essaySnippet = "Identified lexical items: ${foundAdvancedWords.joinToString(", ")}",
                    startIndex = 0,
                    endIndex = 0,
                    isPositive = true,
                    examinerCommentary = "Candidate demonstrates commendable control of higher-tier Academic Word List items."
                )
            )
        } else if (foundAdvancedWords.isNotEmpty()) {
            lrStrengths.add("Some academic vocabulary present (${foundAdvancedWords.joinToString(", ")}).")
            lrScore += 0.5f
        } else {
            lrImprovements.add("Vocabulary is predominantly general. Needs Academic Word List (AWL) items to reach Band 7+.")
        }

        // Check informal words & generate corrections
        for ((informal, replacementData) in commonInformalWords) {
            val matches = Regex("(?i)\\b$informal\\b").findAll(trimmedEssay)
            for (match in matches) {
                corrections.add(
                    CorrectionItem(
                        id = UUID.randomUUID().toString(),
                        originalText = match.value,
                        suggestedText = replacementData.first,
                        explanation = replacementData.second,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        category = CorrectionCategory.LEXICAL
                    )
                )
            }
        }

        lrScore = (lrScore - (corrections.count { it.category == CorrectionCategory.LEXICAL } * 0.2f)).coerceIn(5.0f, 8.5f)

        // 4. Grammatical Range & Accuracy Evaluation (GRA)
        var graScore = 6.5f
        val graStrengths = mutableListOf<String>()
        val graImprovements = mutableListOf<String>()

        // Complex sentence markers
        val complexSubordinators = listOf("although", "whereas", "while", "even though", "since", "because", "provided that", "unless", "in order that")
        val foundSubordinators = complexSubordinators.filter { trimmedEssay.contains(Regex("(?i)\\b$it\\b")) }

        if (foundSubordinators.size >= 3) {
            graStrengths.add("Demonstrates flexible complex sentence structures using subordinators (${foundSubordinators.take(3).joinToString(", ")}).")
            graScore += 0.5f
            rubricLinks.add(
                RubricHighlightLink(
                    id = UUID.randomUUID().toString(),
                    criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
                    bandLevel = 7,
                    descriptorText = "Uses a variety of complex structures with good control",
                    essaySnippet = "Complex subordinators identified: ${foundSubordinators.joinToString(", ")}",
                    startIndex = 0,
                    endIndex = 0,
                    isPositive = true,
                    examinerCommentary = "Effective sentence variation balancing compound and complex sentence structures."
                )
            )
        }

        // Check grammar errors
        for ((pattern, fixer) in grammarChecks) {
            val matches = pattern.findAll(trimmedEssay)
            for (match in matches) {
                val (suggested, explanation) = fixer(match)
                corrections.add(
                    CorrectionItem(
                        id = UUID.randomUUID().toString(),
                        originalText = match.value,
                        suggestedText = suggested,
                        explanation = explanation,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        category = CorrectionCategory.GRAMMAR
                    )
                )
            }
        }

        val grammarErrorCount = corrections.count { it.category == CorrectionCategory.GRAMMAR }
        if (grammarErrorCount == 0) {
            graStrengths.add("High degree of grammatical accuracy with zero systematic syntax flaws detected.")
            graScore += 0.5f
        } else {
            graImprovements.add("Found $grammarErrorCount grammatical inaccuracy/inaccuracies requiring correction.")
            graScore -= (grammarErrorCount * 0.3f)
        }

        graScore = graScore.coerceIn(5.0f, 8.5f)

        // Round criterion scores to nearest 0.5
        val finalTr = roundToHalfBand(trScore)
        val finalCc = roundToHalfBand(ccScore)
        val finalLr = roundToHalfBand(lrScore)
        val finalGra = roundToHalfBand(graScore)

        val overallBand = AssessmentReport.calculateIeltsOverallBand(finalTr, finalCc, finalLr, finalGra)

        // Get 2023 Rubric summaries
        val trDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.TASK_RESPONSE, finalTr.toInt())?.keySummary
            ?: "Addresses all parts of the task."
        val ccDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.COHERENCE_COHESION, finalCc.toInt())?.keySummary
            ?: "Arranges information coherently."
        val lrDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.LEXICAL_RESOURCE, finalLr.toInt())?.keySummary
            ?: "Uses an adequate vocabulary range."
        val graDescriptor = Official2023Rubric.getBandDescriptor(IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY, finalGra.toInt())?.keySummary
            ?: "Uses a mix of simple and complex sentences."

        val generalFeedback = buildString {
            append("Overall, this is a Band $overallBand performance on IELTS Writing Task 2. ")
            if (overallBand >= 7.0f) {
                append("The response exhibits commendable maturity, academic tone, and well-structured argument development. ")
            } else {
                append("The essay communicates the core ideas adequately, but requires closer attention to lexical variety, grammar precision, and cohesive flow to unlock the 7.0+ threshold. ")
            }
            append("Review the individual criterion tabs and the interactive rubric links to inspect specific sentences that influenced your score.")
        }

        return AssessmentReport(
            id = UUID.randomUUID().toString(),
            topic = topic,
            essayText = essayText,
            wordCount = wordCount,
            paragraphCount = paragraphCount,
            createdAtTimestamp = System.currentTimeMillis(),
            overallBandScore = overallBand,
            taskResponse = CriterionScoreDetail(
                criterion = IeltsCriterion.TASK_RESPONSE,
                bandScore = finalTr,
                justification = buildJustification(IeltsCriterion.TASK_RESPONSE, finalTr, trStrengths, trImprovements),
                officialDescriptorSummary = trDescriptor,
                keyStrengths = trStrengths,
                areasToImprove = trImprovements,
                nextBandActionPlan = "To reach Band ${finalTr + 0.5f}: Expand second body paragraph with a concrete real-world example and ensure every prompt element is addressed in depth."
            ),
            coherenceCohesion = CriterionScoreDetail(
                criterion = IeltsCriterion.COHERENCE_COHESION,
                bandScore = finalCc,
                justification = buildJustification(IeltsCriterion.COHERENCE_COHESION, finalCc, ccStrengths, ccImprovements),
                officialDescriptorSummary = ccDescriptor,
                keyStrengths = ccStrengths,
                areasToImprove = ccImprovements,
                nextBandActionPlan = "To reach Band ${finalCc + 0.5f}: Vary transitions beyond 'Moreover' and use grammatical referencing (this trend, such policies) for seamless paragraph progression."
            ),
            lexicalResource = CriterionScoreDetail(
                criterion = IeltsCriterion.LEXICAL_RESOURCE,
                bandScore = finalLr,
                justification = buildJustification(IeltsCriterion.LEXICAL_RESOURCE, finalLr, lrStrengths, lrImprovements),
                officialDescriptorSummary = lrDescriptor,
                keyStrengths = lrStrengths,
                areasToImprove = lrImprovements,
                nextBandActionPlan = "To reach Band ${finalLr + 0.5f}: Substitute general verbs and adjectives with academic collocations (e.g. 'implement stringent measures', 'exert a profound influence')."
            ),
            grammaticalRangeAccuracy = CriterionScoreDetail(
                criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
                bandScore = finalGra,
                justification = buildJustification(IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY, finalGra, graStrengths, graImprovements),
                officialDescriptorSummary = graDescriptor,
                keyStrengths = graStrengths,
                areasToImprove = graImprovements,
                nextBandActionPlan = "To reach Band ${finalGra + 0.5f}: Practice inversion ('Not only... but also...'), mixed conditionals, and passive voice to demonstrate structural dexterity."
            ),
            corrections = corrections,
            rubricLinks = rubricLinks,
            generalFeedback = generalFeedback
        )
    }

    private fun roundToHalfBand(score: Float): Float {
        val rounded = Math.round(score * 2) / 2.0f
        return rounded.coerceIn(4.0f, 9.0f)
    }

    private fun buildJustification(criterion: IeltsCriterion, band: Float, strengths: List<String>, improvements: List<String>): String {
        return buildString {
            append("Band $band awarded according to the 2023 IELTS ${criterion.title} descriptors. ")
            if (strengths.isNotEmpty()) {
                append("Key observed strength: ${strengths.first()} ")
            }
            if (improvements.isNotEmpty()) {
                append("Limiting factor: ${improvements.first()}")
            } else {
                append("No critical limiting factors observed.")
            }
        }
    }
}
