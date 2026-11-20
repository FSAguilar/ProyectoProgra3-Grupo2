package com.example.vamospues.ui.screens.admin

import com.example.vamospues.data.FakeData
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminUiStateTest {
    @Test
    fun `solo conserva lobbies cuyo ultimo analisis esta pendiente`() {
        val contenido = contenidoAdmin(FakeData.reportes, FakeData.lobbies, FakeData.filtros)

        assertEquals(listOf("lobby-8"), contenido.lobbiesPendientes.map { it.id })
    }

    @Test
    fun `clasifica el nivel de riesgo para presentacion`() {
        assertEquals("bajo", nivelRiesgo(0))
        assertEquals("medio", nivelRiesgo(1))
        assertEquals("alto", nivelRiesgo(2))
    }
}
