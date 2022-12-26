package calculator

import junit.framework.TestCase.assertEquals
import org.junit.Test


internal class CalculatorTest {
    @Test
    fun `adding 2 and -3 should return -1`() {
        val result = Calculator().processInput("2 + -3")
        assertEquals("-1", result)
    }
    @Test
    fun `multiple pluses and minuses`() {
        val result = Calculator().processInput("2 -- 3 --- 5 ---- 8 +++++ 13")
        assertEquals("21", result)
    }
    @Test
    fun `invalid expression`() {
        val result = Calculator().processInput("123+")
        assertEquals(Calculator.Const.INVALID_EXPRESSION, result)
    }
    @Test
    fun `process should ignore blank spaces`() {
        val result = Calculator().processInput("         -99    ")
        assertEquals("-99", result)
    }
    @Test
    fun `process invalid identifier`() {
        val result = Calculator().processInput("a2a")
        assertEquals(Calculator.Const.INVALID_IDENTIFIER, result)
    }
    /*@Disabled
    @Test
    fun `division by 0 throws IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            Calculator().processExpression("1 / 0")
        }
    }*/
}