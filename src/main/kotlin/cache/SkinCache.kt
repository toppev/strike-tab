package ga.strikepractice.striketab.cache

import com.google.gson.Gson
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
        val st = System.currentTimeMillis()
        debug { "Loading skins from the cache file at ${file.absolutePath}" }
        val map: Map<String, String> = Gson().fromJson(file.bufferedReader(), MAP_TYPE)
        Skins.getProfileCache().putAll(map)
        debug { "Done loading skins from the cache file in ${System.currentTimeMillis() - st} ms." }
        loading = false
    }

    override fun save(file: File) {
        if (loading) {
            Bukkit.getLogger().warning("Not saving cache to ${file.absolutePath}: cache was not loaded before attempting to save it.")
            return
        }
        val st = System.currentTimeMillis()
        val data = Skins.getProfileCache().asMap()
        debug { "Saving ${data.size} skins to cache file at ${file.absolutePath}" }
        Gson().toJson(data, file.bufferedWriter())
        debug { "Done saving skins in the cache file in ${System.currentTimeMillis() - st} ms." }
    }

}