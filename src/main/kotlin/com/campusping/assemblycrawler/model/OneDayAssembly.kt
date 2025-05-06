package com.campusping.assemblycrawler.model

import java.time.LocalDate

data class OneDayAssembly(
    val date: LocalDate,
    val assemblies: List<Assembly>
)
