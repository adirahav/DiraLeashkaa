package com.adirahav.diraleashkaa

import com.adirahav.diraleashkaa.common.Utilities
import org.testng.AssertJUnit.assertTrue
import org.testng.annotations.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SignUpUnitTest {
    /*@Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }*/
    @Test
    fun phoneValidator_CorrectEmailSimple_ReturnsTrue() {
        assertTrue(Utilities.isPhoneValid("0546666666"))
    }
}