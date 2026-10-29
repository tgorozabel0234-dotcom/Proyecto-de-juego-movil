# Enemigos y vida de prueba

`types.json` define vida del jugador, proteccion tras un golpe y perfiles de IA.
Las instancias y sus vidas estan en `assets/levels/test_combat_targets.json`.
`type` acepta `MELEE`, `FLYING` o `DUMMY` (blanco inmovil para pruebas).
`stunImmune` sigue siendo opcional y false por defecto.

- Jugador: 10 de vida; proteccion de 0.8 s tras recibir dano para evitar perder
  toda la vida por contacto repetido. El HUD muestra diez celdas de integridad y el personaje parpadea. Cada celda equivale a 1 punto; ambos enemigos de prueba causan 1 punto por golpe.
- Terrestre rojo: 50 de vida, persigue por el suelo, respeta paredes y evita
  abandonar plataformas; anuncia el golpe 0.35 s y causa 1 de dano si aun esta cerca.
- Volador azul con alas: 30 de vida, persigue en dos ejes, respeta solidos;
  anuncia su ataque de proximidad 0.45 s y causa 1 de dano si alcanza al jugador.
- Un aro naranja anuncia el ataque. Se puede esquivar saliendo del alcance.
- Los ataques requieren vision libre y tienen recuperacion antes de repetirse.
- Los golpes del jugador siguen usando el dano/aura de `assets/combat/prototype`.
  Golpear cancela el ataque pendiente del enemigo comun e inmoviliza 0.75 s.
- Enemigos inmunes siguen recibiendo dano pero no se interrumpe su ataque por stun.
- Al morir un enemigo desaparece y deja de actuar. Al morir el jugador se bloquean
  sus acciones; R reinicia vida, enemigos, posiciones, ganchos y camara.

Los graficos son formas procedurales reutilizables, con colores en `types.json`:
cuerpo y extremidades para terrestre; nucleo, contorno y alas animadas para volador.
Los enemigos siguen siendo procedurales y no necesitan sprites externos. El jugador usa temporalmente los dos cuadrados amarillos de assets/players/prototype para un nuevo redisenyo.
La IA no busca rutas, no salta entre plataformas y no dispara proyectiles.
Los enemigos no tienen colision entre si. Son limitaciones intencionadas de esta
primera IA de prueba; no se teletransportan para superar obstaculos.

La cuerda U actualiza su destino mientras el enemigo se mueve. Llegar a el sigue
produciendo el impulso y reseteo de salto; fallar no concede el salto.
Las reglas obligatorias de stun estan en `docs/gameplay/enemy-stun.md`.

Comprobar con `gradlew.bat :lwjgl3:classes :core:check --offline`.
