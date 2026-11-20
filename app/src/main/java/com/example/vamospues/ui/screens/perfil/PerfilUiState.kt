package com.example.vamospues.ui.screens.perfil

import com.example.vamospues.model.Lobby
import com.example.vamospues.model.Usuario
import com.example.vamospues.model.enums.EstadoCuenta

sealed interface PerfilUiState {
    data class Contenido(
        val usuario: Usuario,
        val historial: List<Lobby>,
    ) : PerfilUiState {
        val estadoCuenta: EstadoCuenta get() = usuario.estado

        val sinHistorial: Boolean
            get() = estadoCuenta == EstadoCuenta.ACTIVA && historial.isEmpty()
    }

    data object Cargando : PerfilUiState

    data object Error : PerfilUiState
}
