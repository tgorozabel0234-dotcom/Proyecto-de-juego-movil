# Historia del nivel 1: Cenizas de Neón

Implementa el nivel 1 de los guiones *Guion Técnico* y *Guion Narrativo (Niveles 1 a 5)* sobre las salas que ya existían del Callejón 42.

## Escenas

`nivel_1.json` contiene el texto de cada escena. Para corregir un diálogo no hace falta tocar el código. Cada línea admite `speaker`, `text` y `style`:

| style | Uso |
|---|---|
| `dialogue` (por defecto) | Personaje hablando |
| `narration` | Voz en off de Aka |
| `action` | Acotación del guion |
| `glitch` | Flashback con interferencia VHS |
| `datapad` | Pantalla del datapad |
| `tutorial` | Indicación de controles |

Los disparadores viven en el campo `story` del mapa (`levels/section_1.json`):

| Escena | Guion | Evento |
|---|---|---|
| `intro` | 1.1 Cinemática de apertura | `start`: solo en partida nueva |
| `matones` | 1.2 Encuentro y despertar del Salto Frame | `zone` de pie en la plataforma del Taller de Aka; cámara en los Matones; concede `frame` |
| `matones_huida` | 1.2 El Matón 2 huye | `flee`: cae un Matón y su compañero escapa |
| `capataz` | Tutorial del mini jefe | `zone` de pie en la plataforma de la zona de carga; cámara en el Capataz |
| `datapad` | 1.3 Outro y pista al Sector Industrial | `boss`: derrota del Capataz; cámara en el Capataz; concede `capataz` |

Por decisión del usuario, las escenas de encuentro usan `"grounded": true`: se disparan solo cuando el jugador está de pie dentro de la zona, que cubre la superficie de la plataforma de los enemigos. Nunca se disparan en mitad de un salto o con el gancho. `"focus"` lista los ids de los enemigos: durante la escena la cámara se desplaza en tiempo real hasta ellos y, al terminar, vuelve a seguir al jugador.

Cada escena se ve una sola vez. La bandera `scene:<id>` se guarda con las demás en el guardado manual, sin cambiar el formato v3. Si cargas una partida o llegas por una puerta, las escenas de zonas que ya quedaron atrás se marcan como vistas sin reproducirse. Durante una escena la simulación se detiene, como en la pausa. Tocar la pantalla, Enter, Espacio, K o H avanzan; SALTAR, Esc o Atrás la saltan. Al abrirse, la escena cancela los toques activos.

## Salto Frame (nivel I)

No usa un botón nuevo: los controles táctiles y el teclado siguen igual. Un **dash que alcanza a un enemigo** hace que Aka reaparezca a su espalda mirándolo, con estática azul, 0.35 s de intangibilidad y tiempo ralentizado (bullet-time). No causa daño ni stun por sí solo: el golpe lo sigue dando el jugador. Se cancela si el destino está ocupado por un sólido. Solo se activa una vez por dash.

Lógica: `FrameJump.java`. Valores: `assets/combat/prototype/frame_jump.json`. En el Callejón 42 se desbloquea con la escena 1.2. En las demás salas y en el escenario 00 ya está disponible.

## Matones de Celestia

Son dos enemigos `THUG` en el Taller de Aka (zona 2 del guion) y reemplazan a `robot-03`. Por decisión del usuario usan los sprites del Robot (Voltaic Slinker), tintados en rojo (`tint` en `types.json`). Su golpe usa la animación `swipe` del pack, que imita el garrote eléctrico. Usan la IA común (`EnemyAI`) con el perfil `thug` de `enemies/prototype/types.json` y respetan el stun de 0.75 s. Forman el grupo `squad: "matones"`: cuando cae uno, el otro huye (`fleeWhenSquadFalls`), se desvanece y no vuelve al cargar la partida.

## El Capataz Brutal (mini jefe)

`FOREMAN` (`foreman-01`) está en la plataforma de la zona de carga, al final del Callejón 42, y reemplaza a `scorp-06`. Su lógica está en `ForemanBoss.java` y sus valores en `types.json`, bajo `foreman`.

- **Fase 1, carga frontal:** anuncia la carga con un anillo rojo y corre hasta el borde. Su frente está blindado (`backArmor`) y solo la batería trasera recibe daño. Atravesar la carga con el Salto Frame deja al jugador a su espalda.
- **Fase 2, onda de choque:** a partir del segundo ciclo golpea el suelo antes de cargar. La onda recorre el suelo y se esquiva saltando.
- **Victoria:** la vida se divide en 3 tercios (3 ciclos de vulnerabilidad). El daño de un ciclo no pasa del tercio actual.
- Es `stunImmune`, según [las reglas de aturdimiento](../../docs/gameplay/enemy-stun.md): recibe daño, pero no se aturde.
- La salida a la Torre de bombeo exige la bandera `capataz` y la puerta se ve apagada hasta entonces.

## Arte provisional

