package com.campusping.assemblycrawler.crawling

import org.jsoup.Jsoup

// 목록 페이지에서 boardNo 추출
internal fun collectBoardNos(pageSize: Int): List<String> {
    val boardNos = mutableSetOf<String>()
    val boardRegex = Regex("goBoardView\\('[^']*','[^']*','(\\d+)'\\)")

    for (page in 1..pageSize) {
        val url = "https://www.smpa.go.kr/user/nd54882.do?page=$page"
        println("▶ 목록 페이지: $url")

        val doc = Jsoup.connect(url).get()

        // 게시글 목록에서 a 태그 전부 탐색
        val links = doc.select("a[href^=javascript:goBoardView]")

        for (link in links) {
            val href = link.attr("href")
            val match = boardRegex.find(href)
            match?.let {
                val boardNo = it.groupValues[1]
                if (boardNos.add(boardNo)) {
                    println("  - 발견된 boardNo: $boardNo")
                }
            }
        }
    }

    return boardNos.toList()
}