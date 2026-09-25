package org.saintqd.asuretraits.listeners

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.kyori.adventure.util.TriState
import org.bukkit.Material
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.saintqd.asurelib.utils.AsureUtils
import org.saintqd.asuretraits.enums.InteractionType
import org.saintqd.asuretraits.managers.TraitManager
import org.saintqd.asuretraits.managers.TraitOwner
import org.saintqd.asuretraits.traits.BindableAction
import java.util.UUID
import kotlin.collections.set

class BindableActionListener : Listener {

    private val executedEventsMap = hashMapOf<UUID, Long>()

    fun executeInteraction(traitOwner: TraitOwner, interactionType: InteractionType, cancellableEvent : Cancellable) {
        if (traitOwner.player.permissionValue("asuretraits.interactdisabled") == TriState.TRUE) return

        val isSneaking = traitOwner.player.isSneaking
        val isEmptyHand = traitOwner.player.inventory.itemInMainHand.type == Material.AIR

        val possibleTraits = traitOwner.bindedTraits.filter { entry ->
            val bindData = entry.value
            if (!traitOwner.traits.contains(entry.key))
                return@filter false
            if (interactionType != bindData.interactionType)
                return@filter false
            if (bindData.requireSneaking && !isSneaking)
                return@filter false
            if (!bindData.requireSneaking && isSneaking)
                return@filter false
            if (bindData.requireEmptyHand && !isEmptyHand)
                return@filter false
            return@filter true
        }.keys

        if (possibleTraits.isEmpty()) return
        for (traitName in possibleTraits) {
            val action = TraitManager.instance.actionsRegistry[traitName] ?: continue
            if (action is BindableAction) {
                if (!action.isBindable())
                    continue
                TraitManager.instance.executeAction(traitName,traitOwner)
                cancellableEvent.isCancelled = action.shouldCancelEvent()
            }
        }
    }

    @EventHandler
    fun onPlayerSwapHands(event : PlayerSwapHandItemsEvent) {

        val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
        executeInteraction(traitOwner,InteractionType.SWAP_HANDS,event)

    }

    @EventHandler
    fun onPlayerClick(event : PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND)
            return

        if (executedEventsMap.getOrDefault(event.player.uniqueId, 0L)
            == AsureUtils.getCurrentTick()) return

        val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
        if (event.action.isLeftClick)
            executeInteraction(traitOwner,InteractionType.LEFT_CLICK,event)
        else if (event.action.isRightClick)
            executeInteraction(traitOwner,InteractionType.RIGHT_CLICK,event)

        executedEventsMap[event.player.uniqueId] = AsureUtils.getCurrentTick()
    }

    @EventHandler
    fun onPlayerItemDrop(event : PlayerDropItemEvent) {

        val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
        executeInteraction(traitOwner,InteractionType.DROP,event)

        executedEventsMap[event.player.uniqueId] = AsureUtils.getCurrentTick()
    }

    @EventHandler
    fun onPlayerJump(event : PlayerJumpEvent) {

        val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
        executeInteraction(traitOwner,InteractionType.JUMP,event)
    }
}