Los Matones usan los sprites del Robot tintados (`EnemySprites`). El Capataz usa la hoja de sprites que entregó el usuario, recortada en `assets/enemies/Capataz` (ver su README.txt) y dibujada por `ForemanSprites`. Los efectos de la batería, el anuncio, el bloqueo y la onda siguen siendo procedurales (`StoryFoesRenderer`). Si faltaran los PNG, se usa una figura procedural de respaldo. El renderer solo lee estados y no modifica posiciones, daño ni temporizadores.

## Verificación

`gradlew.bat :lwjgl3:classes :core:check --offline` incluye `storyCheck` (`StoryLevelCheck`). Cubre el Salto Frame (incluidos paredes y enemigos muertos), el blindaje, la huida de los Matones, los ciclos y la onda del Capataz, las escenas y los datos del mapa. También simula el combate completo contra el Capataz en la geometría real del Callejón 42.

---

# Historia del nivel 2: Conductos y Sombras (fase 1)

Implementa las escenas 2.1 y 2.2 del *Guion Narrativo* y la mecánica del nivel 2 del *Guion Técnico* (Salto Frame Doble y barreras electromagnéticas), sobre las salas que ya existían de los Conductos de Central Corp. Los textos están en `nivel_2.json`; los mapas lo indican con `"storyFile": "story/nivel_2.json"`.

| Escena | Guion | Sala | Evento |
|---|---|---|---|
| `intro2` | 2.1 Introducción al sector industrial | Colector del distrito (`section_2`) | `enter`: al llegar por la puerta desde el nivel 1 |
| `transmision` | 2.2 Transmisión interceptada | Cámara de conmutación (`section_2_room_3`) | `zone` de pie en la plataforma del terminal; cámara en el terminal; holograma de Celestia; concede `frame2` |

- `enter` es un evento nuevo: se dispara al entrar a la sala por una puerta (o en partida nueva). Al cargar un guardado en esa sala se marca como vista sin reproducirse.
- La salida de la Cámara de conmutación exige `scene:transmision` (`"requires"`), así que la escena 2.2 no se puede saltar con el gancho. La puerta se ve apagada hasta entonces y el letrero del terminal cambia a `CANAL INTERCEPTADO`.
- `"focusPoint": [x, y]` centra la cámara en un punto del mapa (pies en `y`) en vez de en enemigos.
- `"hologram": {"x", "y", "facing", "from", "until", "fade"}` proyecta el holograma provisional de Celestia (`StoryFoesRenderer.drawHologram`) desde la línea `from` hasta la línea `until` de la escena, donde se disipa en `fade` segundos. Es solo dibujo.
- Estilo de línea nuevo `hologram`: nombre del personaje + `/ HOLOGRAMA` en azul, con líneas de escaneo.

## Salto Frame Doble (nivel II)

Sigue sin botón nuevo: es una mejora del mismo DASH. Con la bandera `frame2`:

- **Segundo impulso en el aire:** tras un dash en el aire se puede hacer otro sin esperar la recarga, una vez por salto. Se recupera al tocar el suelo. Al empezar fija una velocidad vertical mínima (`airDashLift`, 2.5): corta la caída con un pequeño impulso de apenas 0.1 de altura, sin ampliar el alcance del salto. En el suelo, el dash espera su recarga como siempre.
- **Barreras electromagnéticas:** durante cualquier dash se atraviesan. Fuera del dash son sólidas como un muro, también para el gancho. Si un dash termina dentro de una barrera, continúa hasta salir, y el jugador nunca queda atrapado dentro (sale hacia donde avanzaba).
- Efectos: estática morada al atravesar, cian en el segundo dash (`StoryFoesRenderer.drawPhase`).

Valores en `assets/combat/prototype/frame_jump.json` (`doubleUnlockFlag`, `airDashLift`). Lógica en `PlayerBody` (`addBarrier`, `setDoubleFrame`, `isPhasing`).

Las barreras son piezas con `"role": "barrier"` en el mapa. Por ahora hay una en el Retorno de servicio (`section_2_room_4`), antes de la salida: llega hasta y=160, como los muros laterales, para que no se pueda rodear con el gancho. Una sala con barreras que no concede `frame2` lo da automáticamente al cargarse, así ninguna partida antigua queda bloqueada. `LevelFlowCheck` no trata las barreras como superficies; las verifica `Level2StoryCheck`.

## Pendiente (fases siguientes del nivel 2)

Láseres de seguridad en movimiento, minijuego de hackeo de nodos, elevadores con drones de rastreo y el mini jefe UNIT-09 (el Centinela Púrpura).

## Verificación del nivel 2

`gradlew.bat :lwjgl3:classes :core:check --offline` incluye `level2StoryCheck` (`Level2StoryCheck`): barreras con y sin desbloqueo, dash en el aire y en el suelo, segundo dash (uno por salto, se recupera al aterrizar), salida de una barrera, la barrera real del Retorno de servicio (sin `frame2` no se llega a la salida; con él, sí) y los datos de las escenas 2.1 y 2.2.
