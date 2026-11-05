# Sección 1: reinterpretación jugable

Rediseño autorizado para priorizar claridad. Las cuatro imágenes anteriores quedan como referencias, sin usarse como superficies del juego.

1. Acceso extendido: 168 unidades de ancho (antes 48), nueve plataformas, huecos graduales, dos obstáculos y ocho anclajes.
2. Torre de ascenso: 64 por 100 unidades (antes 36 por 36). Siete pisos separados por 12 unidades. Las losas alternan el paso libre de lado, obligando a cruzar la torre para continuar subiendo. Anclajes visibles en cada paso.
3. Cadena de péndulos: 184 unidades de ancho (antes 56), siete cruces con anclajes y ocho plataformas de descanso a distintas alturas. Huecos de 13 a 15 unidades.
4. Distrito terminal: 172 unidades de ancho (antes 48). Diez terrazas que ascienden de 2 a 25 unidades, nueve anclajes y dos obstáculos intermedios. Combina saltos, tirón y balanceo hasta la puerta final.

Ampliación solicitada para que el recorrido dure más con movimiento experto. No se modifica la velocidad del jugador ni se añaden esperas. Los huecos conservan distancias abordables con la física existente. No hay puntos de control intermedios: R o una caída profunda reinician la sala actual.

Borde claro = superficie sólida; aro cian = punto de gancho; bordes ámbar = extremos de plataforma; puerta verde con flecha = salida. Los edificios oscuros del fondo son decorativos. Las paredes y plataformas también admiten el gancho existente. La geometría visible usa exactamente las cajas cargadas en PlayerBody, sin trazar colisiones sobre ilustraciones.

Los datos reutilizables están en assets/levels/section_1.json y section_1_room_2.json a section_1_room_4.json; ReadableLevelScene dibuja esa geometría. Caer por debajo del límite o pulsar R reinicia la sala. Cada puerta restaura vida en la siguiente; la última devuelve al menú. No hay enemigos en estas salas de recorrido. Se conservan los controles y la física.

Verificación visual: gradlew.bat :lwjgl3:sectionPreviewCheck --offline. Capturas en lwjgl3/build/reports/section-1. La revisión visual no sustituye una partida completa.

## Arte cyberpunk activo
La presentación usa los cuatro recursos de assets/environments/cyberpunk_v1. Consultar su README y render.json. Los datos de recorrido de esta sección permanecen idénticos a la ampliación aprobada; el render de metal, neón y ciudad reemplaza únicamente la apariencia anterior.
