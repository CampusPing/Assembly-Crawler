package com.campusping.assemblycrawler.crawling

import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun collectBoardNos(year: Int, month: Int): List<String> {
    val boardNos = mutableSetOf<String>()
    val boardRegex = Regex("goBoardView\\('[^']*','[^']*','(\\d+)'\\)")
    val dateRegex = Regex("오늘의 집회 (\\d{6})")
    val formatter = DateTimeFormatter.ofPattern("yyMMdd")

    var page = 1

    while (true) {
        val url = "https://www.smpa.go.kr/user/nd54882.do?page=$page"
        println("▶ 목록 페이지: $url")

        val doc = Jsoup.connect(url).get()
        val rows = doc.select("table tbody tr")

        if (rows.isEmpty()) {
            println("  - 더 이상 게시물이 없습니다. 크롤링 종료")
            break
        }

        var reachedOldPost = false

        for (row in rows) {
            val link = row.selectFirst("td:nth-child(2) a") ?: continue
            val href = link.attr("href")
            val text = link.text()

            val boardMatch = boardRegex.find(href)
            val dateMatch = dateRegex.find(text)

            if (boardMatch != null && dateMatch != null) {
                val boardNo = boardMatch.groupValues[1]
                val dateStr = dateMatch.groupValues[1]
                val parsedDate = LocalDate.parse(dateStr, formatter)

                println("  - 발견된 boardNo: $boardNo, 날짜: $parsedDate")

                if (parsedDate.year < year || (parsedDate.year == year && parsedDate.monthValue < month)) {
                    println("  - ${year}년 ${month}월 이전 게시물 발견 → 크롤링 종료")
                    reachedOldPost = true
                    break
                }

                if (parsedDate.year == year && parsedDate.monthValue == month) {
                    boardNos.add(boardNo)
                }
            }
        }

        if (reachedOldPost) break

        page++
    }

    return boardNos.toList()
}
