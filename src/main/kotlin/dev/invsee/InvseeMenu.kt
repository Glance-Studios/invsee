package dev.invsee

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

/**
 * A 6-row chest menu whose top 54 slots are backed by an [InventoryView] over another player. Real
 * slots accept pickup and place only when [canEdit] is true; filler slots are always locked.
 * Shift-click returns EMPTY, so items cannot be dumped into filler or unexpected slots.
 */
class InvseeMenu(
    containerId: Int,
    playerInv: Inventory,
    private val view: InventoryView,
    private val canEdit: Boolean,
) : AbstractContainerMenu(MenuType.GENERIC_9x6, containerId) {

    init {
        // Top: the 54 view slots.
        for (row in 0 until 6) {
            for (col in 0 until 9) {
                val idx = row * 9 + col
                addSlot(object : Slot(view, idx, 8 + col * 18, 18 + row * 18) {
                    override fun mayPickup(player: Player): Boolean = canEdit && view.isRealSlot(idx)
                    override fun mayPlace(stack: ItemStack): Boolean = canEdit && view.isRealSlot(idx)
                })
            }
        }

        // Bottom: the viewer's own inventory + hotbar, positioned as vanilla does for a 6-row chest.
        val yOff = (6 - 4) * 18
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + yOff))
            }
        }
        for (col in 0 until 9) {
            addSlot(Slot(playerInv, col, 8 + col * 18, 161 + yOff))
        }
    }

    override fun stillValid(player: Player): Boolean = view.stillValid(player)

    /** Disable shift-click transfers. */
    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY
}
