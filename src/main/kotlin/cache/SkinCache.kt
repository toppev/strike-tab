package ga.strikepractice.striketab.cache

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.keenant.tabbed.util.Skins
import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.debug
import org.bukkit.Bukkit
import java.io.File
import java.lang.reflect.Type

private val MAP_TYPE: Type = object : TypeToken<Map<String?, String?>>() {}.type

@Suppress("UnstableApiUsage")
class SkinCache(plugin: StrikeTab) : FileCache(plugin, "skins.json") {

    private var loading = true

    override fun load(file: File) {
        if (!file.exists() || !plugin.config.getBoolean("cache.cache-offline-skins")) return
        val st = System.currentTimeMillis()
        try {
            debug { "Loading skins from the cache file at ${file.absolutePath}" }
            val map: Map<String, String>? = Gson().fromJson(file.bufferedReader(), MAP_TYPE)
            if (map != null) {
                Skins.getProfileCache().putAll(map)
            }
            Bukkit.getLogger().info("Done loading (${map?.size ?: 0}) skins from the cache file in ${System.currentTimeMillis() - st} ms.")
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to load skin cache. Deleting the file to invalidate cache automatically.")
            e.printStackTrace()
            if (file.exists()) file.delete()
        } finally {
            loading = false
        }
    }

    override fun save(file: File) {
        if (loading) {
            Bukkit.getLogger().warning("Not saving cache to ${file.absolutePath}: cache was not loaded before attempting to save it.")
            return
        }
        val st = System.currentTimeMillis()
        val data = Skins.getProfileCache().asMap()
        debug { "Saving ${data.size} skins to cache file at ${file.absolutePath}" }
        file.bufferedWriter().use {
            GsonBuilder().setPrettyPrinting().create().toJson(data, it)
        }
        debug { "Done saving skins in the cache file in ${System.currentTimeMillis() - st} ms." }
    }

}
