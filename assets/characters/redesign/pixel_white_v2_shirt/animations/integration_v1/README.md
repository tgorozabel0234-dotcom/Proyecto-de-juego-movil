# Rediseño integrado — versión 01

Integración autorizada expresamente por el usuario. El nivel de pruebas 00 usa ahora el personaje de pelo blanco, camiseta bajo chaleco y antebrazo metálico. El prototipo amarillo y cyber_runner_v1 se conservan como referencias y no se cargan al jugar este nivel.

`runtime.json` reúne 21 clips y 63 PNG reutilizados desde las carpetas originales. Caminar conserva sus cuatro poses aprobadas, correr sus seis y caída sus dos. Salto usa solo los fotogramas aéreos: se excluyen las poses de contacto para respetar la omisión de aterrizaje. El resto de acciones usa remaining_actions_v1.

`RedesignAnimation` selecciona estados sin modificar física ni bloquear acciones. Tras tres segundos quieto comienza la secuencia de mirar y arrodillarse; conserva la última pose hasta moverse. Levantarse se interrumpe de inmediato al saltar, atacar o hacer dash. Daño, muerte y reinicio limpian sus estados visuales. Los eventos de ataque y de impacto se leen desde getters de CombatSystem; no se emiten impulsos desde el renderer.

`RedesignClips` carga los datos y las ramas de fallo/impacto. `RedesignSprite` carga los PNG, aplica filtro nearest, mantiene pivotes/escala y dibuja el brazo de lanzamiento como capa orientable desde el suelo. `SpritePose` calcula coordenadas y orientaciones en el mundo sin cambiar PlayerBody. La cuerda termina en la mano transformada. Péndulo y ataques enganchados siguen el ángulo radial y el sentido tangencial; se permite la inversión sobre el anclaje. Propulsión sigue el objetivo. No se añade un giro automático al torbellino.

Los lienzos grandes de combate no se ajustan al collider: se usa `pixelsPerUnit` para conservar el tamaño del personaje y dejar espacio a la hoja. El collider de movimiento, las velocidades, el dash, la duración de los ataques, los impulsos por impacto y el stun siguen siendo los del sistema existente. El alcance y el centro del ataque se ajustaron al tamaño visual ampliado; consultar el ajuste de escala al final de este documento. Los barridos procedurales antiguos se retiraron para evitar superponer armas; el aura tenue persistente se mantiene cuando el personaje se mueve.

El brazo se separa mediante regiones de la imagen en tiempo de render, sin alterar el original. Al apuntar a la izquierda se refleja el sprite, incluidos sus detalles asimétricos; la precisión anatómica de una vista lateral opuesta puede pulirse con arte específico. La escala y los puntos de unión siguen siendo ajustables en los datos.

## Comprobación

- `gradlew.bat :lwjgl3:classes :core:check --offline`: integración, poses RGBA, ramas por impacto, reposo, orientación sobre el anclaje y regresiones de física, combate, vida y controles.
- `gradlew.bat :lwjgl3:redesignPreviewCheck --offline`: render real con OpenGL en ventana oculta, con galería de poses, ángulos de péndulo/torbellino y apuntado terrestre. Capturas en `lwjgl3/build/reports/redesign`.
- `gradlew.bat :lwjgl3:hudPreviewCheck --offline`: nivel real y HUD móvil/escritorio; capturas en `lwjgl3/build/reports/hud`.

La integración fue compilada y verificada en escritorio; no se ha probado todavía en un teléfono físico. Reiniciar la aplicación que ya estuviera abierta para cargar las nuevas clases y texturas. No se hizo commit ni push como parte de esta tarea.

## Ajuste de tamaño y apoyo

El usuario pidió duplicar el tamaño del personaje y revisar cómo encajan los ataques en el escenario. `renderScale: 2.0` duplica todas las poses, incluyendo mano, hoja y gancho, sin ampliar el lienzo completo como si fuera el cuerpo ni cambiar el collider de movimiento. La cámara y el HUD mantienen sus ajustes.

Se corrigió un fallo del barrido lateral: sus PNG usaban el pivote de cadera como apoyo de suelo, lo que hundía los pies en la plataforma. Cada frame tiene ahora `floorY` explícito; `SpritePose.placeAtFeet` conserva la misma colocación al cambiar entre apoyo y aire. Los efectos bajo la línea de apoyo se recortan al estar en suelo; no se recortan durante el ataque aéreo. El personaje sigue siendo recortado por su imagen/pivotes, no estirado para llenar el margen del arma.

El alcance lateral/superior se amplió de 1.65 a 3.3 unidades y el origen del ataque se sitúa a 1.5 unidades sobre los pies para acompañar el torso ampliado. El ataque inferior tiene alcance independiente de 1.9 desde ese origen, evitando aplicar golpes invisibles muy por debajo de su punta. El contacto del torbellino usa radio 1.5; continúa sin hacer daño y solo libera el gancho. Estos datos viven en assets/combat/prototype/attacks.json. Daño 10, stun 0.75, impulsos y reseteos no cambiaron.

Las comprobaciones cubren los dos sentidos, los puntos de apoyo y la continuidad suelo/aire de los 63 PNG, así como los alcances de combate con los datos reales. `:lwjgl3:attackLevelPreviewCheck` exporta ocho capturas en el nivel 00, con reposo, barrido, ataque superior, estocada y ataque inferior. Se guardan en lwjgl3/build/reports/character-polish. `:lwjgl3:hudPreviewCheck` comprueba el nuevo tamaño con el HUD móvil y de escritorio.

