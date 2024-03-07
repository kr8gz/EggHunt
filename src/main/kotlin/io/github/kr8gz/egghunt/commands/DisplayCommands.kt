package io.github.kr8gz.egghunt.commands

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.database.inDatabase
import io.github.kr8gz.egghunt.pluralSuffix
import io.github.kr8gz.egghunt.plus
import net.minecraft.text.Text
import net.minecraft.util.Formatting.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object DisplayCommands {
    fun getProgressMessage(context: ServerCommandContext): Text {
        val totalEggs = Database.Eggs.totalCount().also {
            if (it == 0L) return EggHunt.MESSAGE_PREFIX.append(RED + "There are no eggs to be found... yet!")
        }

        @Suppress("UsePropertyAccessSyntax")
        val playerFound = context.source.getPlayerOrThrow().inDatabase().foundEggCount()
        val foundAllEggs = playerFound == totalEggs

        val percentageText = (playerFound / totalEggs.toFloat() * 100).let { foundPercentage ->
            val percentageColor = when {
                foundAllEggs -> AQUA
                foundPercentage >= 75 -> GREEN
                foundPercentage >= 50 -> YELLOW
                foundPercentage >= 25 -> GOLD
                else -> RED
            }

            val formattedPercentage = run {
                val decimalSymbols = DecimalFormatSymbols(Locale.US)
                DecimalFormat("#.##", decimalSymbols).format(foundPercentage)
            }

            GRAY + "(" + percentageColor + "$formattedPercentage%" + GRAY + ")"
        }

        return EggHunt.MESSAGE_PREFIX + when {
            foundAllEggs && totalEggs == 1L -> GRAY + "You found " + WHITE + "the only" + GRAY + " egg! " + percentageText
            foundAllEggs -> GRAY + "You found all " + WHITE + "$totalEggs" + GRAY + " eggs! " + percentageText
            else ->
                GRAY + "You found " + WHITE + "$playerFound" + GRAY + " out of " +
                WHITE + "$totalEggs" + GRAY + " egg${totalEggs.pluralSuffix("s")} " + percentageText
        }
    }

    fun getLeaderboardMessage(context: ServerCommandContext): Text {
        val leaderboard = Database.getLeaderboard().also {
            if (it.isEmpty()) return EggHunt.MESSAGE_PREFIX.append(RED + "No eggs have been found so far...")
        }

        val executorPlayerName = context.source.player?.name?.string

        val topEntries = leaderboard.take(9).toMutableList()
        leaderboard.find { it.playerName == executorPlayerName }?.let {
            if (it !in topEntries) topEntries[topEntries.lastIndex] = it
        }

        return EggHunt.MESSAGE_PREFIX.apply {
            append(YELLOW + "Leaderboard")
            topEntries.forEach { entry ->
                val playerNameColor = if (entry.playerName == executorPlayerName) GREEN else RED
                append("\n")
                append(
                    WHITE + "${entry.rank}. " + playerNameColor + entry.playerName + GRAY + " found " +
                    WHITE + "%,d".format(entry.eggsFound) + GRAY + " egg${entry.eggsFound.pluralSuffix("s")}"
                )
            }
        }
    }
}
