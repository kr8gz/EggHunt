package io.github.kr8gz.egghunt

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun Number.pluralSuffix(suffix: String) = if (this != 1) suffix else ""

// Create a `Text` from `Formatting` and a `String`
operator fun Formatting.plus(text: String): MutableText = Text.literal(text).formatted(this)

// After `Text + Formatting` we expect another string → lambda
operator fun MutableText.plus(formatting: Formatting) = { text: String -> this.append(formatting + text) }

// From that lambda create another `Text`
operator fun ((String) -> MutableText).plus(text: String) = this(text)

// Add 2 existing `Text` objects together
operator fun MutableText.plus(text: MutableText): MutableText = this.append(text)
