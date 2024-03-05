package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
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

    fun randomEggItem(): ItemStack = Items.PLAYER_HEAD.defaultStack.apply {
        setCustomName(Text.literal(EGG_ITEM_NAME).styled { style -> style.withItalic(false) })
        setSubNbt(EggHunt.MOD_NAME, NbtByte.ONE) // marker tag
        setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, generateRandomSkullOwner())
    }

    @JvmStatic
    fun tryPlaceEggAndUpdateItem(context: ItemPlacementContext) {
        val nbt = context.stack.nbt?.takeIf { it.getBoolean(EggHunt.MOD_NAME) } ?: return
        val player = context.player ?: return
        nbt.put(PlayerHeadItem.SKULL_OWNER_KEY, generateRandomSkullOwner())
        Database.createEggAtPos(context.blockPos, player.uuid)?.also { id ->
            player.sendMessage(EggHunt.MESSAGE_PREFIX.append("${Formatting.GREEN}Egg #$id placed!"))
        }
    }

    private fun generateRandomSkullOwner() = NbtCompound().apply {
        putString("Name", EGG_ITEM_NAME)
        put("Properties", NbtCompound().apply {
            put("textures", NbtList().apply {
                add(NbtCompound().apply {
                    config.eggPlacement.textures.randomOrNull()?.let { putString("Value", it) }
                })
            })
        })
    }
}
