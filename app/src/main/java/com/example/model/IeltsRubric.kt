package com.example.model

enum class IeltsCriterion(val title: String, val code: String) {
    TASK_RESPONSE("Task Response", "TR"),
    COHERENCE_COHESION("Coherence & Cohesion", "CC"),
    LEXICAL_RESOURCE("Lexical Resource", "LR"),
    GRAMMATICAL_RANGE_ACCURACY("Grammatical Range & Accuracy", "GRA")
}

data class BandDescriptor(
    val band: Int,
    val criterion: IeltsCriterion,
    val keySummary: String,
    val descriptors: List<String>
)

object Official2023Rubric {
    val descriptors: List<BandDescriptor> = listOf(
        // TASK RESPONSE
        BandDescriptor(
            band = 9,
            criterion = IeltsCriterion.TASK_RESPONSE,
            keySummary = "Fully addresses all parts of the task with a fully developed position.",
            descriptors = listOf(
                "Fully addresses all parts of the task",
                "Presents a fully developed position in answer to the question with relevant, fully extended and well-supported ideas",
                "Ideas are nuanced and perceptive"
            )
        ),
        BandDescriptor(
            band = 8,
            criterion = IeltsCriterion.TASK_RESPONSE,
            keySummary = "Sufficiently addresses all parts of the task with a well-developed response.",
            descriptors = listOf(
                "Sufficiently addresses all parts of the task",
                "Presents a well-developed position in answer to the question with relevant, extended and supported ideas",
                "May lack the nuanced development of Band 9"
            )
        ),
        BandDescriptor(
            band = 7,
            criterion = IeltsCriterion.TASK_RESPONSE,
            keySummary = "Addresses all parts of the task with a clear position throughout.",
            descriptors = listOf(
                "Addresses all parts of the task",
                "Presents a clear position throughout the response",
                "Presents, extends and supports main ideas, but there may be a tendency to over-generalise or supporting ideas may lack focus"
            )
        ),
        BandDescriptor(
            band = 6,
            criterion = IeltsCriterion.TASK_RESPONSE,
            keySummary = "Addresses all parts of the task, though some parts may be more fully covered than others.",
            descriptors = listOf(
                "Addresses all parts of the task, though some parts may be more fully covered than others",
                "Presents a relevant position, though the conclusions may become unclear or repetitive",
                "Presents relevant main ideas but some may be inadequately developed or unclear"
            )
        ),
        BandDescriptor(
            band = 5,
            criterion = IeltsCriterion.TASK_RESPONSE,
            keySummary = "Addresses the task only partially; the format may be inappropriate in places.",
            descriptors = listOf(
                "Addresses the task only partially; the format may be inappropriate in places",
                "Expresses a position, but the development is not always clear",
                "Presents some main ideas, but these are limited and not sufficiently developed; there may be irrelevant detail"
            )
        ),

        // COHERENCE & COHESION
        BandDescriptor(
            band = 9,
            criterion = IeltsCriterion.COHERENCE_COHESION,
            keySummary = "Cohesion is used in such a way that it attracts no attention.",
            descriptors = listOf(
                "Uses cohesion in such a way that it attracts no attention",
                "Skilfully manages paragraphing",
                "Full progression of ideas is effortlessly maintained"
            )
        ),
        BandDescriptor(
            band = 8,
            criterion = IeltsCriterion.COHERENCE_COHESION,
            keySummary = "Sequences information and ideas logically with managed paragraphing.",
            descriptors = listOf(
                "Sequences information and ideas logically",
                "Manages all aspects of cohesion well",
                "Uses paragraphing sufficiently and appropriately"
            )
        ),
        BandDescriptor(
            band = 7,
            criterion = IeltsCriterion.COHERENCE_COHESION,
            keySummary = "Logically organizes information and ideas with clear progression throughout.",
            descriptors = listOf(
                "Logically organises information and ideas; there is clear progression throughout",
                "Uses a range of cohesive devices appropriately although there may be some under-/over-use",
                "Presents a clear central topic within each paragraph"
            )
        ),
        BandDescriptor(
            band = 6,
            criterion = IeltsCriterion.COHERENCE_COHESION,
            keySummary = "Arranges information and ideas coherently with clear overall progression.",
            descriptors = listOf(
                "Arranges information and ideas coherently and there is a clear overall progression",
                "Uses cohesive devices effectively, but cohesion within and/or between sentences may be faulty or mechanical",
                "May not always use referencing clearly or appropriately; paragraphing may lack uniformity"
            )
        ),
        BandDescriptor(
            band = 5,
            criterion = IeltsCriterion.COHERENCE_COHESION,
            keySummary = "Presents information with some organisation but may lack overall progression.",
            descriptors = listOf(
                "Presents information with some organisation but there may be a lack of overall progression",
                "Makes inadequate, inaccurate or overuse of cohesive devices",
                "May be repetitive because of lack of referencing and substitution; paragraphing may be inadequate"
            )
        ),

        // LEXICAL RESOURCE
        BandDescriptor(
            band = 9,
            criterion = IeltsCriterion.LEXICAL_RESOURCE,
            keySummary = "Uses a wide range of vocabulary with very natural and sophisticated control.",
            descriptors = listOf(
                "Uses a wide range of vocabulary with very natural and sophisticated control of lexical features",
                "Rare minor errors occur only as 'slips'",
                "Full flexibility and precise meaning in all academic contexts"
            )
        ),
        BandDescriptor(
            band = 8,
            criterion = IeltsCriterion.LEXICAL_RESOURCE,
            keySummary = "Uses a wide resource fluently and flexibly to convey precise meanings.",
            descriptors = listOf(
                "Uses a wide resource fluently and flexibly to convey precise meanings",
                "Skilfully uses uncommon lexical items, but there may be occasional inaccuracies in word choice and collocation",
                "Produces rare errors in spelling and/or word formation"
            )
        ),
        BandDescriptor(
            band = 7,
            criterion = IeltsCriterion.LEXICAL_RESOURCE,
            keySummary = "Uses a sufficient range of vocabulary to allow some flexibility and precision.",
            descriptors = listOf(
                "Uses a sufficient range of vocabulary to allow some flexibility and precision",
                "Uses less common lexical items with some awareness of style and collocation",
                "May produce occasional errors in word choice, spelling and/or word formation"
            )
        ),
        BandDescriptor(
            band = 6,
            criterion = IeltsCriterion.LEXICAL_RESOURCE,
            keySummary = "Uses an adequate range of vocabulary for the task with some inaccuracies.",
            descriptors = listOf(
                "Uses an adequate range of vocabulary for the task",
                "Attempts to use less common vocabulary but with some inaccuracy",
                "Makes some errors in spelling and/or word formation, but they do not impede communication"
            )
        ),
        BandDescriptor(
            band = 5,
            criterion = IeltsCriterion.LEXICAL_RESOURCE,
            keySummary = "Uses a limited range of vocabulary, but minimally adequate for the task.",
            descriptors = listOf(
                "Uses a limited range of vocabulary, but this is minimally adequate for the task",
                "May make noticeable errors in spelling and/or word formation that may cause some difficulty for the reader",
                "Rarely attempts uncommon lexical items; frequent repetition"
            )
        ),

        // GRAMMATICAL RANGE & ACCURACY
        BandDescriptor(
            band = 9,
            criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
            keySummary = "Uses a wide range of structures with full flexibility and accuracy.",
            descriptors = listOf(
                "Uses a wide range of structures with full flexibility and accuracy",
                "Rare minor errors occur only as 'slips'",
                "Error-free sentences are the continuous norm"
            )
        ),
        BandDescriptor(
            band = 8,
            criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
            keySummary = "Uses a wide range of structures; the majority of sentences are error-free.",
            descriptors = listOf(
                "Uses a wide range of structures",
                "The majority of sentences are error-free",
                "Makes only very occasional errors or non-systematic inaccuracies"
            )
        ),
        BandDescriptor(
            band = 7,
            criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
            keySummary = "Uses a variety of complex structures with frequent error-free sentences.",
            descriptors = listOf(
                "Uses a variety of complex structures",
                "Produces frequent error-free sentences",
                "Has good control of grammar and punctuation but may make a few errors"
            )
        ),
        BandDescriptor(
            band = 6,
            criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
            keySummary = "Uses a mix of simple and complex sentence forms with some errors.",
            descriptors = listOf(
                "Uses a mix of simple and complex sentence forms",
                "Makes some errors in grammar and punctuation, but they rarely reduce communication",
                "Produces several error-free sentences, though errors persist in complex constructions"
            )
        ),
        BandDescriptor(
            band = 5,
            criterion = IeltsCriterion.GRAMMATICAL_RANGE_ACCURACY,
            keySummary = "Uses only a limited range of structures with frequent grammatical errors.",
            descriptors = listOf(
                "Uses only a limited range of structures",
                "Attempts complex sentences but these tend to be less accurate than simple sentences",
                "May make frequent grammatical errors and punctuation may be faulty; errors can cause some difficulty for the reader"
            )
        )
    )

    fun getDescriptorsFor(criterion: IeltsCriterion): List<BandDescriptor> {
        return descriptors.filter { it.criterion == criterion }.sortedByDescending { it.band }
    }

    fun getBandDescriptor(criterion: IeltsCriterion, band: Int): BandDescriptor? {
        val normalizedBand = band.coerceIn(5, 9)
        return descriptors.find { it.criterion == criterion && it.band == normalizedBand }
    }
}
