package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.EggHunt
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.PlayerHeadItem
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object EggPlacer {
    private const val EGG_ITEM_NAME = "Easter Egg"

    /**
     * A set of Base64-encoded JSON objects with the following format:
     * ```
     * {
     *     "textures": {
     *         "SKIN": {
     *             "url": "http://textures.minecraft.net/texture/{id}"
     *         }
     *     }
     * }
     * ```
     **/
    private val EGG_TEXTURES = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWViMzM1MTgyZGI1ZjNiZTgwZmNjZjZlYWJlNTk5ZjQxMDdkNGZmMGU5ZjQ0ZjM0MTc0Y2VmYTZlMmI1NzY4In19fQ==",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjJjZDVkZjlkN2YxZmE4MzQxZmNjZTJmM2MxMThlMmY1MTdlNGQyZDk5ZGYyYzUxZDYxZDkzZWQ3ZjgzZTEzIn19fQ==",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc2NTk1ZWZmY2M1NjI3ZTg1YjE0YzljODgyNDY3MWI1ZWMyOTY1NjU5YzhjNDE3ODQ5YTY2Nzg3OGZhNDkwIn19fQ==",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGViNWFiYjEyMzUyN2E3YTFmNWM5NTg5NzE1OTY0YjU5Zjc2ODI0OTI2ZDNiOTgyZmE4NDExZDQ2MDZjNzkifX19",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjNkNjliMjNhZTU5MmM2NDdlYjhkY2ViOWRhYWNlNDQxMzlmNzQ4ZTczNGRjODQ5NjI2MTNjMzY2YTA4YiJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY0NDMwZTQ5M2ZlYjVlYWExNDU1ODJlNTRlNzYxYTg2MDNmYjE2Y2MwZmYxMjY4YTVkMWU4NjRlNmY0NzlmNiJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjU2ZjdmM2YzNTM2NTA2NjI2ZDVmMzViNDVkN2ZkZjJkOGFhYjI2MDA4NDU2NjU5ZWZlYjkxZTRjM2E5YzUifX19",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThiOWUyOWFiMWE3OTVlMmI4ODdmYWYxYjFhMzEwMjVlN2NjMzA3MzMzMGFmZWMzNzUzOTNiNDVmYTMzNWQxIn19fQ==",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTViOGRjYmVhMjdmNDJmNWFlOTEwNDQ1ZTA1ZGFjODlkMzEwYWFmMjM2YTZjMjEyM2I4NTI4MTIwIn19fQ==",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI4YjJiZGZjNWQxM2I5ZjQ1OTkwZWUyZWFhODJlNDRhZmIyYTY5YjU2YWM5Mjc2YTEzMjkyYjI0YzNlMWRlIn19fQ==",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTg4OWYxMWM4ODM4YzA5ZTFlY2YyZjgzNDM5ZWJjYjlmMzI0ZTU2N2IwZTlkYzRiN2MyNWQ5M2U1MGZmMmIifX19",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM2ZmU4M2M0MmM2Y2U3Mzg2Y2YyMzMzYTFjNTk1ZDBiNDMzZGE3YmM1NTkyYjg2ODY2ODU1MWQ2OWI5YjAifX19",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDY0MmFmYTM5Njg1M2I4MWIxN2JlZjVjOGQ3YTQ0YzEyZGU2ODlhNTZhZjQ3NDg0NjY3OTgzOTlkYTNjZmVhZSJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWYzMjlmMWI0NDhlYWFiMmY5ZjA0NDZmYzBiMjMxZWI3NzUxMzczYWZlNDc0MjZlNDk1MGY3ZDA4NjIwZjA4ZCJ9fX0=",
    )

    fun getEggItem(): ItemStack = Items.PLAYER_HEAD.defaultStack.apply {
        setCustomName(Text.literal(EGG_ITEM_NAME).styled { style -> style.withItalic(false) })
        setSubNbt(EggHunt.MOD_NAME, NbtByte.ONE) // marker tag
        setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, generateRandomSkullOwner())
    }

    @JvmStatic
    fun placeEggAndUpdateItem(context: ItemPlacementContext) {
        val nbt = context.stack.nbt?.takeIf { it.getBoolean(EggHunt.MOD_NAME) } ?: return
        val player = context.player ?: return
        nbt.put(PlayerHeadItem.SKULL_OWNER_KEY, generateRandomSkullOwner())
        Database.createEggAtPos(context.blockPos, player.uuid)?.also { id ->
            val message = Text.literal("Egg #$id placed!").formatted(Formatting.GREEN)
            player.sendMessage(EggHunt.MESSAGE_PREFIX.append(message))
        }
    }

    private fun generateRandomSkullOwner() = NbtCompound().apply {
        putString("Name", EGG_ITEM_NAME)
        put("Properties", NbtCompound().apply {
            put("textures", NbtList().apply {
                add(NbtCompound().apply {
                    putString("Value", EGG_TEXTURES.random())
                })
            })
        })
    }
}
