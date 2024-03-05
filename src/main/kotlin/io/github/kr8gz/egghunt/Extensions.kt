package io.github.kr8gz.egghunt

fun Int.pluralSuffix(suffix: String) = if (this != 1) suffix else ""
