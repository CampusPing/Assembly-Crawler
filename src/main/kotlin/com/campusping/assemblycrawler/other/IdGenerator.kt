package com.campusping.assemblycrawler.other

import java.security.MessageDigest

fun generateId(vararg values: Any?): String {
    val input = values.joinToString("|") { it.toString() }
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
