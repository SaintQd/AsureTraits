package org.saintqd.asuretraits

import io.lumine.mythic.core.skills.CustomComponentRegistry
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.saintqd.asurelib.AsureLib
import org.saintqd.asurelib.utils.AsureUtils
import org.saintqd.asurelib.utils.ResourceUtils
import org.saintqd.asuretraits.commands.AsureTraitCommands
import org.saintqd.asuretraits.listeners.BindableActionListener
import org.saintqd.asuretraits.listeners.PlayerListener
import org.saintqd.asuretraits.managers.ActionTypeRegistrar
import org.saintqd.asuretraits.managers.TraitManager
import org.saintqd.asuretraits.storage.DataStorage
import org.saintqd.asuretraits.storage.MySqlJdbiStorage
import placeholders.AsureTraitsPlaceholders
import java.io.File
import java.util.concurrent.TimeUnit

class AsureTraits : JavaPlugin() {

    var storage : DataStorage? = null
    var loadFinished = false

    var mythicMobsEnabled = false
    var cmiEnabled = false

    companion object {
        private var plugin : AsureTraits? = null

        fun inst() : AsureTraits {
            return plugin!!
        }
    }

    override fun onLoad() {
        plugin = this

        ActionTypeRegistrar.registerFromPackage("org.saintqd.asuretraits.traits")
    }

    override fun onEnable() {
        loadFinished = true
        ResourceUtils.fetchAllResources(this, file)

        val cmi = Bukkit.getPluginManager().getPlugin("CMI")
        if (cmi != null && cmi.isEnabled()) {
            cmiEnabled = true
            AsureUtils.sendDebugMessage(0, "CMI found, compatibility features enabled.")
        }

        val placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
        if (placeholderAPI != null && placeholderAPI.isEnabled) {
            AsureTraitsPlaceholders.instance = AsureTraitsPlaceholders(this)
            AsureTraitsPlaceholders.instance?.registerPlaceholders()
            AsureTraitsPlaceholders.instance?.register()
        }

        val mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs")
        if (mythicMobs != null && mythicMobs.isEnabled) {
            mythicMobsEnabled = true
            AsureUtils.sendDebugMessage(0,"MythicMobs found, compatibility features enabled.")

            CustomComponentRegistry(this,"org.saintqd.asuretraits.mythicmobs")

            //val mythicMobsListener = MythicMobsListener()
            //mythicMobsListener.registerMechanics()
            //mythicMobsListener.registerConditions()
            //server.pluginManager.registerEvents(mythicMobsListener,this)
        }

        loadData()

        when(config.getString("Storage","mysql")!!) {
            "mysql" -> storage = MySqlJdbiStorage()
        }

        AsureTraitCommands.setupCommands(this)

        server.pluginManager.registerEvents(PlayerListener(), this)
        server.pluginManager.registerEvents(BindableActionListener(), this)

        server.asyncScheduler.runAtFixedRate(this, {
            saveData()
        }, 30L, 30L, TimeUnit.MINUTES)
    }

    override fun onDisable() {
        saveData()
    }

    fun saveData() {
        storage?.save()
        storage?.saveOnlinePlayersData()
        logger.info("Trait owners data saved.")
    }

    fun loadData() {
        reloadConfig()

        val selectedLang = getConfig().getString("Language")
        val langLines = AsureLib.inst().langManager.loadLanguageFile(
            this,
            dataFolder.path + File.separator + "lang" + File.separator + selectedLang + ".yml"
        )
        AsureLib.inst().langManager.registerLangLines(langLines)

        var prevTime = System.currentTimeMillis()
        TraitManager.instance.unregisterAllActions()
        TraitManager.instance.loadParams(this)
        var time = System.currentTimeMillis()
        logger.info("Loaded " + TraitManager.instance.traits.size + " traits (${TraitManager.instance.getActionsRegistrySize()} total actions). ("+(time-prevTime)+" ms)")
        prevTime = System.currentTimeMillis()

    }

}