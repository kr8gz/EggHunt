package io.github.kr8gz.egghunt.commands

import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.Database.getEggCount
import io.github.kr8gz.egghunt.EggHunt
import net.minecraft.text.Text
import net.minecraft.util.Formatting.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object DisplayCommands {
    fun getProgressMessage(context: ServerCommandContext): Text {
        val totalEggs = Database.getTotalEggCount()?.also {
            if (it == 0) return EggHunt.MESSAGE_PREFIX.append("${RED}There are no eggs to be found... yet!")
        } ?: return EggHunt.MESSAGE_PREFIX.append("${RED}Could not determine total number of eggs, please try again!")

        @Suppress("UsePropertyAccessSyntax")
        val playerFound = context.source.getPlayerOrThrow().getEggCount() ?: return EggHunt.MESSAGE_PREFIX.append(
            "${RED}Could not determine number of found eggs, please try again!"
        )

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

            "$GRAY($percentageColor$formattedPercentage%$GRAY)"
        }

        return when {
            foundAllEggs && totalEggs == 1 -> EggHunt.MESSAGE_PREFIX.append(
                "${GRAY}You found ${WHITE}the only ${GRAY}egg! $percentageText"
            )
            foundAllEggs -> EggHunt.MESSAGE_PREFIX.append(
                "${GRAY}You found all $WHITE$totalEggs ${GRAY}eggs! $percentageText"
            )
            else -> EggHunt.MESSAGE_PREFIX.append(
                "${GRAY}You found $WHITE$playerFound ${GRAY}out of $WHITE$totalEggs ${GRAY}egg${totalEggs.pluralSuffix("s")} $percentageText"
            )
        }
    }

    fun getLeaderboardMessage(context: ServerCommandContext): Text {
        val leaderboard = Database.getLeaderboard()?.also {
            if (it.isEmpty()) return EggHunt.MESSAGE_PREFIX.append("${RED}No eggs have been found so far...")
        } ?: return EggHunt.MESSAGE_PREFIX.append("${RED}Could not retrieve the leaderboard, please try again!")

        val executorPlayerName = context.source.player?.name?.string

        val topEntries = leaderboard.take(9).toMutableList()
        leaderboard.find { it.playerName == executorPlayerName }?.let {
            if (it !in topEntries) topEntries[topEntries.lastIndex] = it
        }

        return EggHunt.MESSAGE_PREFIX.apply {
            append("${YELLOW}Leaderboard")
            topEntries.forEach { entry ->
                val playerNameColor = if (entry.playerName == executorPlayerName) GREEN else RED
                val foundEggsText = "$WHITE%,d ${GRAY}egg${entry.eggsFound.pluralSuffix("s")}".format(entry.eggsFound)
                append("\n$WHITE${entry.rank}. $playerNameColor${entry.playerName} ${GRAY}found $foundEggsText")
            }
        }
    }
}
