package com.example.vamospues.ui.screens.reportar

/** Motivos visibles en la hoja; son texto de presentación, no se envían al dominio en la demo. */
enum class MotivoReporte(val texto: String) {
    ACTIVIDAD_PELIGROSA("Actividad dañina o peligrosa"),
    ACOSO("Acoso o discriminación"),
    CONTENIDO_INAPROPIADO("Contenido inapropiado"),
    OTRO("Otro motivo"),
}

enum class EnvioReporte { EDITANDO, ENVIANDO, ENVIADO, ERROR }

/**
 * Estado del formulario de reporte. Las variantes del Figma salen de aquí:
 * SinSeleccion (motivo == null), Motivo, Enviando, Enviado y Error (conserva motivo y detalle).
 */
data class ReportarUiState(
    val motivo: MotivoReporte? = null,
    val detalle: String = "",
    val envio: EnvioReporte = EnvioReporte.EDITANDO,
) {
    val enviando: Boolean get() = envio == EnvioReporte.ENVIANDO
    val puedeEnviar: Boolean
        get() = motivo != null && (envio == EnvioReporte.EDITANDO || envio == EnvioReporte.ERROR)
}

/** Variantes seleccionables con DemoEstadoSelector. */
enum class VarianteReporte(val etiqueta: String) {
    SIN_SELECCION("Sin selección"),
    MOTIVO("Motivo"),
    ENVIANDO("Enviando"),
    ENVIADO("Enviado"),
    ERROR("Error");

    companion object {
        fun de(state: ReportarUiState): VarianteReporte = when (state.envio) {
            EnvioReporte.ENVIANDO -> ENVIANDO
            EnvioReporte.ENVIADO -> ENVIADO
            EnvioReporte.ERROR -> ERROR
            EnvioReporte.EDITANDO -> if (state.motivo == null) SIN_SELECCION else MOTIVO
        }
    }
}

/** Aplica una variante demo conservando el borrador escrito. */
fun ReportarUiState.conVariante(variante: VarianteReporte): ReportarUiState {
    val motivoDemo = motivo ?: MotivoReporte.ACTIVIDAD_PELIGROSA
    return when (variante) {
        VarianteReporte.SIN_SELECCION -> copy(motivo = null, envio = EnvioReporte.EDITANDO)
        VarianteReporte.MOTIVO -> copy(motivo = motivoDemo, envio = EnvioReporte.EDITANDO)
        VarianteReporte.ENVIANDO -> copy(motivo = motivoDemo, envio = EnvioReporte.ENVIANDO)
        VarianteReporte.ENVIADO -> copy(motivo = motivoDemo, envio = EnvioReporte.ENVIADO)
        VarianteReporte.ERROR -> copy(motivo = motivoDemo, envio = EnvioReporte.ERROR)
    }
}
