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
