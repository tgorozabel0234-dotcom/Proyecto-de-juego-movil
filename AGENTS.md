# Instrucciones para agentes que trabajen en este proyecto

## Enemigos y combate

Antes de crear o modificar enemigos, su IA, sus ataques o las reacciones al dano,
leer y respetar [las reglas de aturdimiento](docs/gameplay/enemy-stun.md).
Estas reglas son decisiones del usuario; conservarlas salvo que el usuario pida
cambiarlas. No sustituir el aturdimiento por invulnerabilidad, pausa global o
retroceso fisico.

Los detalles del prototipo y sus controles estan en
[assets/combat/prototype/README.md](assets/combat/prototype/README.md).
La configuracion reutilizable se guarda en `assets`, no solo en el codigo.

Al cambiar estas mecanicas, actualizar su documentacion y verificar los casos
afectados con `gradlew.bat :lwjgl3:classes :core:check --offline`.
Si no es posible ejecutar la verificacion, informar de esa limitacion.

## HUD y entrada tactil

Antes de modificar el HUD o los controles, consultar `assets/ui/mobile_hud/README.md`. Conservar el teclado y la entrada independiente por dedo. Los gestos tactiles del pendulo y sprint permiten atacar con dos pulgares; no sustituirlos por combinaciones que requieran tres dedos sin una decision del usuario. Respetar area segura, pausa, cancelacion de toques y centro libre. La geometria y la configuracion reutilizable estan separadas del escenario.

`gradlew.bat :lwjgl3:hudPreviewCheck --offline` verifica el render y la entrada de Scene2D con OpenGL y exporta capturas a `lwjgl3/build/reports/hud`; es una comprobacion opcional de escritorio, no una prueba de hardware movil.

Estado actual de vida/HUD: maximo 10 puntos, 10 celdas de 1 punto; enemigos de prueba causan 1 por golpe. Mantener solo la barra compacta como informacion permanente, sin titulos, porcentajes ni estados adicionales. Los controles tactiles y pausa permanecen como entrada. Consultar los datos de `assets/ui/mobile_hud` y `assets/enemies/prototype` antes de cambiar estos valores.

## Animacion del jugador

El usuario autorizó integrar el rediseño: el nivel 00 usa ahora RedesignAnimation, RedesignClips, RedesignSprite y SpritePose, configurados en assets/characters/redesign/pixel_white_v2_shirt/animations/integration_v1/runtime.json. Consultar su README. Caminar usa cuatro poses, correr seis y caída dos; aterrizaje se omite. El prototipo de cuadrados amarillos queda archivado. Las hojas de `assets/players/cyber_runner_v1` y su codigo se conservan solo como referencia anterior; no reactivarlas sin indicacion del usuario. Leer su README antes de modificarlo. PlayerAnimation solo selecciona estados y PlayerSprite solo dibuja: no cambiar velocidades, colisiones, dano, aturdimiento o reseteos de salto desde el renderer. Mantener alineacion de pies/pivotes, espejo izquierdo y direccion de ataques coherente con attackFacing. Las hojas usan regiones medidas en animations.json, no una cuadricula de recorte uniforme. Los assets amarillos anteriores y los conceptos permanecen como referencias de versiones previas.

### Criterio de fotogramas del rediseño

El usuario pide pocos fotogramas que marquen claramente cada acción; evitar poses redundantes e intermedios de relleno. No generar ocho fotogramas por costumbre ni imponer tres como límite universal. La prioridad es conservar el ciclo completo, con las dos piernas alternándose en caminar y correr: el defecto anterior repetía el paso de la misma pierna incluso antes de reducir fotogramas. Eliminar solo poses redundantes; no imponer una cantidad. Aprobaciones actuales: caminar de cuatro poses (locomotion_two_step_v3), correr de seis (run_six_frame_v4), caída de dos (fall_draft_v1). Dash conserva su borrador independiente de tres poses. Comprobar botas completas, escala y pivotes antes de exportar. Mantener aprobación por animación antes de integrar estos recursos al juego.


### Hoja de luz y orientación del gancho

El usuario autorizó generar conjuntamente las animaciones restantes y después autorizó expresamente integrarlas al proyecto. Están activas en el nivel 00. Omitir la animación de aterrizaje. Los recursos nuevos y la galería están en assets/characters/redesign/pixel_white_v2_shirt/animations/remaining_actions_v1; consultar README.md e integration-notes.md.

La hoja de luz recta, cian de núcleo blanco, nace del antebrazo metálico izquierdo y tiene alcance visible más allá del cuerpo. No usar una espada empuñada ni cambiar el brazo emisor. La estocada corre hacia delante; solo al impactar convierte su impulso en subida. El rebote y el ataque rápido enganchado también reservan su salida de subida para el impacto.

El péndulo orienta la postura según el ángulo real de cuerda y la dirección tangencial; puede quedar invertida por encima del anclaje. No reproducir un giro continuo del cuerpo desconectado de la trayectoria. La propulsión se orienta al objetivo. La cuerda se dibuja por separado hasta el punto de unión del sprite. Estos contratos y la muestra JavaScript son visuales: no reemplazar la física existente desde el renderer. La activación ya está autorizada; conservar la separación entre gráficos y mecánicas.


Pruebas del rediseño: RedesignIntegrationCheck forma parte de :core:check. :lwjgl3:redesignPreviewCheck exporta el renderer real y :lwjgl3:hudPreviewCheck verifica el nivel con el HUD existente.


Escala actual del rediseño: renderScale 2.0 en integration_v1/runtime.json. Conservar floorY y SpritePose.placeAtFeet: el barrido lateral se hundía por anclar la cadera al suelo. El tamaño ampliado es visual; el collider de movimiento no se duplicó. Alcance lateral/superior 3.3, origen de ataque 1.5 sobre los pies, alcance inferior independiente 1.9 y contacto de torbellino 1.5 en attacks.json. Daño, stun e impulsos conservados. :lwjgl3:attackLevelPreviewCheck verifica ataques dentro del nivel real.
