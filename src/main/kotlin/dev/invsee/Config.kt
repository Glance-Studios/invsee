package dev.invsee

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/** User-editable config, stored at `config/invsee.json`. */
data class Config(
    // LuckPerms-style permission node required to use /invsee and the [inv] chat click.
    var permissionNode: String = "invsee.use",
    // Node required to force ANOTHER player to open a view: /invsee show <viewer> <target>.
    var showPermissionNode: String = "invsee.show",
    // Fallback op level (2 = gamemasters) checked when no permission mod is installed.
    var defaultOpLevel: Int = 2,
    // Whether the viewer can move/edit items in the target's inventory (edits apply live).
    var allowEdit: Boolean = true,
    // GUI title. `%s` = the target player's name. `&` colour codes allowed.
    var titleFormat: String = "&8%s's Inventory",
) {
    companion object {
        private val LOG = LoggerFactory.getLogger("invsee")
        private val GSON = GsonBuilder().setPrettyPrinting().create()

        private fun path(): Path = FabricLoader.getInstance().configDir.resolve("invsee.json")

        fun load(): Config {
            val p = path()
            return try {
                if (Files.exists(p)) {
                    val cfg = Files.newBufferedReader(p).use { GSON.fromJson(it, Config::class.java) }
                        ?: Config()
                    cfg.save()
                    cfg
                } else {
                    Config().also {
                        it.save()
                        LOG.info("Wrote default config to {}", p)
                    }
                }
            } catch (e: Exception) {
                LOG.error("Failed to read {}, using defaults.", p, e)
                Config()
            }
        }

        private fun Config.save() {
            val p = path()
            try {
                Files.createDirectories(p.parent)
                Files.newBufferedWriter(p).use { GSON.toJson(this, it) }
            } catch (e: Exception) {
                LOG.error("Failed to write {}", p, e)
            }
        }
    }
}
