package com.campusping.assemblycrawler.parser

import com.campusping.assemblycrawler.model.Assembly
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.nio.file.Paths.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun parsePdf(fileName: String): List<Assembly> {
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

    val assemblies = parseAssemblyData(text)
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

private fun parseAssemblyData(rawText: String): List<Assembly> {
    val result = mutableListOf<Assembly>()
    val dateRegex = Regex("""\d{4}\.\s*\d{2}\.\s*\d{2}""")
    val timeRangeRegex = Regex("""(\d{2}:\d{2})~(\d{2}:\d{2})""")
    val entryRegex = Regex(
        """(\d{2}:\d{2}~(?:\d{2}:\d{2})?)\s+(.+?)\s*(?:<(.+?)>)?\s*(\d{1,3}(?:,\d{3})*|\d+)(?:명|)\s+([^\n<]+)"""
    )

    val dateMatches = dateRegex.findAll(rawText).toList()
    if (dateMatches.isEmpty()) return result

    for (dateMatch in dateMatches) {
        val dateStr = dateMatch.value.replace(" ", "")
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        val index = dateMatch.range.last
        val restText = rawText.substring(index)

        for (match in entryRegex.findAll(restText)) {
            val timeRange = match.groupValues[1]
            val location = match.groupValues[2].trim()
            val dong = match.groupValues.getOrNull(3)?.trim() ?: ""
            val participantsStr = match.groupValues[4]
            val district = match.groupValues[5].replace(" ", "").trim()

            val timeMatch = timeRangeRegex.find(timeRange)
            val (startTime, endTime) = if (timeMatch != null) {
                val (startStr, endStr) = timeMatch.destructured
                LocalDateTime.parse("${date}T$startStr") to LocalDateTime.parse("${date}T$endStr")
            } else {
                val onlyStartTimeMatch = Regex("""(\d{2}:\d{2})~""").find(timeRange)
                val startStr = onlyStartTimeMatch?.groupValues?.get(1)
                (startStr?.let { LocalDateTime.parse("${date}T$startStr") }) to null
            }

            val participants = participantsStr.replace(",", "").toInt()

            result.add(
                Assembly(
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

    return result
}
