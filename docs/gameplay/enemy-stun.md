# Regla de diseno: aturdimiento de enemigos

Decision del usuario registrada el 28 de octubre de 2026. Aplicable a nuevos
enemigos y futuras implementaciones de IA, independientemente de quien las programe.

## Comportamiento requerido

- Los enemigos comunes reciben **0.75 segundos de aturdimiento** al recibir un
  golpe con dano. El valor de prueba es ajustable mediante `stunDuration`.
- El estado pertenece al **enemigo impactado**, no al jugador ni al mundo.
- Durante el estado, el enemigo queda inmovil y no puede actuar, iniciar ataques
  ni ejecutar decisiones de movimiento. La futura IA debe consultar `canAct()`;
  el movimiento debe respetar `tryMove()` o una comprobacion equivalente. No
  actualizar su posicion por otra ruta ignorando el estado.
- El tiempo transcurre en segundos de simulacion, no por cantidad de cuadros de
  pantalla; debe funcionar igual a distintas tasas de FPS.
- Cada nuevo golpe valido reinicia la ventana a 0.75 segundos. No suma duraciones.
- El enemigo sigue siendo vulnerable al dano durante ese tiempo.
- Al terminar, un enemigo vivo recupera sus acciones. Uno muerto no puede actuar.
- Reiniciar el nivel limpia el tiempo pendiente y conserva las inmunidades.

## Excepciones configurables

La mayoria de enemigos debe conservar este comportamiento por defecto.
Jefes, enemigos grandes o tanques planeados como resistentes pueden usar
`stunImmune: true` en sus datos. No inferir inmunidad solo del tamano visual.

La inmunidad evita el aturdimiento; no impide recibir dano ni elimina los rebotes
o impulsos del jugador que dependan de acertar un ataque.

El requisito actual implementa **inmovilidad temporal**, no empuje del enemigo.
No hay retroceso fisico implementado. Si se agrega despues, conservar esta
distincion y definir expresamente sus excepciones sin asumir que ambas inmunidades
son automaticamente equivalentes. Tampoco se ha pedido una pausa global de impacto.

## Integracion actual

- `core/src/main/java/io/github/some_example_name/CombatSystem.java`:
  `Enemy.receiveHit`, `isStunned`, `getStunRemaining`, `canAct` y `tryMove`.
- `assets/combat/prototype/attacks.json`: `stunDuration: 0.75`.
- `assets/levels/test_combat_targets.json`: cada objetivo admite
  `stunImmune` (opcional, `false` por defecto).
- `TestLevelScreen.java`: carga la inmunidad desde los datos; el dorado y el arco
  alrededor del objetivo indican el estado y su tiempo restante.

Ya hay enemigos terrestres y voladores con IA simple en `EnemyAI.java`. Ambos consultan `canAct()` y usan `tryMove()`, respetando la inmovilidad y el bloqueo de ataques. Los DUMMY siguen inmoviles para pruebas.
Una animacion de dolor por si sola no implementa el bloqueo de acciones.
El torbellino del pendulo es solo visual: no causa dano ni aturdimiento.

## Regresiones que deben evitarse

Verificar que el enemigo comun se inmoviliza y recupera su capacidad de actuar,
que el reimpacto refresca sin acumular ni volverlo invulnerable, y que el enemigo
inmune recibe dano sin quedar aturdido. Mantener pruebas de muerte, reinicio,
distintas tasas de FPS y ausencia de congelacion del jugador.

Las comprobaciones actuales estan en `core/src/test/java/CombatSystemCheck.java`.
Ejecutarlas mediante `gradlew.bat :lwjgl3:classes :core:check --offline` al modificar
codigo de combate o enemigos.

La integracion real de IA, ataques y stun se verifica tambien en `core/src/test/java/EnemyAICheck.java`. Los datos reutilizables estan en `assets/enemies/prototype`.
