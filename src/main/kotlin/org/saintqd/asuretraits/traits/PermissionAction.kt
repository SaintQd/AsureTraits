package org.saintqd.asuretraits.traits

import org.bukkit.configuration.ConfigurationSection
import org.saintqd.asurelib.AsureLib
import org.saintqd.asuretraits.annotations.AsureTraitType
import org.saintqd.asuretraits.managers.TraitOwner

@AsureTraitType("permission")
class PermissionAction(name : String, config : ConfigurationSection) : TraitAction(name,config) {

    private val permissions = config.getStringList("Permissions")

    override fun onAdd(owner: TraitOwner) {
        AsureLib.inst().vaultManager?.permissionProvider?.let { permissionProvider ->
            permissions.forEach { permission ->
                permissionProvider.playerAdd(null,owner.player,permission)
            }
        }
    }

    override fun onRemove(owner: TraitOwner) {
        AsureLib.inst().vaultManager?.permissionProvider?.let { permissionProvider ->
            permissions.forEach { permission ->
                permissionProvider.playerRemove(null,owner.player,permission)
            }
        }
    }

    companion object {
        const val NAME = "permission"
    }
}