# Caída — borrador 01

Solo animación de caída, pendiente de aprobación. No integrada al juego.

Dos poses con funciones distintas: entrada al descenso y caída sostenida, con piernas extendidas y brazos equilibrando el cuerpo. La primera dura 120 ms en la muestra; la segunda se conserva indefinidamente hasta tocar una superficie o cambiar de estado. No se repite en bucle, no simula pasos en el aire y no contiene aterrizaje, daño ni muerte. No fija una cantidad de fotogramas para otras acciones.

Abrir preview.html para revisar ambas poses, repetir la transición o verla a mitad de velocidad. La muestra presenta poses en el sitio; el desplazamiento vertical real depende de la física y no debe pintarse como una trayectoria dentro del sprite.

frames/: dos PNG RGBA transparentes de 128 × 192. Hoja sheet_256x192.png de dos columnas. Recortes medidos con margen alrededor de botas, manos y cabeza; misma escala en ambas poses, cadera alineada a (64,92), pivote del lienzo (64,184). Extender las piernas cambia la altura de las botas, sin desplazar artificialmente todo el cuerpo.

La generación original es de mayor tamaño que las hojas de carrera; la escala de exportación se eligió para conservar un tamaño de cabeza comparable. Exportación con vecino más cercano, sin estirar la figura. master_generated.png conserva el original. Generado con ImageGen integrado; prompt completo en ../prompt.json. Se mantiene el personaje aprobado: pelo blanco, camiseta de manga corta bajo chaleco abierto, neón y antebrazo metálico izquierdo.

No modificar física, controles, colisiones, daño ni el sprite activo al revisar estos recursos. Caminar y correr tienen aprobación del usuario; esta caída todavía debe revisarse individualmente. El aterrizaje se hará aparte cuando se solicite.
