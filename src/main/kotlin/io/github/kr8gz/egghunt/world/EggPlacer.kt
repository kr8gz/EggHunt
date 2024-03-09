package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
import io.github.kr8gz.egghunt.plus
import me.lucko.fabric.api.permissions.v0.Permissions
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
    private val EGG_ITEM_NAME = Text.translatable("egghunt.item.name")

    fun randomEggItem(): ItemStack = Items.PLAYER_HEAD.defaultStack.apply {
        setCustomName(EGG_ITEM_NAME.styled { style -> style.withItalic(false) })
        setSubNbt(EggHunt.MOD_NAME, NbtByte.ONE) // marker tag
        setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, generateRandomSkullOwner())
    }

    @JvmStatic
    fun tryRegisterEggAndUpdateItem(context: ItemPlacementContext) {
        val nbt = context.stack.nbt?.takeIf { it.getBoolean(EggHunt.MOD_NAME) } ?: return
        val player = context.player ?: return
        if (!Permissions.check(player, EggHunt.Permissions.PLACE, config.defaultPermissionLevel)) return

        nbt.put(PlayerHeadItem.SKULL_OWNER_KEY, generateRandomSkullOwner())
        Database.Eggs.create(context.blockPos within context.world, player).also { id ->
            player.sendMessage(EggHunt.MESSAGE_PREFIX + Text.translatable("egghunt.egg.placed", id).formatted(Formatting.GREEN))
        }
    }

    private fun generateRandomSkullOwner() = NbtCompound().apply {
        putString("Name", EGG_ITEM_NAME.string)
        put("Properties", NbtCompound().apply {
            put("textures", NbtList().apply {
                add(NbtCompound().apply {
                    config.eggPlacement.textures.randomOrNull()?.let { putString("Value", it) }
                })
            })
        })
    }
}
