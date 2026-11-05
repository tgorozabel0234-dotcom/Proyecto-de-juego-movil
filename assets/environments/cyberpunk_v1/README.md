# Ambiente cyberpunk v1

Cambio exclusivamente visual autorizado: los cuatro JSON de nivel conservan sus bytes, recorridos, posiciones, tamaños, colisiones, puntos de entrada y salida. layout-baseline.json registra sus SHA-256 al iniciar este cambio. La física, combate y controles no se modificaron.

## Recursos generados e integrados

- city.png: ciudad industrial nocturna; fondo de bajo contraste con neón y profundidad.
- metal.png: módulo de metal con conductos, ventilación y luces para plataformas y paredes.
- door.png: puerta cian/magenta con indicador verde, RGBA transparente.
- anchor.png: acople mecánico luminoso, RGBA transparente.

Generados con la herramienta integrada image_gen. Las cuatro referencias del usuario guiaron el estilo; el fondo usó las cuatro imágenes como entradas de referencia. Los prompts completos por recurso se conservan en prompts.json. Los originales generados se copiaron a este directorio sin modificarlos.

render.json contiene rutas, tamaños visuales, intensidad del fondo, paralaje y pivotes. ReadableLevelScene es dueño de las cuatro texturas y las libera al cambiar de sala. Los paneles se repiten y recortan dentro de los sólidos originales. El borde superior continúa mostrando el apoyo real. Los anclajes usan el centro físico existente; su cable es decorativo. La puerta se dibuja sobre la salida ya existente sin alterar su área de activación. Los soportes y cables oscuros inferiores son decorativos. El fondo repite su imagen con espejo alternado para evitar cortes visibles y se desplaza al 20% del movimiento de cámara. No se han añadido plataformas ni objetos de juego.

## Verificación

Pasaron :lwjgl3:classes, :core:check y :lwjgl3:sectionPreviewCheck --offline. Se exportaron 16 vistas (general, entrada, centro y salida de cada sala) a lwjgl3/build/reports/section-1; se inspeccionaron vistas cercanas de las cuatro salas. Los hashes de los cuatro niveles coinciden con el estado anterior al cambio visual. No se realizó una partida completa ni una prueba en hardware móvil.
