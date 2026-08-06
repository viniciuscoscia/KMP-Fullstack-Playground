package com.viniciuscoscia.kmpfullstackplayground.foregroundservices

import org.junit.Assert.assertEquals
import org.junit.Test

class ElapsedTimeFormatterTest {

    @Test
    fun `formats elapsed seconds as zero-padded minutes and seconds`() {
        assertEquals("00:00", formatElapsed(totalSeconds = 0))
        assertEquals("01:05", formatElapsed(totalSeconds = 65))
        assertEquals("10:00", formatElapsed(totalSeconds = 600))
    }
}
