package com.campusping.assemblycrawler

import com.campusping.assemblycrawler.crawling.collectBoardNos
import com.campusping.assemblycrawler.crawling.downloadPdf
import com.campusping.assemblycrawler.crawling.parsePdfUrl
import com.campusping.assemblycrawler.model.OneDayAssembly
import org.jsoup.Jsoup
import com.campusping.assemblycrawler.parser.parsePdf

object AssemblyCrawler {
    /**
     * 서울시 집회 정보를 크롤링하여 한 페이지에 해당하는 집회 정보를 반환합니다.
     *
     * @param pageSize 크롤링할 페이지 크기 (기본값은 1페이지). 각 페이지는 최대 10개의 집회 정보를 포함합니다.
     *
     * @return [OneDayAssembly] 객체 목록으로, 크롤링한 집회 정보를 담고 있습니다.
     *         만약 PDF 정보가 없는 게시글이 있을 경우, 해당 게시글은 제외됩니다.
     *
     * @throws Exception 크롤링 중 발생할 수 있는 예외를 처리해야 할 수 있습니다.
     */
    fun crawl(pageSize: Int = 1): List<OneDayAssembly> {
        val oneDayAssemblies = mutableListOf<OneDayAssembly>()

        val boardNos = collectBoardNos(pageSize = pageSize)
        boardNos.forEach { boardNo ->
            val boardUrl = "https://www.smpa.go.kr/user/nd54882.do?View&boardNo=$boardNo"
            println("\n[게시글 URL] $boardUrl")

            val oneDayAssembly = crawlOneDayAssembly(boardUrl, boardNo)

            if (oneDayAssembly != null) {
                oneDayAssemblies.add(oneDayAssembly)
            } else {
                println("[PDF 없음] $boardUrl")
            }
        }

        return oneDayAssemblies
    }

    private fun crawlOneDayAssembly(
        boardUrl: String,
        boardNo: String
    ): OneDayAssembly? {
        val assemblyPdf = Jsoup.connect(boardUrl).get()
        val pdfUrl = parsePdfUrl(doc = assemblyPdf, boardNo = boardNo) ?: return null
        println("[PDF 링크] $pdfUrl")

        val fileName = "temp_$boardNo.pdf"
        downloadPdf(pdfUrl, fileName)
        println("[PDF 다운로드 완료] $fileName")

        val assemblies = parsePdf(fileName)
        val firstAssembly = assemblies.firstOrNull() ?: return null

        return OneDayAssembly(
            date = firstAssembly.date,
            assemblies = assemblies,
        )
    }
}
