EL CAPATAZ BRUTAL - sprites del mini jefe del nivel 1
=====================================================
Origen: hoja de sprites entregada por el usuario (IDLE_01-03, WALK_01-04,
ATTACK1_01, ATTACK2_02/03, ATTACK2_02/04/06, HIT/DAMAGE, DEFEATED/DEATH).

Cada cuadro se recorto de la hoja, se le quito el fondo de panel metalico y las
etiquetas, y se coloco en un lienzo transparente de 240x200 con los pies en el
borde inferior y las caderas centradas. Asi no "salta" al cambiar de cuadro.
Los sprites miran a la derecha; el juego los espeja cuando el jefe mira a la izquierda.

Nombre en la hoja -> archivo -> uso en ForemanBoss
  IDLE_01..03          idle_00..02   DORMANT / RECOVER
  WALK_01..04          walk_00..03   CHARGE (carga frontal, ciclo rapido)
  ATTACK1_01           windup_00     TELEGRAPH (anuncio de la carga)
  ATTACK2_02 (rayo)    swing_00      inicio de EXPOSED (remate de la carga)
  ATTACK2_03           swing_01      EXPOSED (espalda y bateria expuestas)
  ATTACK2_02 (garra)   slam_00       SLAM, primera mitad
  ATTACK2_04           slam_01       SLAM, segunda mitad
  ATTACK2_06           slam_02       impacto: sale la onda de choque
  HIT/DAMAGE           hit_00        destello al recibir dano
  DEFEATED/DEATH       death_00..01  DEFEATED

animations.json guarda escala (pixelsPerUnit) y tiempos. ForemanSprites solo dibuja:
no cambia posicion, dano ni estados.
