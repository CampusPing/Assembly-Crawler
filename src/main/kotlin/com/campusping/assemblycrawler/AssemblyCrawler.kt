package com.campusping.assemblycrawler

import com.campusping.assemblycrawler.crawling.collectBoardNos
import com.campusping.assemblycrawler.crawling.downloadPdf
import com.campusping.assemblycrawler.crawling.parsePdfUrl
import com.campusping.assemblycrawler.model.OneDayAssembly
import com.campusping.assemblycrawler.parser.parsePdf
import org.jsoup.Jsoup
import java.time.LocalDate

object AssemblyCrawler {
    /**
     * 서울시 집회 정보를 크롤링하여 한 페이지에 해당하는 집회 정보를 반환합니다.
     *
     * @param targetYear 크롤링할 집회 정보의 연도 (예: 2025)
     * @param targetMonth 크롤링할 집회 정보의 월 (1월: 1, 2월: 2, ..., 12월: 12)
     *
     * @return [OneDayAssembly] 객체 목록으로, 크롤링한 집회 정보를 담고 있습니다.
     *         만약 PDF 정보가 없는 게시글이 있을 경우, 해당 게시글은 제외됩니다.
     *         또한, 크롤링할 연도와 월이 유효하지 않은 경우 빈 리스트를 반환합니다.
     *
     * @throws Exception 크롤링 중 발생할 수 있는 예외를 처리해야 할 수 있습니다.
     *
     * @sample com.campusping.assemblycrawler.sample.main
     */
    fun crawl(targetYear: Int, targetMonth: Int): List<OneDayAssembly> {
        if (!isValidDate(targetYear, targetMonth)) {
            return emptyList()
        }

        val oneDayAssemblies = mutableListOf<OneDayAssembly>()
        val boardNos = collectBoardNos(
            year = targetYear,
            month = targetMonth
        )

        boardNos.forEach { boardNo ->
            val boardUrl = "https://www.smpa.go.kr/user/nd54882.do?View&boardNo=$boardNo"
            println("\n[게시글 URL] $boardUrl")

            val oneDayAssembly = crawlOneDayAssembly(boardUrl, boardNo)

            if (oneDayAssembly == null) {
                println("[PDF 없음] $boardUrl")
                return@forEach
            }
            if (oneDayAssembly.date.year != targetYear || oneDayAssembly.date.monthValue != targetMonth) {
                println("[날짜 불일치] ${oneDayAssembly.date} (원하는 날짜: $targetYear-$targetMonth)")
                return@forEach
            }

            oneDayAssemblies.add(oneDayAssembly)
        }

        return oneDayAssemblies.sortedBy(OneDayAssembly::date)
    }

    private fun isValidDate(year: Int, month: Int): Boolean {
        val nowYear = LocalDate.now().year

        if (year < nowYear) {
            println("[오류] 현재 년도보다 이전 년도는 크롤링 할 수 없습니다 : $year < $nowYear")
            return false
        }
        if (month < 1 || month > 12) {
            println("[오류] 월은 1월부터 12월 사이여야 합니다 : $month")
            return false
        }

        return true
    }

    private fun crawlOneDayAssembly(
        boardUrl: String,
        boardNo: String
    ): OneDayAssembly? {
        val doc = Jsoup.connect(boardUrl).get()

        val titleText =
            doc.selectXpath("/html/body/form/div/section/div[1]/div/div[2]/div/div[2]/table/tbody/tr[1]/td[1]")
                .firstOrNull()?.text() ?: return null

        val dateFromTitle = parseDateFromTitle(titleText) ?: return null
        println("[제목에서 추출한 날짜] $dateFromTitle")

        // 2. PDF 링크 파싱
        val pdfUrl = parsePdfUrl(doc = doc, boardNo = boardNo) ?: return null
        println("[PDF 링크] $pdfUrl")

        val fileName = "temp_$boardNo.pdf"
        downloadPdf(pdfUrl, fileName)
        println("[PDF 다운로드 완료] $fileName")

        val assemblies = parsePdf(fileName, dateFromTitle)
        if (assemblies.isEmpty()) return null

        val updatedAssemblies = assemblies.map {
            it.copy(date = dateFromTitle)
        }

        return OneDayAssembly(
            date = dateFromTitle,
            assemblies = updatedAssemblies,
        )
    }

    private fun parseDateFromTitle(title: String): LocalDate? {
        val regex = Regex("""\d{6}""")
        val match = regex.find(title)?.value ?: return null

        return try {
            val year = "20" + match.substring(0, 2)      // 25 -> 2025
            val month = match.substring(2, 4)             // 07
            val day = match.substring(4, 6)               // 08
            LocalDate.parse("$year-$month-$day")
        } catch (e: Exception) {
            null
        }
    }
}
