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
