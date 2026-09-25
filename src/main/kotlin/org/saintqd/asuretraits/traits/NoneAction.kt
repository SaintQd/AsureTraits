package org.saintqd.asuretraits.traits

import org.bukkit.configuration.ConfigurationSection
import org.saintqd.asuretraits.annotations.AsureTraitType

@AsureTraitType("none")
class NoneAction(name : String, config : ConfigurationSection) : TraitAction(name,config) {

    companion object {
        const val NAME = "none"
    }
}