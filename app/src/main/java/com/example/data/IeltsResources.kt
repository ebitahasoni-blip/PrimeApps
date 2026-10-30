package com.example.data

data class OfflineStudyResource(
    val id: String,
    val title: String,
    val category: String,
    val estimatedReadMinutes: Int,
    val summary: String,
    val contentMarkdown: String
)

object OfflineResourceRepository {

    val resources: List<OfflineStudyResource> = listOf(
        OfflineStudyResource(
            id = "res_structures",
            title = "Task 2 Master Essay Structures",
            category = "Essay Architecture",
            estimatedReadMinutes = 5,
            summary = "The standard 4-paragraph and 5-paragraph structures designed by Ebrahim Tahassoni for all 5 IELTS Task 2 question types.",
            contentMarkdown = """
# Task 2 Master Essay Structures
*By Ebrahim Tahassoni (2026 Edition)*

Achieving Band 7.0+ in Task Response and Coherence & Cohesion requires a predictable, logical progression. 

## 1. Opinion (Agree/Disagree)
- **Introduction**: Background sentence (paraphrase topic) + Clear Thesis Statement stating your explicit view.
- **Body Paragraph 1**: Primary reason supporting your stance + Explanation + Concrete Example.
- **Body Paragraph 2**: Secondary reason or refutation of opposing view + Elaboration + Impact.
- **Conclusion**: Restate your thesis using different lexical items + Final synthesis/recommendation.

## 2. Discussion & Opinion (Discuss both views and give your opinion)
- **Introduction**: Paraphrase both viewpoints + Declare your opinion in the thesis.
- **Body Paragraph 1**: Examine the view you DISAGREE with (explain why proponents believe it).
- **Body Paragraph 2**: Examine the view you AGREE with (explain why it is more compelling).
- **Conclusion**: Summarize both sides and reaffirm your personal perspective.

## 3. Problem & Solution
- **Introduction**: Paraphrase the phenomenon + State that serious causes exist and viable solutions can be implemented.
- **Body Paragraph 1 (Causes)**: Primary root cause + Secondary contributing factor with illustrations.
- **Body Paragraph 2 (Solutions)**: Concrete, pragmatic measures matching the identified causes.
- **Conclusion**: Summarize that joint action is needed to mitigate the challenge.
            """.trimIndent()
        ),
        OfflineStudyResource(
            id = "res_descriptors_2023",
            title = "Official 2023 Band Descriptors Explained",
            category = "Official Rubric",
            estimatedReadMinutes = 7,
            summary = "Comprehensive breakdown of the updated 2023 IELTS Writing Task 2 descriptors and how examiners score each criterion.",
            contentMarkdown = """
# Official 2023 IELTS Band Descriptors Guide
*Annotated by Ebrahim Tahassoni*

In 2023, the IELTS partners updated the official band descriptors to provide greater clarity on paragraph progression, lexical precision, and complex grammatical control.

## 1. Task Response (TR)
- **Band 9**: Fully addresses all parts; fully developed position; nuanced and perceptive ideas.
- **Band 8**: Sufficiently addresses all parts; well-developed position with relevant, supported ideas.
- **Band 7**: Addresses all parts; clear position throughout; main ideas extended, though occasional over-generalisation may occur.
- **Band 6**: Addresses all parts, but some more fully than others; position is relevant though conclusions may become unclear.

## 2. Coherence & Cohesion (CC)
- **Band 9**: Cohesion attracts no attention; effortless progression; skilful paragraphing.
- **Band 8**: Sequences information logically; manages all aspects of cohesion well.
- **Band 7**: Logically organises ideas; clear progression throughout; appropriate range of cohesive devices.
- **Band 6**: Clear overall progression, but cohesion within/between sentences may be faulty or mechanical.

## 3. Lexical Resource (LR)
- **Band 9**: Wide range with natural and sophisticated control; rare slips only.
- **Band 8**: Fluently and flexibly conveys precise meanings; skilful use of uncommon lexical items.
- **Band 7**: Sufficient range with flexibility and precision; awareness of style and collocation.
- **Band 6**: Adequate range; attempts less common words but with occasional inaccuracy.

## 4. Grammatical Range & Accuracy (GRA)
- **Band 9**: Full flexibility and accuracy; error-free sentences are the norm.
- **Band 8**: Majority of sentences error-free; non-systematic inaccuracies only.
- **Band 7**: Variety of complex structures; frequent error-free sentences.
- **Band 6**: Mix of simple and complex sentences; errors rarely impede communication.
            """.trimIndent()
        ),
        OfflineStudyResource(
            id = "res_collocations",
            title = "High-Scoring Academic Collocations",
            category = "Lexical Resource",
            estimatedReadMinutes = 6,
            summary = "Curated academic word combinations that impress examiners and satisfy Band 7+ Lexical Resource requirements.",
            contentMarkdown = """
# Academic Collocations for IELTS Task 2
*Essential Lexical Assets*

Using individual "big words" out of context lowers your Lexical score. Examiners look for natural, authentic word partnerships (collocations).

## Technology & Automation
- `exert a profound influence on`: Smart automation exerts a profound influence on daily workflows.
- `exponential growth`: The past decade has witnessed exponential growth in artificial intelligence.
- `readily accessible`: Educational platforms have become readily accessible to students globally.

## Environment & Sustainability
- `deplete natural resources`: Industrial overconsumption threatens to deplete finite natural resources.
- `implement stringent regulations`: Governments must implement stringent regulations on carbon emissions.
- `irreversible environmental damage`: Failure to act now will precipitate irreversible environmental damage.

## Society & Education
- `foster critical thinking`: Modern curricula must foster critical thinking rather than rote memorization.
- `socio-economic disparity`: Widening socio-economic disparity poses a severe threat to social cohesion.
- `shoulder the responsibility`: Both public institutions and individuals must shoulder the responsibility.
            """.trimIndent()
        ),
        OfflineStudyResource(
            id = "res_cohesion_tactics",
            title = "Cohesion Beyond 'Moreover' and 'Furthermore'",
            category = "Coherence & Cohesion",
            estimatedReadMinutes = 4,
            summary = "Learn how to achieve Band 8 Coherence by eliminating mechanical transitional adverbs and mastering thematic referencing.",
            contentMarkdown = """
# Cohesion Beyond Mechanical Linkers
*By Ebrahim Tahassoni (2026)*

One of the most frequent examiner complaints in Task 2 is the mechanical overuse of linking words at the start of every sentence (e.g. *Firstly, Moreover, Furthermore, In addition, Consequently*).

## The Band 8+ Solution: Thematic & Pronoun Referencing

Instead of starting with a transition adverb, link your sentences by referencing the concept from the previous sentence:

### Example 1 (Mechanical Band 6):
> Many people are moving into urban centers. *Furthermore*, cities provide better healthcare. *Moreover*, job opportunities are abundant.

### Example 2 (Natural Band 8):
> Many people are relocating to urban centers. **This demographic shift** is largely propelled by superior healthcare infrastructure and the abundance of specialized employment opportunities.

Notice how "**This demographic shift**" organically glues the two thoughts together without shouting "transition!".

## Useful Organic Linkers
- *This state of affairs has led to...*
- *Such initiatives are likely to...*
- *In doing so, policymakers can...*
- *A case in point is...*
            """.trimIndent()
        ),
        OfflineStudyResource(
            id = "res_grammar_toolkit",
            title = "Band 7+ Complex Grammar Formulas",
            category = "Grammatical Range",
            estimatedReadMinutes = 5,
            summary = "Master 4 reliable sentence structures (Conditionals, Inversions, Concessions, Nominalizations) that demonstrate high grammatical range.",
            contentMarkdown = """
# Band 7+ Grammar Toolkit
*Formulas for Structural Variety*

To score Band 7 or higher in Grammatical Range & Accuracy, examiners require a demonstrable variety of complex structures.

## 1. Concession Clauses (Although / While / Whereas)
- Formula: *Although [Counter-argument], [Your stronger argument]*
- *Example*: Although proponents argue that space exploration drives technological advancement, the allocation of billions to interplanetary missions remains hard to justify while poverty persists on Earth.

## 2. Inversion for Emphasis (Not only... but also / Under no circumstances)
- Formula: *Not only does [Action], but it also [Consequence]*
- *Example*: Not only does renewable energy curtail greenhouse gas emissions, but it also creates sustainable employment in local communities.

## 3. Mixed / Advanced Conditionals
- Formula: *Were governments to [Verb in subjunctive], [Outcome]*
- *Example*: Were public authorities to subsidize electric transit systems, urban vehicular congestion would diminish noticeably.

## 4. Nominalization (Transforming verbs into academic nouns)
- Instead of: *Because the population is growing rapidly, housing is expensive.*
- Use: *The rapid growth of the urban population has precipitated an acute escalation in housing costs.*
            """.trimIndent()
        )
    )
}
