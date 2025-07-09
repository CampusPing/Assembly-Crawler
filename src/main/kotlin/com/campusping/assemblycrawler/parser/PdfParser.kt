package com.campusping.assemblycrawler.parser

import com.campusping.assemblycrawler.model.Assembly
import com.campusping.assemblycrawler.other.generateId
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.nio.file.Paths.get
import java.time.LocalDate
import java.time.LocalDateTime

internal fun parsePdf(fileName: String, overrideDate: LocalDate): List<Assembly> {
    val file = get("src", "main", "resources", "pdfs", fileName).toFile()

    if (!file.exists()) {
        println("[PDF 파일 없음] ${file.absolutePath}")
        return emptyList()
    }

    val pdfDoc = PDDocument.load(file)
    val stripper = PDFTextStripper()
    val text = stripper.getText(pdfDoc)
    pdfDoc.close()

    println("\n[PDF 내용 출력]\n$text")

    val assemblies = parseAssemblyData(text, overrideDate)
    println("\n[추출된 Assembly 목록]")
    assemblies.forEach(::println)

    // 파일 삭제
    if (file.exists()) {
        val deleted = file.delete()
        if (deleted) {
            println("[PDF 파일 삭제 완료] ${file.absolutePath}")
        } else {
            println("[PDF 파일 삭제 실패] ${file.absolutePath}")
        }
    }

    return assemblies
}

private fun parseAssemblyData(rawText: String, date: LocalDate): List<Assembly> {
    val noDuplicatedRawText = rawText.removeDuplicateLines()
    val oneDayAssembly = mutableListOf<Assembly>()

    val multiTimeEntryRegex = Regex(
        """((?:\d{2}:\d{2}~(?:翌\s*)?(?:\d{2}:\d{2})?\s*\n?)+)([^\n]+)\s*(?:<(.+?)>)?\s*(\d{1,3}(?:,\d{3})*|\d+)(?:명|)\s+([^\n<]+)"""
    )
    val timeRangeRegex = Regex("""(\d{2}:\d{2})~(?:翌\s*)?(\d{2}:\d{2})?""")

    for (match in multiTimeEntryRegex.findAll(noDuplicatedRawText)) {
        val allTimeRangesText = match.groupValues[1]
        val location = match.groupValues[2].trim()
        val dong = match.groupValues.getOrNull(3)?.trim() ?: ""
        val participants = match.groupValues[4].replace(",", "").toInt()
        val district = match.groupValues[5].replace(" ", "").trim()

        for (timeMatch in timeRangeRegex.findAll(allTimeRangesText)) {
            var (startStr, endStr) = timeMatch.destructured
            val fullMatch = timeMatch.value
            val isNextDay = fullMatch.contains("翌")

            // 24시 보정
            if (startStr.startsWith("24:")) startStr = startStr.replaceFirst("24:", "00:")
            if (endStr.startsWith("24:")) endStr = endStr.replaceFirst("24:", "00:")

            val startTime = LocalDateTime.parse("${date}T$startStr")
            val endTime = if (endStr.isNotBlank()) {
                val endDate = if (isNextDay) date.plusDays(1) else date
                LocalDateTime.parse("${endDate}T$endStr")
            } else {
                date.atTime(23, 59)
            }

            val assemblyId = generateId(date, location)

            oneDayAssembly.add(
                Assembly(
                    id = assemblyId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    dong = dong,
                    participants = participants,
                    district = district
                )
            )
        }
    }

    return oneDayAssembly.filterAnomalyParticipant()
}

private fun String.removeDuplicateLines() = this
    .lines()
    .fold(mutableListOf<String>()) { acc, line ->
        if (acc.isEmpty() || acc.last().trim() != line.trim()) {
            acc.add(line)
        }
        acc
    }.joinToString("\n")

private fun List<Assembly>.filterAnomalyParticipant(): List<Assembly> {
    return this.filter { assembly -> assembly.participants > 0 }
}
