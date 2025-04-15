package com.campusping.assemblycrawler.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Assembly(
    val date: LocalDate,                // 집회 날짜
    val startTime: LocalDateTime?,            // 집회 일시 (LocalDateTime으로 변환)
    val endTime: LocalDateTime?,            // 집회 일시 (LocalDateTime으로 변환)
    val location: String,               // 집회 장소
    val dong: String,               // 동 (ex. 오류동)
    val participants: Int,           // 신고 인원
    val district: String,          // 관할서 (ex. 남대문)
)
