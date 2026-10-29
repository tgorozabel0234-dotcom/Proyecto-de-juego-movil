# Estado: version archivada para referencia, no activa

# Personaje animado cyber_runner_v1

Diseno basado en la referencia del usuario `references/user_reference.png`:
cabello blanco, chaqueta oscura con lineas cian, pantalones cargo y brazo mecanico.
Version anterior conservada, NO cargada por el nivel: el usuario decidio redisenar al personaje y volver temporalmente a los cuadrados amarillos. El concepto
anterior de pintado se conserva separado; no es la referencia de estos sprites.

## Assets y regiones

Tres hojas PNG con transparencia real, cargadas una sola vez al entrar al nivel.
48 regiones en total. `animations.json` guarda coordenadas, dimensiones, pivote
horizontal y pixeles por unidad para cada region. NO usar TextureRegion.split:
las hojas generadas no tienen celdas matematicamente uniformes. Las regiones se
midieron por siluetas alfa; se conservan intactos los PNG fuente.

| Hoja | Regiones | Uso |
| --- | --- | --- |
| locomotion.png | 0-3 | Reposo / respiracion |
| locomotion.png | 4-7 | Caminar |
| locomotion.png | 8-11 | Correr; dos poses reutilizadas por dash con estela |
| locomotion.png | 12-15 | Preparar salto, ascenso, apex y descenso; aterrizaje reutiliza crouch |
| combat.png | 0-3 | Barrido lateral, golpe breve del gancho y torbellino |
| combat.png | 4-7 | Ataque superior |
| combat.png | 8-11 | Ataque inferior / rebote |
| combat.png | 12-15 | Estocada en carrera |
| grapple_reactions.png | 0-3 | Preparacion y disparo de gancho |
| grapple_reactions.png | 4-7 | Pendulo, propulsion y salida del enganche |
| grapple_reactions.png | 8-9 | Reaccion de dano |
| grapple_reactions.png | 10-11 | Muerte: arrodillarse y caer; no se repite |
| grapple_reactions.png | 12-15 | Carga del aura |

Es una primera version con cuatro poses clave por ciclo base; no es una copia
del numero de frames impreso en la referencia. Algunos estados comparten poses
y se complementan con rotacion o estelas en el renderer. El torbellino usa los
barridos rotados y el efecto del juego, no una hoja adicional de giro de 360 grados.

## Integracion

- PlayerAnimation.java selecciona 19 estados visuales segun fisica, combate y vida.
- PlayerSprite.java carga las regiones, crea su espejo izquierdo y dibuja.
- El jugador sigue midiendo 0.5 x 1 en fisica. No cambiar la colision por el ancho
  de un brazo, una pose de ataque o una estela.
- En suelo se alinean los pies; en aire se mantiene el centro del cuerpo. Los
  pivotes horizontales compensan los brazos extendidos sin mover la colision.
- Los ataques se orientan con attackFacing, igual que su hitbox; el dash mira a
  su velocidad real. No orientar un golpe hacia otra direccion solo por cambiar A/D.
- Los eventos de dano muestran una reaccion visual, sin aturdir al jugador ni
  impedir acciones. La muerte mantiene la ultima pose. R reinicia los relojes.
- Los ataques usan su progreso de combate, no un contador independiente de dano.
- La cuerda, los barridos y el aura siguen siendo efectos del motor; los colores
  de ataque/aura se ajustaron a cian. Las reglas de gancho, dano y rebote no cambian.
- references/combat_glow_concept.png es un concepto alternativo con efectos que
  invadian regiones vecinas; se conserva para arte, pero no se carga en el juego.

## Reutilizacion y pruebas

Para agregar poses, guardar una nueva hoja en assets y declarar sus regiones y
pivotes. Conservar los nombres/versiones previas si se reemplaza el diseno.
Al editar una hoja no recortar automaticamente cada frame y escalarlo a altura
completa: eso agranda las poses agachadas y causa saltos de tamano.

`gradlew.bat :lwjgl3:classes :core:check --offline` comprueba selector, assets,
regiones, alfa, transiciones, direccion y que la animacion no modifica la fisica.
`gradlew.bat :lwjgl3:characterPreviewCheck --offline` requiere OpenGL y exporta
cuatro galerias de poses a `lwjgl3/build/reports/character/poses-0.png` ... `poses-3.png`.
`gradlew.bat :lwjgl3:hudPreviewCheck --offline` muestra al jugador integrado en
el escenario con el HUD compacto, formatos de pantalla y controles tactiles.

Las texturas son ilustraciones generadas y pueden requerir refinamiento artistico
de continuidad entre frames. Las pruebas de regiones y estados no garantizan una
animacion final de produccion. La galeria sirve para revisar estos detalles.

Algunas regiones incluyen `exclude`: pequenos rectangulos donde asomaban pixeles de la pose vecina. El renderer los omite dividiendo la region en piezas con el mismo pivote; no cambia ni repinta los PNG fuente. Respetar estas exclusiones al crear un atlas o portarlo a otro motor.

## Tamano visual

`renderScale: 1.5` en animations.json aumenta un 50% el dibujo de TODAS las acciones dentro del nivel. El escalado se aplica de forma comun a regiones, pivotes y estelas, manteniendo los pies en el suelo y el espejo izquierdo. No modificar individualmente pixelsPerUnit para agrandar poses: eso rompe la continuidad. La camara y la colision fisica de 0.5 x 1 conservan sus valores; este ajuste es visual y el dibujo ocupa mas espacio que el cuerpo fisico. La galeria de poses tiene escala propia para encuadrar las muestras; las capturas del nivel muestran la escala real de juego.
