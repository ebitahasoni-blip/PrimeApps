package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essays")
data class EssayEntity(
    @PrimaryKey
    val id: String,
    val topic: String,
    val essayText: String,
    val wordCount: Int,
    val paragraphCount: Int,
    val createdAtTimestamp: Long,
    val overallBandScore: Float,
    val taskResponseBand: Float,
    val coherenceBand: Float,
    val lexicalBand: Float,
    val grammarBand: Float,
    val trJustification: String,
    val ccJustification: String,
    val lrJustification: String,
    val graJustification: String,
    val generalFeedback: String,
    val correctionsJson: String,
    val rubricLinksJson: String,
    val isSynced: Boolean = false,
    val docxFilePath: String? = null
)
