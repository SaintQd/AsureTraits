package org.saintqd.asuretraits.traits

import org.bukkit.configuration.ConfigurationSection
import org.saintqd.asurelib.utils.MMAbilityData
import org.saintqd.asuretraits.annotations.AsureTraitType
import org.saintqd.asuretraits.managers.TraitOwner

@AsureTraitType("mm_skill")
class MMSkillAction(name : String, config : ConfigurationSection) : TraitAction(name,config) {

    private val skillName = config.getString("SkillName","asuretrait_$name")!!

    companion object {
        const val NAME = "mm_skill"
    }

    override var executeFunction : (TraitOwner) -> Boolean = Function@ { traitOwner ->
        if (canExecute(traitOwner)) {
            val skillMetadata = MMAbilityData.prepareMMSkillData(traitOwner.player)
            return@Function MMAbilityData.executeMMSkill(skillName,skillMetadata)
        }
        else
            return@Function false
    }
}