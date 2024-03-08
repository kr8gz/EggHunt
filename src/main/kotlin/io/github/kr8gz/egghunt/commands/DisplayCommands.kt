package io.github.kr8gz.egghunt.commands

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.database.inDatabase
import io.github.kr8gz.egghunt.plus
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object DisplayCommands {
    fun getProgressMessage(context: ServerCommandContext): Text {
        val totalEggs = Database.Eggs.totalCount().also {
            if (it == 0L) return EggHunt.MESSAGE_PREFIX + Text.translatable("command.egghunt.progress.no_eggs").formatted(Formatting.RED)
        }

        @Suppress("UsePropertyAccessSyntax")
        val playerFound = context.source.getPlayerOrThrow().inDatabase().foundEggCount()
        val foundAllEggs = playerFound == totalEggs

        val percentageText = (playerFound / totalEggs.toFloat() * 100).let { foundPercentage ->
            val percentageColor = when {
                foundAllEggs -> Formatting.AQUA
                foundPercentage >= 75 -> Formatting.GREEN
                foundPercentage >= 50 -> Formatting.YELLOW
                foundPercentage >= 25 -> Formatting.GOLD
                else -> Formatting.RED
            }

            val formattedPercentage = run {
                val decimalSymbols = DecimalFormatSymbols(Locale.US)
                DecimalFormat("#.##", decimalSymbols).format(foundPercentage)
            }

            Text.translatable("command.egghunt.progress.percentage", formattedPercentage).formatted(percentageColor)
        }

        return EggHunt.MESSAGE_PREFIX + when {
            foundAllEggs && totalEggs == 1L -> Text.translatable("command.egghunt.progress.found.single")
            foundAllEggs -> Text.translatable("command.egghunt.progress.found.multiple", Formatting.WHITE + "$totalEggs")
            totalEggs == 1L -> Text.translatable("command.egghunt.progress.single")
            else -> Text.translatable("command.egghunt.progress.multiple", Formatting.WHITE + "$playerFound", Formatting.WHITE + "$totalEggs")
        }.formatted(Formatting.GRAY) + Formatting.GRAY + " (" + percentageText + Formatting.GRAY + ")"
    }

    fun getLeaderboardMessage(context: ServerCommandContext): Text {
        val leaderboard = Database.getLeaderboard().also {
            if (it.isEmpty()) return EggHunt.MESSAGE_PREFIX + Text.translatable("command.egghunt.leaderboard.no_eggs").formatted(Formatting.RED)
        }

        val executorPlayerName = context.source.player?.name?.string

        val topEntries = leaderboard.take(9).toMutableList()
        leaderboard.find { it.playerName == executorPlayerName }?.let {
            if (it !in topEntries) topEntries[topEntries.lastIndex] = it
        }

        return EggHunt.MESSAGE_PREFIX.apply {
            append(Text.translatable("command.egghunt.leaderboard.label").formatted(Formatting.YELLOW))
            topEntries.forEach { entry ->
                val entryType = if (entry.eggsFound == 1L) "single" else "multiple"
                val playerNameColor = if (entry.playerName == executorPlayerName) Formatting.GREEN else Formatting.RED
                append("\n")
                append(Formatting.WHITE + "${entry.rank}. " + Text.translatable("command.egghunt.leaderboard.entry.$entryType", playerNameColor + entry.playerName, Formatting.WHITE + "%,d".format(entry.eggsFound)).formatted(Formatting.GRAY))
            }
        }
    }
}
