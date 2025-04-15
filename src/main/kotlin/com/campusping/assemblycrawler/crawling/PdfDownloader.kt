package com.campusping.assemblycrawler.crawling

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.Paths

internal fun downloadPdf(url: String, fileName: String) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()

    val saveDir = Paths.get("src", "main", "resources", "pdfs").toFile()
    if (!saveDir.exists()) saveDir.mkdirs()

    val file = File(saveDir, fileName)
    file.writeBytes(response.body!!.bytes())

    println("[PDF 저장 위치] ${file.absolutePath}")
}
