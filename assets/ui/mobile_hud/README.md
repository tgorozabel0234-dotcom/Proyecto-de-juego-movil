# HUD y controles tactiles reutilizables

`layout.json` configura margen, diametro del joystick, zona muerta, opacidad y
color de acento. El HUD dibuja sus elementos mediante geometria compartida;
no requiere imagenes externas. GameHud.java contiene los elementos de Scene2D;
HudLayout.java concentra la distribucion; TouchControls.java conserva el estado
de cada dedo independientemente de la frecuencia de renderizado.

## Distribucion

- Arriba izquierda: barra compacta de diez celdas; cada celda equivale a un punto de vida.
- Se ocultan los textos, porcentajes y estados de dash, gancho y ataque. Solo queda la vida como informacion permanente.
- PAUSA (II) se muestra cuando hay controles tactiles. En teclado, Esc abre pausa. Cambiar el modo tactil desde pausa o F3.
- Abajo izquierda: joystick de movimiento y apuntado, con ocho sectores y zona muerta.
- Abajo derecha: ATAQUE grande; SALTO, DASH, TIRON y GIRO separados.
- El centro de la pantalla queda libre para ver el escenario y los enemigos.

Se adapta a horizontal, vertical y pantallas panoramicas, respetando los margenes
seguros que comunica el dispositivo (camara, muesca y zona de gestos). Los controles
no cambian el campo de vision ni los valores de fisica. Para jugar se recomienda
horizontal; la distribucion vertical tambien se mantiene utilizable.

## Gestos

- Joystick: direccion en ocho sectores para ataque/gancho y movimiento horizontal analogico.
- SALTO: pulsar para saltar, mantener para conservar el salto variable.
- DASH: pulsar inicia un dash. Activa sprint mientras se mantenga el joystick hacia
  un lado; centrar o soltar el joystick lo desactiva. Mantener DASH tambien funciona.
- ATAQUE: pulsar o mantener, segun la mecanica actual.
- TIRON: propulsion hacia superficies o enemigos usando la direccion del joystick.
- GIRO: tocar engancha; otro toque suelta. Este cambio es solo del control tactil,
  para liberar el pulgar y poder saltar/atacar con dos manos. El teclado conserva I mantenido.
- Una cancelacion por salto/estocada o un disparo fallido limpia el modo GIRO.
- Los toques muy breves se conservan hasta procesarse. Cada dedo controla su propia
  accion; soltar un dedo no libera los demas controles.
- Pausar, cambiar tamano, ocultar controles, salir del nivel o suspender la app
  limpia los dedos y las acciones pendientes para evitar botones atascados.

## Menu y pruebas

PAUSA ofrece continuar, ayuda, reinicio, controles tactiles, mapa, datos y menu
principal. Pausa y ayuda detienen la simulacion; datos permite observarla en vivo.
R reinicia tambien desde derrota. El teclado sigue usando WASD, H, J, K, U e I.
Esc abre/cierra pausa. F3 o la opcion CONTROLES TACTILES en pausa muestra los controles para probarlos con
raton en computadora; el raton solo puede emular un dedo a la vez.

Se activan automaticamente con un backend Android/iOS. El repositorio todavia
incluye unicamente core y lwjgl3: este cambio no crea una APK ni un modulo Android.
Las pruebas de estado simultaneo y layout no sustituyen probar varios dedos en
un telefono real cuando se incorpore ese lanzador.

Verificacion: `gradlew.bat :lwjgl3:classes :core:check --offline`.

Revision visual opcional en escritorio: `gradlew.bat :lwjgl3:hudPreviewCheck --offline`. Exporta horizontal, vertical, panoramica, pausa y ayuda a `lwjgl3/build/reports/hud` y comprueba eventos reales de Stage con varios punteros, salto/movimiento y pausa. Requiere un entorno con OpenGL; no verifica dedos fisicos de un telefono.

## Estilo cyberpunk y vida compacta

Se conserva el diseno biselado y neon. `healthCells: 10` en `layout.json` coincide con `player.health: 10` en `assets/enemies/prototype/types.json`: UNA celda equivale a UN punto. Los dos enemigos de prueba causan 1 punto por golpe. La proteccion de 0.8 s tras dano y el reinicio con R se conservan. Cada impacto normal vacia una celda completa y al morir quedan todas vacias; R repone las diez.

La barra mide hasta 200 x 32 unidades del HUD (antes 230 x 122), sin titulo, porcentaje, estados ni avisos adicionales. Solo conserva el efecto magenta de dano y la alerta con dos puntos o menos. Los controles tactiles y pausa son entradas del jugador y permanecen disponibles aparte; en modo teclado se ve unicamente la barra. El modo tactil se cambia desde pausa o F3.

El boton tactil DASH conserva su sombra radial, cuenta regresiva y destello de recarga de 0.6 s. Se elimina la barra informativa adicional de dash. La imagen `desktop-life-only.png` verifica el HUD solo con vida; tambien se exportan las versiones tactiles horizontales, verticales, con dano y con vida critica.
