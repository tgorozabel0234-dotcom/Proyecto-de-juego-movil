Estado actualizado: integrado por autorización expresa del usuario. Consultar ../integration_v1/README.md. Las indicaciones de revisión previa que siguen describen la etapa de generación de este paquete.

# Contrato visual de integración

No activar automáticamente estos sprites. El personaje activo continúa siendo el prototipo amarillo hasta que el usuario apruebe la integración. No se incluyó ni debe añadirse una animación de aterrizaje contra su instrucción.

## Coordenadas y escala

Los metadatos usan coordenadas de imagen: X hacia la derecha, Y hacia abajo. `frameWidth/frameHeight` son el lienzo con margen. Cada frame conserva `pivot`, `hip`, `hand`, región original y escala de exportación. No estirar todo el lienzo al tamaño del cuerpo/collider: eso reduciría al jugador al añadir un arma larga. Usar una escala de píxeles a unidades, anclar el pivote al cuerpo y mantener el collider independiente. El punto `hand` es una guía visual medida; ajustar al puerto real del gauntlet en la revisión final.

## Péndulo: eje y tangente

Sean A el anclaje, H la mano mundial, P la cadera y u = normalizar(H − A). En el sprite base, v = cadera − mano. El cuerpo debe rotar para alinear v con u, aplicando el espejo antes de calcular esa rotación. Tras transformar el sprite, volver a calcular H desde el punto de mano, no desde el centro ni los pies. Dibujar la cuerda desde A hasta H.

Con un ángulo θ medido desde abajo: u = (sin θ, cos θ). El giro se obtiene de la diferencia entre atan2(u.y,u.x) y atan2(v.y,v.x). La dirección lateral se selecciona por el signo de la velocidad angular, conservando el último signo cuando está prácticamente en cero para evitar parpadeos. Se admiten θ mayores de 90° y ±180°: no bloquear la inversión ni fijar una animación circular que ignore la posición real.

La demostración `SpriteOrientation.pendulum()` devuelve matriz, mano, cadera y tangente. No impone tensión, longitud física, reeling, colisiones ni velocidad. Integrarlo con el estado real ya calculado por PlayerBody, sin sustituir su sistema de movimiento.

En torbellino la mano cambia con el barrido, por lo que no se debe recalcular una rotación corporal diferente a partir de esa pose y provocar giros bruscos. `orbitingBody()` mantiene el eje del cuerpo ligado al ángulo orbital y transforma después el punto de mano de cada frame; la cuerda termina en esa mano. La integración real debe tomar la posición del cuerpo del motor y su ángulo actual, sin forzar una órbita ni mover al jugador por cambiar de imagen.

## Propulsión

La dirección visual es objetivo − cadera. Alinear el vector mano − cadera del sprite hacia esa dirección, con espejo elegido para el lado correcto. `SpriteOrientation.propulsion()` muestra el cálculo. La cuerda se conecta a la mano transformada. No frenar ni reiniciar momentum por cambiar de frame. La pose de lanzamiento base y el apuntado de brazo desde el suelo requieren tratamiento de brazo separado: no usar toda la rotación aérea como solución al apuntado terrestre.

## Combate y eventos

- La hoja de luz nace del antebrazo metálico izquierdo. El área real de daño se define en el sistema de combate; esta entrega no modifica `attacks.json`. Comparar punta/estela con el alcance durante la integración.
- `thrust` ofrece `miss=[0,1,2]` y `hit=[0,1,3]`. Solo el evento de impacto inicia la rama de subida. Mantener inicio desde suelo/superficie, carrera inicial y condiciones actuales de reseteo de salto. No elevar por terminar el temporizador del ataque.
- `attack_down` y `hook_strike`: la última pose de rebote/subida pertenece a `hit`, nunca al fallo.
- `whirlwind`: el bucle visual no causa daño. Al contactar con enemigo, el sistema existente libera la cuerda conservando momentum. Los frames llevan puntos de mano propios; orientar cada uno con la cuerda, no sumar un giro corporal automático.
- No cambiar aturdimiento: enemigos comunes conservan 0,75 s según docs/gameplay/enemy-stun.md, con excepciones configuradas para inmunes. No congelar jugador/mundo desde el renderer.
- Daño, muerte y aura responden a sus estados reales; no implementar curación, invulnerabilidad, daño o reseteos desde la animación.

## Estados mantenidos

Péndulo: pose sostenida durante el enganche. Propulsión: segunda pose mientras dure el tirón. Muerte: último frame hasta reinicio. Aura: pose final solo para revisar el efecto, no bloquear el control del jugador. Levantarse: terminar en postura de pie y continuar con el estado solicitado; no añadir una pausa de entrada que anule salto/dash/ataque.

Estos datos y la galería son borradores para aprobación, no una integración terminada. Al conectar código real, ejecutar las comprobaciones exigidas por AGENTS.md y revisar las acciones en el motor, incluida la orientación a ambos lados y el caso invertido.

