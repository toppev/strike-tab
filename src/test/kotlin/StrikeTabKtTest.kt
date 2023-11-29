package ga.strikepractice.striketab

import kotlin.test.Test
import kotlin.test.assertEquals

internal class StrikeTabKtTest {

    @Test
    fun translateColors() {
        val res = "asd&a&8".translateColors()
        assertEquals("asd§a§8", res)
    }

    @Test
    fun translateHexColors() {
        val res = "asd#ffffff123 another color:#40d1db".translateColors()
        assertEquals("asd§x§f§f§f§f§f§f123 another color:§x§4§0§d§1§d§b", res)
    }
}