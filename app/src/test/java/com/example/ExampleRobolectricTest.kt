package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EssayRepository
import com.example.model.AssessmentReport
import com.example.util.DocxExporter
import com.example.util.IeltsAssessmentEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Prime Essay Feedback", appName)
    }

    @Test
    fun `test official IELTS band score calculation and rounding`() {
        // Official IELTS rounding rule:
        // 6.25 -> 6.5
        assertEquals(6.5f, AssessmentReport.calculateIeltsOverallBand(6.5f, 6.0f, 6.5f, 6.0f), 0.01f)
        // 6.75 -> 7.0
        assertEquals(7.0f, AssessmentReport.calculateIeltsOverallBand(7.0f, 6.5f, 7.0f, 6.5f), 0.01f)
        // 6.125 -> 6.0
        assertEquals(6.0f, AssessmentReport.calculateIeltsOverallBand(6.0f, 6.0f, 6.5f, 6.0f), 0.01f)
    }

    @Test
    fun `test essay assessment engine produces 2023 rubric evaluation`() {
        val report = IeltsAssessmentEngine.evaluateEssay(
            topic = EssayRepository.SAMPLE_TOPICS.first(),
            essayText = EssayRepository.SAMPLE_ESSAY
        )

        assertNotNull(report)
        assertTrue(report.wordCount >= 250)
        assertTrue(report.overallBandScore in 5.0f..9.0f)
        assertTrue(report.taskResponse.bandScore in 5.0f..9.0f)
        assertTrue(report.coherenceCohesion.bandScore in 5.0f..9.0f)
        assertTrue(report.lexicalResource.bandScore in 5.0f..9.0f)
        assertTrue(report.grammaticalRangeAccuracy.bandScore in 5.0f..9.0f)
        assertTrue(report.rubricLinks.isNotEmpty())
    }

    @Test
    fun `test genuine docx file generation with table and copyright`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val report = IeltsAssessmentEngine.evaluateEssay(
            topic = EssayRepository.SAMPLE_TOPICS.first(),
            essayText = EssayRepository.SAMPLE_ESSAY
        )

        val docxFile = DocxExporter.createDocxReport(context, report)
        assertTrue(docxFile.exists())
        assertTrue(docxFile.length() > 0)

        // Validate that docx is a valid zip package with document.xml
        ZipFile(docxFile).use { zip ->
            val documentEntry = zip.getEntry("word/document.xml")
            assertNotNull(documentEntry)
            val stream = zip.getInputStream(documentEntry)
            val content = stream.bufferedReader().use { it.readText() }
            assertTrue(content.contains("PRIME ESSAY FEEDBACK"))
            assertTrue(content.contains("Ebrahim Tahassoni 2026"))
            assertTrue(content.contains("OFFICIAL 2023 IELTS BAND SCORE ASSESSMENT TABLE"))
        }
    }
}
