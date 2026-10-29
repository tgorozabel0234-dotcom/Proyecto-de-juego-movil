# Movimiento — cuatro borradores

Pendiente de aprobación por animación; no integrado en el juego. Abrir preview.html.

walk (caminar), run (correr), jump (salto), dash: ocho fotogramas transparentes PNG de 128 × 192 por acción. Cada carpeta conserva el original generado, la hoja exportada y animation.json. Los fotogramas se aislaron para evitar restos de figuras vecinas. Exportación con vecino más cercano y escala común; no se ensanchó ni se estiró cada pose para llenar su celda. El dash necesita más anchura, por eso el conjunto deja margen vertical.

La vista permite pausa, avance individual, velocidad y espejo. Caminar/correr repiten; salto/dash conservan la pose final. Los tiempos son ilustrativos: el salto en juego dependerá de la velocidad vertical y el contacto con el suelo, y el dash debe respetar la duración física ya existente. La vista en el sitio no representa la trayectoria del personaje ni cambia mecánicas.

Revisar continuidad, alternancia de piernas y consistencia anatómica antes de considerar estos ciclos definitivos. Esta generación es un borrador visual, no una animación ya validada dentro del motor. El espejo refleja también la posición del antebrazo metálico; una vista lateral opuesta específica sería trabajo adicional.

Se usó la herramienta integrada ImageGen con el personaje aprobado de pixel_white_v2_shirt/preview_4x.png como referencia. Los prompts completos se conservan en prompts.json. Se solicitaron silueta atlética sin ensancharla, camiseta de manga corta bajo chaleco abierto, pelo blanco y detalles neón constantes; ciclos completos al caminar/correr, compresión-despegue-ápice-caída-aterrizaje al saltar, y anticipación-impulso-recuperación en dash. No reactivar el personaje archivado ni cambiar el sprite del juego hasta aprobación.
