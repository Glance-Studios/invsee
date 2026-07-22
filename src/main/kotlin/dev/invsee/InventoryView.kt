package dev.invsee

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.server.level.ServerPlayer

/**
 * A 6-row chest-shaped [Container] that is a live view over [target]'s real inventory: reads and
 * writes delegate straight through, so edits land on the target immediately.
 *
 * Chest slot to target index: rows 0-2 (0-26) storage (9-35), row 3 (27-35) hotbar (0-8),
 * row 4 (36-40) armor and offhand (36-40), the rest inert filler.
 */
class InventoryView(private val target: ServerPlayer) : Container {

    /** chest slot -> target inventory index, or -1 for a filler slot. */
    private val slotMap = IntArray(SIZE) { -1 }

    init {
        for (c in 0..26) slotMap[c] = 9 + c        // storage
        for (c in 0..8) slotMap[27 + c] = c        // hotbar
        for (c in 0..4) slotMap[36 + c] = 36 + c   // armor (36-39) + offhand (40)
    }

    fun isRealSlot(slot: Int): Boolean = slot in slotMap.indices && slotMap[slot] >= 0

    private fun target(slot: Int): Int = if (slot in slotMap.indices) slotMap[slot] else -1

    override fun getContainerSize(): Int = SIZE

    override fun isEmpty(): Boolean = false // filler is always present; cheap and correct enough

    override fun getItem(slot: Int): ItemStack {
        val t = target(slot)
        return if (t < 0) FILLER else target.inventory.getItem(t)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val t = target(slot)
        return if (t < 0) ItemStack.EMPTY else target.inventory.removeItem(t, amount)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        val t = target(slot)
        return if (t < 0) ItemStack.EMPTY else target.inventory.removeItemNoUpdate(t)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        val t = target(slot)
        if (t >= 0) target.inventory.setItem(t, stack)
    }

    override fun setChanged() {
        target.inventory.setChanged()
    }

    /** Keep the view valid while the target is still on the server; the menu closes if they leave. */
    override fun stillValid(player: Player): Boolean = !target.hasDisconnected()

    /** Never wipe the target's inventory when the menu closes. */
    override fun clearContent() { /* no-op on purpose */ }

    companion object {
        const val SIZE = 54
        /** A blank, non-interactive placeholder shown in the unused chest slots. */
        private val FILLER: ItemStack = ItemStack(Items.STAINED_GLASS_PANE.gray())
    }
}
