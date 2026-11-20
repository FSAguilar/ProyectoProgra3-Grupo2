PROYECTO: VamosPues, app Android nativa (Kotlin, Jetpack Compose, Material 3).

REFERENCIA VISUAL
Las imágenes adjuntas son capturas del diseño en Figma y son la fuente de verdad VISUAL. Cada teléfono tiene su nombre de variante abajo (por ejemplo "Explorar", "Sin resultados"): es un estado de la misma pantalla. Las imágenes mandan en lo visual (jerarquía, espaciado, colores, iconos y textos exactos); este texto manda en el comportamiento.
- Copia los textos tal como se ven.
- No inventes elementos que no estén en las imágenes. Si algo no se lee o es ambiguo, pregúntame ANTES de adivinar.
- La imagen "Guía Compose" trae el árbol de composables sugerido y los estados: síguela.
- PRIMER PASO: antes de escribir código, describe en 5 a 10 líneas por imagen lo que ves (frames y elementos principales) y ESPERA mi "ok".

ALCANCE: interfaz INTERACTIVA SIN funcionalidad real. Se navega, se escribe en campos, se activan chips y switches, se abren diálogos y hojas. No hay red, base de datos, GPS ni cámara reales, ni moderación real. Los datos salen de data/FakeData.kt y del modelo (model.*).

REGLAS
1. Material 3: usa MaterialTheme.colorScheme/typography/shapes y espaciados de 8/16/24/32 dp. Nunca colores ni tamaños sueltos. Objetivos táctiles de 48 dp o más. contentDescription en los iconos.
2. Un archivo por pantalla: ui/screens/<flujo>/<Nombre>Screen.kt. La pantalla recibe `state` y callbacks (state hoisting). Las variantes (vacío, cargando, error...) son valores de un enum/sealed class <Nombre>UiState, no pantallas distintas.
3. Un @Preview por variante (claro y oscuro donde las imágenes lo muestren).
4. Reutiliza ui/components (LobbyCard, CupoIndicator, EstadoChip, UbicacionBlock, ParticipanteItem y los estados genéricos de cargando/vacío/error). Si falta uno compartido, avísame; si es solo de este flujo, créalo en su carpeta.
5. Solo edita archivos de TU flujo y su ui/navigation/<Flujo>Graph.kt. No toques el tema, los componentes compartidos ni el modelo; si algo falta, dímelo.
6. Modo demo: usa DemoEstadoSelector (ui/components) bajo `if (Config.MODO_DEMO)` para ver cada variante dentro de la app.
7. Textos en español como constantes en el composable (strings.xml no es parte de esta fase).
8. Al terminar: compila, indica qué variantes quedaron, cuáles no y qué diferencias visuales tienes respecto a las imágenes.

REGLAS DE DATOS
- Explorar muestra los lobbies donde esPublico() && estadoActual is Activo (con los datos de FakeData salen exactamente los 2 del Figma).
- La variante del botón principal del Detalle sale del lobby y de FakeData.usuarioActual (participa, pendiente, cupo lleno, etc.). DemoEstadoSelector puede forzar otra variante.
- Distancias (1,2 km, 3,4 km), medallas del perfil y mensajes del chat no están en el modelo: son datos locales de cada pantalla, no se agregan a model.*.