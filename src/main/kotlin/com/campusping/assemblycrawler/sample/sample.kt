package com.campusping.assemblycrawler.sample

import com.campusping.assemblycrawler.AssemblyCrawler

internal fun main() {
    val assemblies = AssemblyCrawler.crawl(
        targetYear = 2025,
        targetMonth = 6
    )

    println("크롤링된 집회 정보: $assemblies")
}