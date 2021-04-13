package ga.strikepractice.striketab.layout


data class TabLayout(
    val slots: List<TabSlot>,
    val header: String?,
    val footer: String?,
) {

    companion object {
        fun parse(rawLines: List<String>, header: String?, footer: String?) =
            TabLayout(rawLines.map { TabSlot.fromString(it) }, header, footer)
    }

}

enum class TabLayoutType {
    DEFAULT,
    IN_MATCH,
}

data class TabSlot(
    val text: String,
    val skin: String?,
    val ping: Int = 0
) {

    companion object {

        private const val DEFAULT_PING = 0

        fun fromString(str: String): TabSlot {
            // A stupid way to make skin & ping configurable
            val skin = str.substringAfter("skin=", "").substringBefore(" ")
            val ping = str.substringAfter("ping=").substringBefore(" ")
            val text = str.replace("skin=$skin", "").replace("ping=$ping", "")
            return TabSlot(
                text = text,
                skin = skin.ifBlank { null },
                ping = ping.toIntOrNull() ?: DEFAULT_PING
            )
        }

    }

}