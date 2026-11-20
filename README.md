# ProyectoProgra3-Grupo2 · VamosPues

<img src="app/src/main/ic_launcher-playstore.png" alt="Logo de VamosPues" width="128" />

VamosPues es una app Android para encontrar y organizar planes en grupo (fútbol, vóley, cine, juegos de mesa) con moderación de contenido. Esta entrega es una **demo interactiva de UI**: se navega, se completan formularios y se cambian variantes visuales, pero no hay red, base de datos, GPS, cámara ni moderación real.

## Tecnologías

- Kotlin + Jetpack Compose + Material 3 (tema propio, sin color dinámico).
- Navigation Compose (`ui/navigation`).
- AndroidX Core SplashScreen para la pantalla de inicio con el logo.
- Datos de ejemplo en `data/FakeData.kt` sobre el modelo de dominio de `model/`.

## Cómo ejecutar

1. Abrir el proyecto en Android Studio (JDK incluido en Android Studio).
2. Compilar desde la terminal:
   - Windows: `gradlew.bat assembleDebug`
   - macOS/Linux: `./gradlew assembleDebug`
3. Ejecutar la configuración `app` en un emulador o dispositivo (minSdk 24).

## Modo demo

`Config.MODO_DEMO = true` (en `com.example.vamospues.Config`) muestra en cada pantalla una fila de chips (`DemoEstadoSelector`) para forzar sus variantes: cargando, error, vacío y estados propios del flujo. Las acciones (unirse, reportar, check-in, publicar, votar…) solo cambian el estado local de la UI; **nunca** modifican `FakeData` ni el modelo.

## Estructura

```
app/src/main/java/com/example/vamospues/
├── Config.kt                 Bandera MODO_DEMO
├── MainActivity.kt           Splash + NavGraph
├── data/FakeData.kt          8 lobbies, usuarios, reportes y filtros de ejemplo
├── model/                    Dominio (Lobby, Usuario, estados, moderación…)
└── ui/
    ├── theme/                Tokens Material 3 (claro/oscuro)
    ├── components/           LobbyCard, CupoIndicator, EstadoChip, UbicacionBlock,
    │                         ParticipanteItem, estados genéricos, DemoEstadoSelector
    ├── navigation/           Rutas, NavGraph y un <Flujo>Graph.kt por flujo
    └── screens/
        ├── explorar/         Explorar (feed de lobbies públicos y activos)
        ├── crearlobby/       Formulario de creación
        ├── moderacion/       Resultado de publicación (pendiente/aprobado/rechazado)
        ├── detalle/          Detalle del lobby (acción contextual según estado)
        ├── reportar/         Hoja modal para reportar un lobby
        ├── checkin/          Check-in con QR/GPS simulados
        ├── chat/             Chat local con encuestas
        └── perfil/           Perfil y estados de cuenta
```

Las capturas de Figma que sirven de referencia visual están en `docs/referencias/`.

## Navegación

| Ruta | Pantalla | Llega desde |
|---|---|---|
| `explorar` (inicio) | Explorar | Barra inferior |
| `misLobbies` | Mis lobbies (provisional) | Barra inferior |
| `chat` | Chat | Barra inferior |
| `perfil` | Perfil | Barra inferior |
| `crearLobby` | Crear lobby | FAB de Explorar · "Editar lobby" en Moderación |
| `moderacion/{estado}` | Resultado de publicación | Publicar en Crear lobby |
| `detalle/{lobbyId}` | Detalle del lobby | Tarjeta de Explorar · "Ver lobby" en Moderación |
| `checkin/{lobbyId}` | Check-in | Botón Check-in del Detalle |
| `admin` | Administración (provisional) | Perfil en variante Admin |

Reportar **no** es una ruta: es un `ModalBottomSheet` que se abre desde el menú ⋮ del Detalle. Un `lobbyId` inexistente muestra un error visible con opción de volver.

### Detalle del lobby: variante por defecto

La acción principal se deriva del estado del lobby (`Lobby.estadoActual`) y de la participación de `FakeData.usuarioActual`:

| Lobby de ejemplo | Estado | Variante |
|---|---|---|
| `lobby-1`, `lobby-2` | Activo, acceso automático | Unirme |
| `lobby-3` | Activo, privado con aprobación | Solicitar unirme |
| `lobby-4` | Lleno y el usuario está confirmado | Check-in |
| `lobby-5` | En proceso | Evento en proceso |
| `lobby-6` | Finalizado | Evento finalizado |
| `lobby-7` | Bloqueado | Lobby bloqueado |
| `lobby-8` | Pendiente de moderación | En revisión |

## Equipo y flujo de trabajo

- Persona A: tema, componentes, Explorar, Crear lobby, Moderación, Detalle y Reportar.
- Persona B: navegación, Config, Check-in, Chat, Perfil y Administración.
- Ramas: `main` (entregable), `develop` (integración) y `feature/<area>-<descripcion>`, con PR hacia `develop` y revisión cruzada.
