# Caminar y correr: alternancia de piernas — v3

Pendiente de aprobación. No integrado en el juego.

El usuario aclaró que el defecto original era la repetición de la misma pierna, no el número de fotogramas ni solamente la fluidez. No reducir una animación a tres fotogramas por obligación. Eliminar solo poses que no aporten y conservar un ciclo completo con ambas piernas.

Se generaron nuevas poses con ImageGen, no se volvieron a seleccionar las tres poses fallidas. Cada ciclo tiene cuatro fotogramas útiles: contacto/apoyo de una pierna, paso o vuelo hacia la contraria, contacto/apoyo contrario y retorno. La pierna cercana tiene el bolsillo burdeos grande; la lejana lleva el muslo liso. En los contactos 00 y 02 la pierna adelantada es distinta. Caminar incluye contrabalanceo del brazo mecánico. Estas son muestras estilizadas para validar poses y ritmo; no se consideran aprobadas ni una animación final dentro del motor.

PNG transparentes de 128 × 192; hojas de 512 × 192, cuatro columnas. Todos se exportan con escala común, sin estirar cada pose y con pivote (64,184). Se midieron regiones y ejes de cadera para mantener las botas completas. Los originales se conservan en cada carpeta y los prompts se incluyen en prompts.json. Se utilizó generación y edición integrada de ImageGen; exportación final con vecino más cercano.

Abrir preview.html y usar cámara lenta o avance individual. La información por fotograma señala qué pierna apoya. Los tiempos son de muestra: no modifican movimiento, colisiones, daño, controles ni sprite activo. Salto y dash se referencian desde las versiones existentes y no se redibujaron. Las vistas anteriores apuntan ahora a estos ciclos; sus PNG y metadatos históricos se conservan para comparación.
