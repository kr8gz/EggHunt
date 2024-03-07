package io.github.kr8gz.egghunt.commands

fun Number.pluralSuffix(suffix: String) = if (this != 1) suffix else ""
