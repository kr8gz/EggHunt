package io.github.kr8gz.egghunt.commands

import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.inDatabase
import io.github.kr8gz.egghunt.plus
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object DisplayCommands {
    fun getProgressMessage(context: ServerCommandContext): Text {
        val totalEggs = Database.Eggs.totalCount().also {
            if (it == 0) return EggHunt.MESSAGE_PREFIX + Text.translatable("command.egghunt.progress.no_eggs").formatted(Formatting.RED)
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
            foundAllEggs && totalEggs == 1 -> Text.translatable("command.egghunt.progress.found.single", percentageText)
            foundAllEggs -> Text.translatable("command.egghunt.progress.found.multiple", Formatting.WHITE + "$totalEggs", percentageText)
            totalEggs == 1 -> Text.translatable("command.egghunt.progress.single", percentageText)
            else -> Text.translatable("command.egghunt.progress.multiple", Formatting.WHITE + "$playerFound", Formatting.WHITE + "$totalEggs", percentageText)
        }.formatted(Formatting.GRAY)
    }

    fun getLeaderboardMessage(context: ServerCommandContext): Text {
        val executorPlayerName = context.source.player?.name?.string
        val leaderboard = Database.getLeaderboard(executorPlayerName)

        return EggHunt.MESSAGE_PREFIX + if (leaderboard.isEmpty()) {
            Text.translatable("command.egghunt.leaderboard.no_eggs").formatted(Formatting.RED)
        } else {
            Text.translatable("command.egghunt.leaderboard.label").formatted(Formatting.YELLOW).apply {
                leaderboard.forEach { entry ->
                    val translationKey = "command.egghunt.leaderboard.entry.${if (entry.eggsFound == 1) "single" else "multiple"}"
                    val playerNameColor = if (entry.playerName == executorPlayerName) Formatting.GREEN else Formatting.RED

                    val leaderboardEntry = Text.translatable(translationKey, playerNameColor + entry.playerName, Formatting.WHITE + "%,d".format(entry.eggsFound))

                    append("\n${entry.rank}. ")
                    append(leaderboardEntry.formatted(Formatting.GRAY))
                }
            }
        }
    }
}
