package placeholders

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.saintqd.asuretraits.AsureTraits
import java.util.*

class AsureTraitsPlaceholders(val plugin : AsureTraits) : PlaceholderExpansion() {

    companion object {
        var instance : AsureTraitsPlaceholders? = null
    }

    private val placeholders = hashMapOf<String, (AsureTraits, OfflinePlayer) -> String>()

    override fun persist(): Boolean {
        return true
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun getIdentifier(): String {
        return "asuretraits"
    }

    override fun getAuthor(): String {
        return plugin.pluginMeta.authors.toString()
    }

    override fun getVersion(): String {
        return plugin.pluginMeta.version
    }

    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        return if (player == null) {
            ""
        } else placeholders[identifier.lowercase(Locale.getDefault())]?.invoke(plugin,player)
    }

    fun registerPlaceholders() {

    }

    fun registerPlaceholder(identifier: String, function : (AsureTraits, OfflinePlayer) -> String) {
        placeholders[identifier] = function
    }
}