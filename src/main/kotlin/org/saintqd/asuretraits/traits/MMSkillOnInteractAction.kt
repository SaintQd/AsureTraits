package org.saintqd.asuretraits.traits

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.lumine.mythic.api.adapters.AbstractEntity
import net.kyori.adventure.util.TriState
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.saintqd.asurelib.utils.AsureUtils
import org.saintqd.asurelib.utils.MMAbilityData
import org.saintqd.asuretraits.AsureTraits
import org.saintqd.asuretraits.managers.TraitManager
import org.saintqd.asuretraits.annotations.AsureTraitType
import org.saintqd.asuretraits.enums.InteractionType
import org.saintqd.asuretraits.managers.TraitOwner
import java.util.UUID

@AsureTraitType("mm_skill_on_interact")
class MMSkillOnInteractAction(name : String, config : ConfigurationSection) : TraitAction(name,config), BindableAction {

    private val interactionType = InteractionType.valueOf(config.getString("InteractionType","RIGHT_CLICK")!!.uppercase())
    private val requiresSneaking = config.getBoolean("Sneaking",false)
    private val requiresSprinting = config.getBoolean("Sprinting",false)
    private val requiresEmptyHand = config.getBoolean("RequiresEmptyHand",false)
    private val skillName = config.getString("SkillName","asuretrait_$name")!!
    private val cancelEvent = config.getBoolean("CancelEvent", true)
    private val bindable = config.getBoolean("Bindable", true)

    override fun isBindable(): Boolean {
        return bindable
    }

    override fun shouldCancelEvent(): Boolean {
        return cancelEvent
    }

    override var executeFunction : (TraitOwner) -> Boolean = Function@ { traitOwner ->
        if (canExecute(traitOwner) && skillName.isNotEmpty()) {
            val skillMetadata = MMAbilityData.prepareMMSkillData(traitOwner.player)
            return@Function MMAbilityData.executeMMSkill(skillName,skillMetadata)
        }
        return@Function false
    }

    override fun register() {
        if (actionsMap.isEmpty())
            Bukkit.getServer().pluginManager.registerEvents(listener, AsureTraits.inst())
        actionsMap[traitName] = this
    }

    override fun unregister() {
        actionsMap.remove(traitName)
        if (actionsMap.isEmpty())
            HandlerList.unregisterAll(listener)
    }

    companion object {
        const val NAME = "mm_skill_on_interact"
        private val actionsMap = hashMapOf<String, MMSkillOnInteractAction>()
        private val executedEventsMap = hashMapOf<UUID, Long>()

        private val listener = object : Listener {

            @EventHandler
            fun onPlayerSwapHands(event : PlayerSwapHandItemsEvent) {
                if (event.player.permissionValue("asuretraits.interactdisabled") == TriState.TRUE) return
                val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
                val commonTraits = traitOwner.traits intersect actionsMap.keys
                if (commonTraits.isEmpty()) return
                for (traitName in commonTraits) {
                    actionsMap[traitName]?.let { action ->
                        if (action.bindable)
                            continue
                        if (action.interactionType == InteractionType.SWAP_HANDS) {
                            if (action.requiresSneaking && !event.player.isSneaking)
                                continue
                            if (!action.requiresSneaking && event.player.isSneaking)
                                continue
                            if (action.requiresSprinting && !event.player.isSprinting)
                                continue
                            if (action.requiresEmptyHand && event.player.inventory.itemInMainHand.type != Material.AIR)
                                continue
                            if (TraitManager.instance.executeAction(traitName, traitOwner))
                                event.isCancelled = action.cancelEvent
                        }
                    }
                }
            }



            @EventHandler
            fun onPlayerClick(event : PlayerInteractEvent) {
                if (event.hand != EquipmentSlot.HAND)
                    return
                if (event.player.permissionValue("asuretraits.interactdisabled") == TriState.TRUE) return
                val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
                val commonTraits = traitOwner.traits intersect actionsMap.keys
                if (commonTraits.isEmpty()) return
                if (executedEventsMap.getOrDefault(event.player.uniqueId, 0L)
                    == AsureUtils.getCurrentTick()) return
                for (traitName in commonTraits) {
                    actionsMap[traitName]?.let { action ->
                        if (action.bindable)
                            continue
                        if (action.requiresSneaking && !event.player.isSneaking)
                            continue
                        if (!action.requiresSneaking && event.player.isSneaking)
                            continue
                        if (action.requiresSprinting && !event.player.isSprinting)
                            continue
                        if (action.requiresEmptyHand && event.player.inventory.itemInMainHand.type != Material.AIR)
                            continue
                        if (event.action.isLeftClick) {
                            if (action.interactionType == InteractionType.LEFT_CLICK) {
                                if (TraitManager.instance.executeAction(traitName,traitOwner))
                                    event.isCancelled = action.cancelEvent
                            }
                        }
                        else if (event.action.isRightClick) {
                            if (action.interactionType == InteractionType.RIGHT_CLICK) {
                                if (TraitManager.instance.executeAction(traitName,traitOwner))
                                    event.isCancelled = action.cancelEvent
                            }
                        }
                    }
                }
                executedEventsMap[event.player.uniqueId] = AsureUtils.getCurrentTick()
            }

            @EventHandler
            fun onPlayerItemDrop(event : PlayerDropItemEvent) {
                if (event.player.permissionValue("asuretraits.interactdisabled") == TriState.TRUE) return
                val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
                val commonTraits = traitOwner.traits intersect actionsMap.keys
                if (commonTraits.isEmpty()) return
                for (traitName in commonTraits) {
                    actionsMap[traitName]?.let { action ->
                        if (action.bindable)
                            continue
                        if (action.interactionType == InteractionType.DROP) {
                            if (action.requiresSneaking && !event.player.isSneaking)
                                continue
                            if (!action.requiresSneaking && event.player.isSneaking)
                                continue
                            if (action.requiresSprinting && !event.player.isSprinting)
                                continue
                            if (action.requiresEmptyHand && event.player.inventory.itemInMainHand.type != Material.AIR)
                                continue
                            if (TraitManager.instance.executeAction(traitName,traitOwner))
                                event.isCancelled = action.cancelEvent
                        }
                    }
                }
                executedEventsMap[event.player.uniqueId] = AsureUtils.getCurrentTick()
            }

            @EventHandler
            fun onPlayerJump(event : PlayerJumpEvent) {
                if (event.player.permissionValue("asuretraits.interactdisabled") == TriState.TRUE) return
                val traitOwner = TraitManager.instance.traitOwners[event.player.uniqueId] ?: return
                val commonTraits = traitOwner.traits intersect actionsMap.keys
                if (commonTraits.isEmpty()) return
                for (traitName in commonTraits) {
                    actionsMap[traitName]?.let { action ->
                        if (action.bindable)
                            continue
                        if (action.interactionType == InteractionType.JUMP) {
                            if (action.requiresSneaking && !event.player.isSneaking)
                                continue
                            if (!action.requiresSneaking && event.player.isSneaking)
                                continue
                            if (action.requiresSprinting && !event.player.isSprinting)
                                continue
                            if (action.requiresEmptyHand && event.player.inventory.itemInMainHand.type != Material.AIR)
                                continue
                            if (TraitManager.instance.executeAction(traitName,traitOwner))
                                event.isCancelled = action.cancelEvent
                        }
                    }
                }
            }
        }
    }
}