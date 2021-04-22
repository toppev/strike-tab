package ga.strikepractice.striketab.cache

import ga.strikepractice.striketab.StrikeTab
import java.io.File

abstract class FileCache(
    private val plugin: StrikeTab,
    private val fileName: String
) {

    fun getFile(create: Boolean = true): File {
        val dir = File(plugin.dataFolder, "cache")
        if (create) dir.mkdirs()
        return File(dir, fileName).also {
            if (create) it.createNewFile()
        }
    }

    abstract fun load(file: File = getFile())

    abstract fun save(file: File = getFile())

}