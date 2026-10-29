Estado actualizado: integrado por autorización expresa del usuario. Consultar ../integration_v1/README.md. Las indicaciones de revisión previa que siguen describen la etapa de generación de este paquete.

# Acciones restantes — paquete de revisión 01

El usuario autorizó generar el resto de animaciones conjuntamente. Este paquete contiene 14 acciones nuevas (36 fotogramas) para su revisión; no están integradas al personaje activo del juego. Se omitió el aterrizaje por instrucción expresa. Abrir **preview.html**.

## Criterio visual acordado

Conservar el personaje aprobado: pelo blanco, ojos cian, camiseta oscura de manga corta bajo chaleco abierto sin mangas, líneas neón cian/magenta, bolsillos burdeos y antebrazo metálico izquierdo. Pocos fotogramas con funciones distintas; no fijar tres, seis u ocho por costumbre ni repetir poses sin aportar movimiento. Caminar de 4 poses, correr de 6 y caída de 2 ya fueron aprobadas y se mantienen aparte.

La hoja de luz es recta, cian con núcleo blanco, nace del antebrazo metálico y sobresale ampliamente del cuerpo. Las estelas indican el barrido. Se amplió el lienzo de combate a 384 × 384 para contener puntas, estelas y ataques verticales sin comprimir el personaje. Los demás recursos usan 256 × 256, también con margen para rotarlos. Son tamaños de lienzo, no cambios de tamaño físico del personaje: conservar los pivotes y las escalas al integrar. Las hojas de locomoción anteriores siguen siendo 128 × 192.

## Recursos

| Acción | Poses | Comportamiento de la muestra |
|---|---:|---|
| Barrido lateral | 3 | Preparación, golpe, seguimiento |
| Barrido superior | 3 | Preparación, corte ascendente, seguimiento |
| Ataque aéreo inferior | 3 | La tercera pose pertenece al rebote por impacto |
| Estocada | 4 | Tres poses por salida; recuperación o impacto/subida |
| Aura | 2 | Activación y mantenimiento visual |
| Ataque rápido desde péndulo | 3 | La salida ascendente pertenece al impacto |
| Torbellino | 3 | Bucle de barridos; la cuerda orienta el cuerpo por separado |
| Disparo del gancho | 2 | Preparación y lanzamiento |
| Propulsión | 2 | Entrada y postura sostenida hacia el objetivo |
| Péndulo | 1 | Postura sostenida orientable, sin giro prefijado |
| Salida del gancho | 2 | Liberar mano y recuperar postura aérea |
| Levantarse | 3 | Rodilla, incorporación, postura de pie |
| Recibir daño | 2 | Reacción y recuperación |
| Muerte | 3 | Pérdida de apoyo, colapso, postura final |

Cada carpeta contiene PNG RGBA individuales, una hoja regular **sheet.png**, **animation.json** con pivotes/tiempos/punto de mano, y los originales seleccionados. **manifest.json** reúne el inventario y referencias a lo aprobado. **catalog.js** permite abrir la galería directamente como archivo local sin depender de peticiones de red. No borrar los originales ni las versiones previas.

Se utilizó la herramienta integrada **ImageGen**, con el personaje como referencia y correcciones de recorte/vestuario cuando fueron necesarias. Los prompts finales están en **prompts.json**, incluida la pose superior independiente. Se midieron regiones y se exportó con vecino más cercano. Los recortes excluyen fragmentos de figuras vecinas; no asumir que los originales generados usan celdas uniformes.

## Vista del gancho

La demostración permite balanceo, ángulo manual y vuelta completa de prueba. La mano es la unión de la cuerda; el eje mano-cadera sigue la dirección radial y la orientación lateral acompaña el sentido tangencial. Al pasar por encima del anclaje el personaje queda invertido. La propulsión alinea la mano y el cuerpo al objetivo. El torbellino anima el barrido mientras el cuerpo sigue la cuerda; no reproduce un giro independiente de la trayectoria.

**orientation.js** implementa estas transformaciones visuales, sin lógica de daño ni físicas del juego. La trayectoria de la muestra no es un péndulo físico nuevo. Consultar **integration-notes.md** antes de conectarlo al motor.

## Límites de esta entrega

Los tiempos son ilustrativos. La subida de estocada, rebote y ataque rápido se selecciona con **Simular impacto**; el juego debe dispararla únicamente al detectar un enemigo. El torbellino no causa daño durante su bucle. No se han alterado controles, alcance, colisiones, vida, velocidad ni sprite activo. El alcance visible de la hoja debe cotejarse con el área real de daño al integrar; estas imágenes no la cambian.

El disparo incluido ofrece la pose base hacia la derecha. Para apuntar desde el suelo sin rotar el torso será necesario separar/animar la capa de brazo durante la integración; no resolverlo girando de forma indiscriminada todo el cuerpo del personaje apoyado. La prueba inferior cubre la orientación aérea del péndulo y de la propulsión. El espejo de revisión refleja también los detalles asimétricos; una vista lateral opuesta específica requiere arte adicional si se desea conservar la anatomía izquierda/derecha exacta.

La aprobación sigue siendo por animación. Estos recursos son un conjunto revisable, no una afirmación de acabado definitivo dentro del juego.

