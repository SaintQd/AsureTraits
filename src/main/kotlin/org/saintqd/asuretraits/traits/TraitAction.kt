package org.saintqd.asuretraits.traits

import org.bukkit.configuration.ConfigurationSection
import org.saintqd.asurelib.utils.AsureUtils
import org.saintqd.asuretraits.managers.TraitOwner

abstract class TraitAction(val traitName: String, config : ConfigurationSection) {

    var checkIfPresent = true
    var cooldown = 0

    open var executeFunction : (TraitOwner) -> Boolean = Function@ { traitOwner ->
        return@Function canExecute(traitOwner)
    }

    init {
        checkIfPresent = config.getBoolean("CheckIfPresent",true)
        cooldown = config.getInt("Cooldown",0)
    }

    fun canExecute(owner: TraitOwner): Boolean {
        owner.cooldowns[traitName]?.let { cooldown ->
            if (AsureUtils.getCurrentTick() < cooldown)
                return false
        }
        return !(checkIfPresent && !owner.traits.contains(traitName))
    }

    fun applyCooldown(traitOwner: TraitOwner) {
        if (cooldown > 0) {
            traitOwner.cooldowns[traitName] = AsureUtils.getCurrentTick() + cooldown
        }
    }

    open fun register() {
    }

    open fun unregister() {

    }

    open fun onLoad(owner: TraitOwner) {

    }

    open fun onUnload(owner: TraitOwner) {

    }

    open fun onAdd(owner: TraitOwner) {

    }

    open fun onRemove(owner: TraitOwner) {

    }
}