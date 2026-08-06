package com.viniciuscoscia.kmpfullstackplayground.substance.atlas

import kotlin.test.Test
import kotlin.test.assertEquals

class AtlasPresentationTest {

    @Test
    fun dashboardLabelUsesTheSelectedLocale() {
        assertEquals("Visão geral", AtlasDestination.DASHBOARD.label("pt-BR"))
        assertEquals("Dashboard", AtlasDestination.DASHBOARD.label("en"))
    }
}
