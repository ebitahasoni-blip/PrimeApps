package com.example.util

import android.content.Context
import com.example.model.AssessmentReport
import com.example.model.CorrectionCategory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxExporter {

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Generates a fully formatted Microsoft Word (.docx) file containing:
     * - Official Prime Essay Feedback Header
     * - Task Topic in styled callout box
     * - Original essay with inline correction markup and explanatory comments
     * - Formal 2023 IELTS Band Descriptors Assessment Table
     * - Detailed criterion-by-criterion justifications
     * - Official Copyright notice: Ebrahim Tahassoni 2026
     */
    fun createDocxReport(context: Context, report: AssessmentReport): File {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.ENGLISH)
        val formattedDate = dateFormat.format(Date(report.createdAtTimestamp))

        val outputDir = File(context.cacheDir, "docx_reports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val safeName = "Prime_Essay_Report_${report.id.take(8)}.docx"
        val docxFile = File(outputDir, safeName)

        val docXml = buildDocumentXml(report, formattedDate)
        val contentTypesXml = buildContentTypesXml()
        val relsXml = buildRelsXml()
        val docRelsXml = buildDocumentRelsXml()
        val stylesXml = buildStylesXml()

        ZipOutputStream(FileOutputStream(docxFile)).use { zip ->
            // [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(contentTypesXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(relsXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // word/_rels/document.xml.rels
            zip.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            zip.write(docRelsXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // word/styles.xml
            zip.putNextEntry(ZipEntry("word/styles.xml"))
            zip.write(stylesXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // word/document.xml
            zip.putNextEntry(ZipEntry("word/document.xml"))
            zip.write(docXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        return docxFile
    }

    private fun buildDocumentXml(report: AssessmentReport, formattedDate: String): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n")
        sb.append("<w:body>\n")

        // Main Title Header
        sb.append("""
            <w:p>
                <w:pPr>
                    <w:jc w:val="center"/>
                    <w:spacing w:after="120"/>
                </w:pPr>
                <w:r>
                    <w:rPr>
                        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                        <w:b/>
                        <w:sz w:val="48"/>
                        <w:color w:val="1E3A8A"/>
                    </w:rPr>
                    <w:t>PRIME ESSAY FEEDBACK</w:t>
                </w:r>
            </w:p>
            <w:p>
                <w:pPr>
                    <w:jc w:val="center"/>
                    <w:spacing w:after="240"/>
                </w:pPr>
                <w:r>
                    <w:rPr>
                        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                        <w:i/>
                        <w:sz w:val="24"/>
                        <w:color w:val="0F766E"/>
                    </w:rPr>
                    <w:t>IELTS Writing Task 2 Diagnostic Assessment (2023 Official Descriptors)</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        // Metadata Info Bar
        sb.append("""
            <w:p>
                <w:pPr>
                    <w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/>
                    <w:spacing w:before="120" w:after="160"/>
                    <w:pBdr>
                        <w:bottom w:val="single" w:sz="12" w:space="8" w:color="CBD5E1"/>
                    </w:pBdr>
                </w:pPr>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="20"/><w:color w:val="334155"/></w:rPr>
                    <w:t>Date: </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:sz w:val="20"/><w:color w:val="475569"/></w:rPr>
                    <w:t>${escapeXml(formattedDate)}   |   </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="20"/><w:color w:val="334155"/></w:rPr>
                    <w:t>Word Count: </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:sz w:val="20"/><w:color w:val="475569"/></w:rPr>
                    <w:t>${report.wordCount} words (${report.paragraphCount} paragraphs)   |   </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="20"/><w:color w:val="1E3A8A"/></w:rPr>
                    <w:t>Awarded Overall Band: ${report.overallBandScore}</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        // Task Topic Box
        sb.append("""
            <w:p>
                <w:pPr>
                    <w:spacing w:before="240" w:after="100"/>
                </w:pPr>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="26"/><w:color w:val="1E3A8A"/></w:rPr>
                    <w:t>TASK 2 PROMPT / TOPIC</w:t>
                </w:r>
            </w:p>
            <w:p>
                <w:pPr>
                    <w:shd w:val="clear" w:color="auto" w:fill="EFF6FF"/>
                    <w:spacing w:before="80" w:after="240"/>
                    <w:pBdr>
                        <w:left w:val="single" w:sz="24" w:space="12" w:color="2563EB"/>
                    </w:pBdr>
                </w:pPr>
                <w:r>
                    <w:rPr><w:sz w:val="22"/><w:color w:val="1E293B"/></w:rPr>
                    <w:t>${escapeXml(report.topic)}</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        // Section Title: Essay with Correction Markup
        sb.append("""
            <w:p>
                <w:pPr><w:spacing w:before="240" w:after="120"/></w:pPr>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="26"/><w:color w:val="1E3A8A"/></w:rPr>
                    <w:t>ESSAY WITH CORRECTION MARKUP &amp; EXPLANATORY COMMENTS</w:t>
                </w:r>
            </w:p>
            <w:p>
                <w:pPr><w:spacing w:after="160"/></w:pPr>
                <w:r>
                    <w:rPr><w:i/><w:sz w:val="18"/><w:color w:val="64748B"/></w:rPr>
                    <w:t>Legend: </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:strike/><w:color w:val="DC2626"/><w:sz w:val="18"/></w:rPr>
                    <w:t>Red Strikethrough</w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:sz w:val="18"/><w:color w:val="64748B"/></w:rPr>
                    <w:t> = Error  |  </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:b/><w:color w:val="059669"/><w:sz w:val="18"/></w:rPr>
                    <w:t>[Green Bold]</w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:sz w:val="18"/><w:color w:val="64748B"/></w:rPr>
                    <w:t> = Recommended Replacement  |  </w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:i/><w:color w:val="7C3AED"/><w:sz w:val="18"/></w:rPr>
                    <w:t>{Comment}</w:t>
                </w:r>
                <w:r>
                    <w:rPr><w:sz w:val="18"/><w:color w:val="64748B"/></w:rPr>
                    <w:t> = Pedagogical Note</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        // Build essay paragraphs with inline markup
        val paragraphs = report.essayText.split("\n\n").filter { it.isNotBlank() }
        for (para in paragraphs) {
            sb.append("<w:p><w:pPr><w:spacing w:after=\"180\" w:line=\"280\" w:lineRule=\"auto\"/></w:pPr>")
            val paraMarkup = buildAnnotatedParagraphXml(para, report.corrections)
            sb.append(paraMarkup)
            sb.append("</w:p>\n")
        }

        // Section Title: Official 2023 Band Scores & Justification Table
        sb.append("""
            <w:p>
                <w:pPr><w:spacing w:before="360" w:after="160"/></w:pPr>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="26"/><w:color w:val="1E3A8A"/></w:rPr>
                    <w:t>OFFICIAL 2023 IELTS BAND SCORE ASSESSMENT TABLE</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        // The Assessment Table
        sb.append(buildScoreTableXml(report))

        // Examiner General Feedback & Personalized Action Plan
        sb.append("""
            <w:p>
                <w:pPr><w:spacing w:before="320" w:after="120"/></w:pPr>
                <w:r>
                    <w:rPr><w:b/><w:sz w:val="26"/><w:color w:val="1E3A8A"/></w:rPr>
                    <w:t>EXAMINER SYNTHESIS &amp; ACTION PLAN</w:t>
                </w:r>
            </w:p>
            <w:p>
                <w:pPr>
                    <w:shd w:val="clear" w:color="auto" w:fill="F8FAFC"/>
                    <w:spacing w:after="240"/>
                    <w:pBdr>
                        <w:left w:val="single" w:sz="18" w:space="10" w:color="0F766E"/>
                    </w:pBdr>
                </w:pPr>
                <w:r>
                    <w:rPr><w:sz w:val="22"/><w:color w:val="334155"/></w:rPr>
                    <w:t>${escapeXml(report.generalFeedback)}</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        // Formal Copyright Footer Notice
        sb.append("""
            <w:p>
                <w:pPr>
                    <w:jc w:val="center"/>
                    <w:spacing w:before="400" w:after="120"/>
                    <w:pBdr>
                        <w:top w:val="single" w:sz="6" w:space="8" w:color="E2E8F0"/>
                    </w:pBdr>
                </w:pPr>
                <w:r>
                    <w:rPr>
                        <w:sz w:val="18"/>
                        <w:color w:val="94A3B8"/>
                    </w:rPr>
                    <w:t>Prime Essay Feedback   |   Copyright © Ebrahim Tahassoni 2026. All rights reserved.</w:t>
                </w:r>
            </w:p>
        """.trimIndent())

        sb.append("</w:body>\n")
        sb.append("</w:document>")
        return sb.toString()
    }

    private fun buildAnnotatedParagraphXml(paragraph: String, corrections: List<com.example.model.CorrectionItem>): String {
        // Find corrections that appear in this paragraph
        val relevantCorrections = corrections.filter { correction ->
            paragraph.contains(correction.originalText)
        }.sortedBy { paragraph.indexOf(it.originalText) }

        if (relevantCorrections.isEmpty()) {
            return """
                <w:r>
                    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/><w:color w:val="0F172A"/></w:rPr>
                    <w:t xml:space="preserve">${escapeXml(paragraph)}</w:t>
                </w:r>
            """.trimIndent()
        }

        val sb = StringBuilder()
        var currentIndex = 0

        for (corr in relevantCorrections) {
            val start = paragraph.indexOf(corr.originalText, currentIndex)
            if (start == -1) continue

            // Text before error
            if (start > currentIndex) {
                val cleanPrefix = paragraph.substring(currentIndex, start)
                sb.append("<w:r><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/><w:sz w:val=\"22\"/><w:color w:val=\"0F172A\"/></w:rPr>")
                sb.append("<w:t xml:space=\"preserve\">${escapeXml(cleanPrefix)}</w:t></w:r>")
            }

            // The original error in red strikethrough
            sb.append("<w:r><w:rPr><w:strike/><w:color w:val=\"DC2626\"/><w:sz w:val=\"22\"/></w:rPr>")
            sb.append("<w:t xml:space=\"preserve\">${escapeXml(corr.originalText)}</w:t></w:r>")

            // The correction in bold green
            sb.append("<w:r><w:rPr><w:b/><w:color w:val=\"059669\"/><w:sz w:val=\"22\"/></w:rPr>")
            sb.append("<w:t xml:space=\"preserve\"> [${escapeXml(corr.suggestedText)}] </w:t></w:r>")

            // The explanatory comment in purple italics
            val catLabel = when (corr.category) {
                CorrectionCategory.GRAMMAR -> "Grammar Note"
                CorrectionCategory.LEXICAL -> "Vocabulary Note"
                CorrectionCategory.COHERENCE -> "Cohesion Note"
                CorrectionCategory.TASK_RESPONSE -> "Task Note"
                CorrectionCategory.PUNCTUATION -> "Punctuation Note"
            }
            sb.append("<w:r><w:rPr><w:i/><w:color w:val=\"7C3AED\"/><w:sz w:val=\"18\"/></w:rPr>")
            sb.append("<w:t xml:space=\"preserve\">{$catLabel: ${escapeXml(corr.explanation)}} </w:t></w:r>")

            currentIndex = start + corr.originalText.length
        }

        // Remaining text in paragraph
        if (currentIndex < paragraph.length) {
            val remainder = paragraph.substring(currentIndex)
            sb.append("<w:r><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/><w:sz w:val=\"22\"/><w:color w:val=\"0F172A\"/></w:rPr>")
            sb.append("<w:t xml:space=\"preserve\">${escapeXml(remainder)}</w:t></w:r>")
        }

        return sb.toString()
    }

    private fun buildScoreTableXml(report: AssessmentReport): String {
        val criteriaRows = listOf(
            Triple(report.taskResponse.criterion.title, report.taskResponse.bandScore, report.taskResponse.justification),
            Triple(report.coherenceCohesion.criterion.title, report.coherenceCohesion.bandScore, report.coherenceCohesion.justification),
            Triple(report.lexicalResource.criterion.title, report.lexicalResource.bandScore, report.lexicalResource.justification),
            Triple(report.grammaticalRangeAccuracy.criterion.title, report.grammaticalRangeAccuracy.bandScore, report.grammaticalRangeAccuracy.justification)
        )

        val sb = StringBuilder()
        sb.append("""
            <w:tbl>
                <w:tblPr>
                    <w:tblW w:w="5000" w:type="pct"/>
                    <w:tblBorders>
                        <w:top w:val="single" w:sz="8" w:space="0" w:color="CBD5E1"/>
                        <w:left w:val="single" w:sz="8" w:space="0" w:color="CBD5E1"/>
                        <w:bottom w:val="single" w:sz="8" w:space="0" w:color="CBD5E1"/>
                        <w:right w:val="single" w:sz="8" w:space="0" w:color="CBD5E1"/>
                        <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
                        <w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
                    </w:tblBorders>
                </w:tblPr>
                <w:tr>
                    <w:trPr><w:tblHeader/></w:trPr>
                    <w:tc>
                        <w:tcPr><w:tcW w:w="1500" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="1E3A8A"/></w:tcPr>
                        <w:p><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="20"/></w:rPr><w:t>Criterion</w:t></w:r></w:p>
                    </w:tc>
                    <w:tc>
                        <w:tcPr><w:tcW w:w="800" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="1E3A8A"/></w:tcPr>
                        <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="20"/></w:rPr><w:t>Band</w:t></w:r></w:p>
                    </w:tc>
                    <w:tc>
                        <w:tcPr><w:tcW w:w="2700" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="1E3A8A"/></w:tcPr>
                        <w:p><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="20"/></w:rPr><w:t>Justification &amp; 2023 Rubric Alignment</w:t></w:r></w:p>
                    </w:tc>
                </w:tr>
        """.trimIndent())

        for ((title, band, justification) in criteriaRows) {
            sb.append("""
                <w:tr>
                    <w:tc>
                        <w:tcPr><w:tcW w:w="1500" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="F8FAFC"/></w:tcPr>
                        <w:p><w:r><w:rPr><w:b/><w:sz w:val="20"/><w:color w:val="1E293B"/></w:rPr><w:t>${escapeXml(title)}</w:t></w:r></w:p>
                    </w:tc>
                    <w:tc>
                        <w:tcPr><w:tcW w:w="800" w:type="pct"/></w:tcPr>
                        <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="22"/><w:color w:val="0F766E"/></w:rPr><w:t>$band</w:t></w:r></w:p>
                    </w:tc>
                    <w:tc>
                        <w:tcPr><w:tcW w:w="2700" w:type="pct"/></w:tcPr>
                        <w:p><w:r><w:rPr><w:sz w:val="19"/><w:color w:val="334155"/></w:rPr><w:t>${escapeXml(justification)}</w:t></w:r></w:p>
                    </w:tc>
                </w:tr>
            """.trimIndent())
        }

        // Summary row for Overall Band
        sb.append("""
            <w:tr>
                <w:tc>
                    <w:tcPr><w:tcW w:w="1500" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="FEF3C7"/></w:tcPr>
                    <w:p><w:r><w:rPr><w:b/><w:sz w:val="22"/><w:color w:val="92400E"/></w:rPr><w:t>OVERALL BAND SCORE</w:t></w:r></w:p>
                </w:tc>
                <w:tc>
                    <w:tcPr><w:tcW w:w="800" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="FEF3C7"/></w:tcPr>
                    <w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="26"/><w:color w:val="B45309"/></w:rPr><w:t>${report.overallBandScore}</w:t></w:r></w:p>
                </w:tc>
                <w:tc>
                    <w:tcPr><w:tcW w:w="2700" w:type="pct"/><w:shd w:val="clear" w:color="auto" w:fill="FEF3C7"/></w:tcPr>
                    <w:p><w:r><w:rPr><w:b/><w:sz w:val="20"/><w:color w:val="92400E"/></w:rPr><w:t>Official IELTS Rounded Average of TR, CC, LR, and GRA.</w:t></w:r></w:p>
                </w:tc>
            </w:tr>
        </w:tbl>
        """.trimIndent())

        return sb.toString()
    }

    private fun buildContentTypesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
            <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
        </Types>
    """.trimIndent()

    private fun buildRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
        </Relationships>
    """.trimIndent()

    private fun buildDocumentRelsXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
    """.trimIndent()

    private fun buildStylesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
            <w:docDefaults>
                <w:rPrDefault>
                    <w:rPr>
                        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                        <w:sz w:val="22"/>
                        <w:lang w:val="en-US"/>
                    </w:rPr>
                </w:rPrDefault>
            </w:docDefaults>
        </w:styles>
    """.trimIndent()
}
