package com.campusping.assemblycrawler.crawling

import org.jsoup.nodes.Document

internal fun parsePdfUrl(doc: Document, boardNo: String): String? {
    val anchors = doc.select("table tbody tr td a[onclick^=attachfileDownload]")
    val pdfAnchor = anchors.firstOrNull { it.text().trim().lowercase().endsWith(".pdf") }

    if (pdfAnchor == null) {
        println("[PDF 링크 없음] boardNo: $boardNo")
        return null
    }

    val onclick = pdfAnchor.attr("onclick")
    val regex = Regex("attachfileDownload\\('([^']+)',\\s*'([^']+)'\\)")
    val match = regex.find(onclick) ?: return null

    val path = match.groupValues[1]
    val attachNo = match.groupValues[2]

    return "https://www.smpa.go.kr$path?attachNo=$attachNo"
}
