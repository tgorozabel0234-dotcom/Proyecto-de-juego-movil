# Combate de prueba reutilizable

`attacks.json` define alcance, duracion, dano, multiplicador y colores. El renderizador
crea barridos, estocadas, aura y torbellino con geometria; no necesita imagenes nuevas.
Los enemigos y sus vidas estan en `assets/levels/test_combat_targets.json`.
Los tipos MELEE y FLYING tienen IA, vida y ataques; DUMMY permite conservar blancos inmoviles. Los perfiles y detalles estan en `assets/enemies/prototype/README.md`. R reinicia vida, posiciones y estados.

## Controles

- K: barrido horizontal hacia el lado del personaje, parado o caminando.
- W + K: barrido superior.
- S + K en suelo: aura tenue durante 4 segundos; aumenta el dano x1.5, sin acumularse.
- S + K en aire: barrido inferior. Impactar un enemigo eleva al jugador a 17 unidades/s, libera el gancho y restaura un salto adicional con H.
- K durante dash o sprint de J, solo apoyado en suelo o plataforma: estocada corriendo: conserva el avance del dash/sprint y reserva su velocidad. Solo AL IMPACTAR un enemigo frena y convierte el avance en ascenso (15.3 a 27.2 unidades/s), con freno lateral de 0.18 s desde el impacto. Fallar no frena la carrera ni agrega impulso vertical.
- I mantenido + K breve (menos de 0.18 s): golpe aereo; al impactar suelta la cuerda y rebota a 17 unidades/s.
- I mantenido + K mantenido: torbellino mientras esta enganchado. Solo animacion,
  sin dano; tocar un enemigo libera la cuerda conservando la velocidad.
- U + direccion en aire: prioriza un enemigo alcanzable y lleva AL JUGADOR hacia el; el enemigo no se mueve. Al llegar, eleva al jugador a 15.3 unidades/s y restaura un salto adicional con H.
  Sin enemigo sigue funcionando la propulsion hacia superficies.

Prioridad: pendulo > abajo > arriba > estocada > barrido. Cada pulsacion da como
maximo un golpe por objetivo; mantener K fuera del pendulo no repite ataques.
El rebote aereo exige impacto y no multiplica impulsos por golpear varios blancos.
La cuerda se libera en el impacto, no al iniciar un golpe breve.
No hay golpes ni atraccion a traves de superficies solidas. El radio del pendulo y
la camara permanecen iguales. Valores iniciales ajustables tras probar sensaciones.

## Aturdimiento del enemigo

Todo golpe con dano aplica `stunDuration` (0.75 segundos). El enemigo conserva su
posicion y no puede moverse ni actuar (`canAct`, `tryMove`). No se congela al jugador.
Cada golpe nuevo reinicia el tiempo, sin sumarlo y sin dar invulnerabilidad.
`stunImmune: true` en un objetivo excluye el aturdimiento, pero conserva el dano y
los rebotes del jugador. Usar para futuros jefes, tanques o enemigos grandes.
No se ha agregado desplazamiento fisico por retroceso. Los enemigos con IA tambien respetan el stun;
se colorean en dorado y muestran un arco de tiempo durante el aturdimiento.
El torbellino sigue siendo solo visual y no aplica dano ni aturdimiento.

## Estocada terrestre y salto adicional

Solo se inicia la estocada con apoyo real (`canStartGroundThrust`); no se permite
en caida, salto o tolerancia de borde. En aire, K sigue permitiendo el barrido normal.
Una estocada iniciada con apoyo puede terminar al salir de una plataforma.
Al acertar, conserva su impulso vertical y habilita UN salto adicional con H.
Ese salto consume la carga y agrega el impulso de salto al ascenso existente (o
reinicia el ascenso si estaba cayendo). Mantener H no repite saltos. Aterrizar o R
limpia la carga. Fallar la estocada no eleva ni otorga salto extra.
El boton tactil SALTO se resalta mientras la carga este disponible. El HUD informativo actual solo muestra la barra compacta de vida.
Dash base reducido de 24 a 22 unidades/s; conserva duracion de 0.18 s, recarga de
0.6 s, sprint x2.3 y el impulso previo si ya se iba mas rapido.

El barrido normal inferior (S + K en aire) tambien restaura el salto adicional
al producir un rebote contra un enemigo. Cada nuevo rebote acertado restaura UNA
carga, sin acumularlas. Fallar o golpear varios enemigos con el mismo ataque no
concede cargas adicionales. Se consume con H y se limpia al aterrizar o reiniciar.

El gancho U contra enemigos restaura una carga al llegar al objetivo, no al dispararlo. No acumula saltos. Fallar, ser bloqueado, cancelar el viaje o enganchar superficies no concede esta carga. Se aplican el mismo consumo con H y limpieza al aterrizar/reiniciar.

## Potencia de rebote

`reboundMultiplier: 0.85` reduce un 15% el impulso vertical base de TODAS las
variantes de rebote: barrido inferior, golpe breve del pendulo, estocada acertada
y llegada con U al enemigo. Se aplica una sola vez, sin acumular multiplicadores
por cuadro. Los valores base `bounceSpeed`, `hookRiseSpeed`, `thrustMinimum` y
`thrustMaximum` se mantienen como referencia; las cifras anteriores ya muestran
el resultado efectivo. El salto con H y la recuperacion de su carga siguen igual.
Se conserva un ascenso previo que ya fuera mayor que el impulso resultante.

### Ajuste al personaje ampliado

La escala visual se duplicó. reach=3.3 para lateral/superior, attackOriginHeight=1.5 desde los pies, downReach=1.9 para el ataque inferior y whirlwindContactRadius=1.5. Los valores por defecto de CombatSystem conservan el perfil anterior; TestLevelScreen carga estos datos. Cambiar el tamaño no duplica el collider, el daño, el stun ni los impulsos. La animación no modifica valores de combate: las medidas se cargan en el sistema correspondiente.
