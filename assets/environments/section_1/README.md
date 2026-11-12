# Acto I de Light Town

Los dos niveles de campaña se pueden iniciar desde el selector. Una partida nueva comienza en el Callejón 42. El nivel 00 es el escenario de pruebas.

## Cenizas de Neón

- **Callejón 42** (194 unidades). El puesto de ramen apagado y el refugio sobre la calle anticipan el regreso. El taller divide la travesía de tejados y el andén final prepara el ascenso.
- **Torre de bombeo** (112 de altura). Las losas alternan los extremos abiertos. Descansos laterales junto a las bombas rompen el ritmo del ascenso. La pasarela superior desemboca en las grúas.
- **Patio de grúas** (206 de ancho). El primer par de anclajes tiene una red de recuperación. Más adelante se puede subir a la cabina en desuso antes de la cadena final de péndulos.
- **Terminal de suministro** (198 de ancho). Las terrazas conducen al registro del transporte de Lila. Desde el último andén se puede volver al barrio por la puerta elevada o seguir hacia los conductos. Ambas salidas habilitan la compuerta del callejón.

## Conductos de Central Corp

- **Colector del distrito**. La entrada inferior muestra la compuerta de retorno por encima del jugador. El indicador de caudal revela el desvío de energía. La salida de la derecha conduce al circuito de refrigeración.
- **Galería de refrigeración**. Una rotura divide la conducción superior. El gancho permite atravesarla directamente. Las plataformas del drenaje permiten recuperarse bajo la tubería y volver a subir.
- **Cámara de conmutación**. El recorrido dobla sobre sí mismo. El robot bajo las ménsulas admite estocada ascendente y los escalones y anclajes ofrecen otra subida. El archivo superior revela la compatibilidad de Lila. Cruzar la puerta del conmutador restablece la energía.
- **Retorno de servicio**. El intercomunicador de Celestia acompaña el descenso. La salida conduce al colector alto, desde donde se vuelve al callejón por la izquierda. El ramen vuelve a encenderse y el refugio abre su salida de fin de acto.

Los mapas viven en `assets/levels`. Los portales definen destino, llegada, condición y desbloqueo. Las banderas de energía y compuerta se incluyen en el guardado manual. Un guardado anterior al cambio comienza con los circuitos cerrados. El borde claro señala superficies físicas y el aro cian identifica un anclaje. Los carteles pertenecen al escenario. El combate usa los perfiles existentes de Robot y Scorp.

La comprobación de escritorio `gradlew.bat :lwjgl3:sectionPreviewCheck --offline` captura la vista general, entrada, tramo medio y salida de las ocho salas en `lwjgl3/build/reports/section-1`.

## Instalaciones del acto I

Los tramos con rol `pipe` son sólidos de movimiento y de gancho. Al pulsar salto en el aire junto a su extremo se rebota hacia fuera. Las válvulas de suelo tienen un ciclo de espera, aviso ámbar y descarga de vapor. El contacto con la columna activa causa un punto de daño, respeta el intervalo de protección del jugador y admite cruzarla durante el dash. La pausa detiene tanto la descarga como su animación. Posición, dimensiones y tiempos se especifican en `vents` en cada mapa.

## Revisión de recorrido (fluidez y recuperación)

Las reglas están en `assets/levels/traversal_rules.json` y `:core:levelFlowCheck`, incluida en `:core:check`, las verifica en las 8 salas con la física real de `PlayerBody`. La prueba cubre cuatro casos: salir caminando de cualquier borde nunca cae al vacío, no quedan bordes pequeños entre superficies contiguas, las entradas y llegadas apoyan en el suelo y los muros laterales superan el alcance del gancho.

Criterios aplicados:
- **Sin bordes que atasquen.** `PlayerBody` no sube escalones, así que un borde de solo 0,1 detenía al jugador. Las tuberías que unen tramos de una losa o de una plataforma quedan a ras.
- **Redes de recuperación.** Las pasarelas `suspended` bajo los huecos se sitúan a 1,6 bajo el lado bajo, para volver a subir de un salto, y a menos de 1 bajo el lado alto, para que el jugador no quepa debajo. Bajo los péndulos se colocan a 3,2 para no poder cruzarlos caminando, y los postes cierran el paso bajo las plataformas finas. Una caída cuesta repetir el tramo, no la sala.
- **Subidas de 1,5–1,7** en el camino principal. El salto alcanza 1,9, así que las subidas de 2,0 quedaban al límite y las de 3,0 obligaban a usar el gancho.
- **Vapor y enemigos** al menos a 3 unidades de cada aterrizaje y lejos de los bordes de despegue.

Por sala:
- **Callejón 42.** Los muros de 6 (x59, x116) pasan a escombros saltables de 1,3. Los huecos de 6 que exigían sprint quedan en 3–3,5. La hondonada del taller ya no es una trampa y hay redes en cada hueco. El camino principal se recorre sin sprint ni gancho. No cambian los Matones, el Capataz, las zonas de historia ni el bloqueo de la salida.
- **Torre de bombeo.** Las 16 tuberías de las losas quedan a ras. Dos tuberías flotantes suben o bajan para no cortar el salto. El ascenso sigue siendo con gancho, que es la identidad de la sala.
- **Patio de grúas.** Hay red de rescate bajo los 8 péndulos (antes solo bajo 2 y medio). La tubería del primer péndulo sirve de escalón de vuelta y las redes de los dos siguientes tienen escaleras. El vapor y los enemigos salen de los aterrizajes. La cadena de péndulos se mantiene.
- **Terminal de suministro.** Hay un escalón intermedio en cada subida de 3. El gancho y sus anclajes siguen como atajo. El muro de 6 en la terraza del robot y el vapor desaparece, el otro muro pasa a parapeto, el vapor ya no está en un despegue y la llegada a la salida sube 1,5.
- **Colector del distrito.** Las subidas de 2,0 pasan a 1,7–1,8, las tuberías quedan a ras, el vapor ya no está junto a un despegue y hay redes en todo el camino inferior.
- **Galería de refrigeración.** El drenaje es ahora un camino completo hasta la salida: el gancho en la rotura queda como atajo, como dice el cartel. La subida de 3 previa tiene un escalón y la tubería sobre el drenaje deja 1,8 de altura libre.
- **Cámara de conmutación.** La escalera de ménsulas se rehace con escalones de 1,5, sin solapes ni techos bajos. El circuito superior sube de 1,5 en 1,5 y el vapor sale de la plataforma estrecha. Se mantiene el atajo de gancho desde la entrada.
- **Retorno de servicio.** El suelo inferior tiene escalera de vuelta hacia adelante y un poste cierra el vacío. Las tuberías quedan a ras y el vapor ya no está sobre la plataforma de 3,4.

Se conservan los atajos (compuerta y retorno al barrio), los puntos de llegada, las escenas y el bloqueo del Capataz. `layout-baseline.json` de `cyberpunk_v1` registra los hashes anteriores a esta revisión.
