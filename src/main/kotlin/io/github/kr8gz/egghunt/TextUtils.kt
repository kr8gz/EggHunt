package io.github.kr8gz.egghunt

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

operator fun Formatting.plus(text: String): MutableText = Text.literal(text).formatted(this)

operator fun MutableText.plus(text: Text): MutableText = this.append(text)
