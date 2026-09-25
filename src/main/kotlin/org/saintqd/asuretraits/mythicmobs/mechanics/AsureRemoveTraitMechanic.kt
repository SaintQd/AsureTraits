package org.saintqd.asuretraits.mythicmobs.mechanics

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.utils.annotations.MythicMechanic
import org.bukkit.entity.Player
import org.saintqd.asurelib.utils.AsureUtils
import org.saintqd.asuretraits.managers.TraitManager

@MythicMechanic(author = "SaintQd", name = "asureremovetrait")
class AsureRemoveTraitMechanic(event : MythicMechanicLoadEvent) : ITargetedEntitySkill {

    val traitName : String = event.config.getString(arrayOf("trait", "t"),"")

    override fun castAtEntity(
        data: SkillMetadata,
        target: AbstractEntity
    ): SkillResult {
        AsureUtils.sendDebugMessage(3,"MythicMobsMechanic: asureremovetrait")

        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is Player)
            return SkillResult.INVALID_TARGET

        val traitOwner = TraitManager.instance.traitOwners[bukkitEntity.uniqueId] ?: return SkillResult.INVALID_TARGET
        val trait = TraitManager.instance.traits[traitName] ?: return SkillResult.INVALID_CONFIG
        traitOwner.removeTrait(trait)
        return SkillResult.SUCCESS
    }
}