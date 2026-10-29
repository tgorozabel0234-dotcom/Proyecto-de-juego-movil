# Movimiento y gancho — ajustes de fluidez

## Controles vigentes

- A/D: caminar y orientar horizontalmente.
- W/S: apuntado vertical; combinadas con A/D permiten diagonales.
- H: saltar; mantener permite un salto mas alto y soltar pronto produce uno corto. Acertar una estocada terrestre, rebotar con el barrido inferior normal o llegar a un enemigo con U restaura un salto adicional en aire, consumible una vez y sin acumular cargas.
- J: dash base de 22 unidades/s (antes 24), y mantener para sprint x2.3. Recarga: 0.6 s.
- U: propulsion hacia una superficie, por pulsacion.
- I: pendulo, mantener para conservar anclaje; soltar conserva impulso.
- K: atacar. W/S orientan el ataque; ver assets/combat/prototype/README.md para combinaciones y efectos.
- L/O: reservadas.
- F1: ayuda, F2: diagnostico, F3: controles tactiles, R: reiniciar, M: vista general, Esc/Atras: pausa.

## Cambios de movimiento

Fisica con acumulador a 240 Hz, independiente del render. Se conservan eventos de salto y dash aunque un cuadro no avance ningun tick. Se limita la recuperacion a 0.25 s por cuadro para evitar una acumulacion incontrolada durante bloqueos prolongados.

La carrera acelera en vez de imponer instantaneamente la velocidad. El impulso sobrante se conserva al aterrizar y se reduce con friccion gradual. Frenar en la direccion contraria es mas rapido que dejar que el impulso se agote. Soltar J desactiva el sprint, pero la velocidad desciende progresivamente.

El salto mantiene el buffer de 120 ms antes de aterrizar y agrega 100 ms de tolerancia despues de abandonar un borde. Mantener H reduce la gravedad ascendente hasta 180 ms; soltarla antes corta una vez el ascenso. Estos margenes no habilitan un doble salto por si solos. La excepcion de combate es el salto aereo unico que otorga acertar una estocada iniciada con apoyo, un rebote normal hacia abajo o llegar a un enemigo con U; se limpia al usarlo, aterrizar o reiniciar.

## Propulsion y pendulo

U usa el objetivo valido y acelera hacia el durante unos 70 ms de respuesta, conservando inicialmente la velocidad previa. La velocidad deseada base es 28 unidades/s; si ya existe mayor impulso hacia el objetivo, no lo reemplaza de golpe por uno menor. El viaje termina al aproximarse al objetivo, chocar o agotar el tiempo de seguridad. H cancela el modo conservando el impulso y solo aplica salto si existe apoyo/tolerancia de borde; J puede cancelarlo para entrar en dash. I permite pasar al pendulo.

La cuerda conserva su anclaje y no colisiona con el escenario: siempre es un segmento recto, puede atravesar paredes y plataformas y no crea pivotes en sus esquinas. Las colisiones del cuerpo del jugador siguen activas. Las correcciones de posicion no borran por si solas las velocidades. Si debe conservar la ultima posicion viable, conserva el impulso y elimina despues solo la componente que estiraria la cuerda. Los choques reales anulan la componente bloqueada y conservan la que permite deslizarse.

La resistencia aumenta progresivamente por encima de 30 unidades/s. En pendulo se aplica sobre la velocidad total; en vuelo libre se aplica al exceso horizontal, de modo que una caida no robe velocidad horizontal. La caida tiene limite de 45 unidades/s y existe una proteccion extrema de 60 para el vector total. Estos valores son iniciales para ajustar con pruebas jugables.

Ambos ganchos conservan alcance de 35 y ayuda circular de radio 1.25, sin anclajes libres en aire. U puede llevar al jugador hacia objetivos de prueba cuando esta en el aire; I sigue anclandose a superficies.

## Camara

Vista base de 24 alturas de jugador: 33% mas por eje que la configuracion anterior de 18 y 60% mas que la original de 15. Anticipacion horizontal suave hasta 3.5 alturas de jugador, adaptada al ancho visible. Anticipacion vertical entre -1 y +2. A alta velocidad se aleja suavemente hasta un 10%, respetando zoom manual, limites del mundo y vista general. R restaura la camara.

## Verificacion

`gradlew.bat :core:check` ejecuta la bateria de fisica y comandos. MovementPolishCheck cubre independencia entre 30/60/144/240/360 FPS, entradas en cuadros sin tick, altura variable, tolerancia de borde, ausencia de salto aereo fuera del margen, aterrizaje conservando impulso, friccion, aceleracion de propulsion, cancelaciones y deslizamiento en paredes.

El registro de teclado F2 sigue mostrando las teclas fisicas recibidas. Estas mejoras de software no eliminan los bloqueos de combinaciones del teclado USB. Los controles multitactiles ya estan integrados (ver assets/ui/mobile_hud/README.md). El modulo Android y las pruebas en telefono real siguen pendientes.

Pendulo con acercamiento automatico: al enganchar con I, recoge suavemente la cuerda hasta un radio objetivo de 15 alturas del jugador. Una cuerda inicial mas corta se conserva. La recogida acelera hasta 18 unidades/s y mantiene el impulso tangencial; al alcanzar el radio objetivo elimina solo la velocidad radial de recogida para facilitar el giro. Si el cuerpo choca con un obstaculo, respeta la colision y pausa el acortamiento. La cuerda sigue atravesando el escenario sin colisiones. El HUD muestra ACERCANDO durante la recogida y PENDULO al alcanzar el radio. Alcance de disparo: 35; no cambia por este ajuste.


Radio del pendulo: objetivo maximo de 15 alturas, conservando los enganches inicialmente mas cortos. Se elimina la restriccion de distancia minima al superar la altura del anclaje: la cuerda solo tira cuando se tensa y puede aflojarse. En una pasada por arriba, si ya existe giro, se aplica una vez un impulso tangencial de hasta 5 unidades/s en ese sentido, limitado por la velocidad actual. El impulso se rearma al pasar al menos 0.5 alturas por debajo del anclaje; no se repite cada cuadro ni inventa giro si esta quieto. La cuerda sigue sin colisiones y la camara permanece igual.

## Control de inercia en vuelo libre

Tras dash o gancho, pulsar A/D en contra del movimiento frena a 45 unidades/s2
(antes 18). Acelerar de nuevo en el sentido elegido sigue usando 18 unidades/s2.
Soltar A/D introduce desaceleracion suave de 16 unidades/s2 hacia cero, sin invertir
la velocidad. Mantener la direccion del impulso conserva el exceso de velocidad.
Desde 22 unidades/s, frenar con direccion contraria tarda aproximadamente 0.49 s
en detener el avance previo (antes 1.22 s); sin teclas tarda aproximadamente 1.38 s.
No cambia gravedad, velocidad vertical, fuerza de rebote, salto extra, dash activo,
propulsion activa ni bombeo/frenado del pendulo. Al soltar el pendulo conserva su
velocidad inicial y luego aplica estas reglas de vuelo libre.
Se aplica siempre en vuelo libre con inercia, no solo al atacar. El experimento
de limitar ataques aereos a 4.5 unidades/s sigue deshecho y no debe reactivarse.
