package com.example.vamospues.ui.screens.mislobbies

import com.example.vamospues.data.FakeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MisLobbiesScreenTest {
    @Test
    fun `incluye lobby 8 entre los creados por el usuario actual`() {
        val state = misLobbiesDe(FakeData.usuarioActual, FakeData.lobbies)

        assertEquals(listOf("lobby-8"), state.creados.map { it.id })
        assertTrue(state.participaciones.none { it.id == "lobby-8" })
    }

    @Test
    fun `elimina lobbies duplicados de la fuente`() {
        val lobby = FakeData.lobbies.first { it.id == "lobby-8" }
        val state = misLobbiesDe(FakeData.usuarioActual, FakeData.lobbies + lobby)

        assertEquals(1, state.creados.count { it.id == "lobby-8" })
    }

    @Test
    fun `sin relaciones devuelve ambas listas vacias`() {
        val state = misLobbiesDe(FakeData.usuarioActual, emptyList())

        assertTrue(state.creados.isEmpty())
        assertTrue(state.participaciones.isEmpty())
    }
}
