package io.github.kr8gz.egghunt.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.Database.getEggCount
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.pluralSuffix
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting.*
import java.text.DecimalFormat

internal fun displayPlayerProgress(context: CommandContext<ServerCommandSource>): Int {
    context.source.sendFeedback({
        val totalEggs = Database.getTotalEggCount()!!.also {
            if (it == 0) {
                return@sendFeedback EggHunt.MESSAGE_PREFIX.append("${RED}There are no eggs to be found... yet!")
            }
        }

        @Suppress("UsePropertyAccessSyntax")
        val playerFound = context.source.getPlayerOrThrow().getEggCount()!!
        val foundAllEggs = playerFound == totalEggs

        val percentageText = (playerFound / totalEggs.toFloat() * 100).let { foundPercentage ->
            val percentageColor = when {
                foundAllEggs -> AQUA
                foundPercentage >= 75 -> GREEN
                foundPercentage >= 50 -> YELLOW
                foundPercentage >= 25 -> GOLD
                else -> RED
            }
            val formattedPercentage = DecimalFormat("#.##").format(foundPercentage)
            "$GRAY($percentageColor$formattedPercentage%$GRAY)"
        }

        when {
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
    }, false)
    return Command.SINGLE_SUCCESS
}

internal fun displayLeaderboard(context: CommandContext<ServerCommandSource>): Int {
    context.source.sendFeedback({
        val leaderboard = Database.getLeaderboard()!!.also {
            if (it.isEmpty()) {
                return@sendFeedback EggHunt.MESSAGE_PREFIX.append("${RED}No eggs have been found so far...")
            }
        }

        val executorPlayerName = context.source.player?.name?.literalString

        val topEntries = leaderboard.take(9).toMutableList()
        leaderboard.find { it.playerName == executorPlayerName }?.let {
            if (it !in topEntries) topEntries[topEntries.lastIndex] = it
        }

        EggHunt.MESSAGE_PREFIX.apply {
            append("${YELLOW}Leaderboard")
            topEntries.forEach { entry ->
                val playerNameColor = if (entry.playerName == executorPlayerName) GREEN else RED
                val foundEggsText = "$WHITE%,d ${GRAY}egg${entry.eggsFound.pluralSuffix("s")}".format(entry.eggsFound)
                append("\n$WHITE${entry.rank}. $playerNameColor${entry.playerName} ${GRAY}found $foundEggsText")
            }
        }
    }, false)
    return Command.SINGLE_SUCCESS
}
