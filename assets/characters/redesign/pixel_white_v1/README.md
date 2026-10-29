# Redisenyo pixel art, cabello blanco v1

Propuesta visual basada en concepto_fusion_neon_v1.png, con cabello blanco/plata,
misma pose y silueta, piel calida, ropa oscura, paneles burdeos, antebrazo metalico
y lineas de neon cian/magenta. No integrada al juego: es una propuesta para revisar.

- character_128x192.png: exportacion final EXACTA 128 x 192, PNG RGBA transparente.
- preview_4x.png: vista ampliada 512 x 768 por vecino mas cercano para ver pixeles.
- master_generated.png: fuente generada de 1024 x 1536, sin modificar, para referencia.

Se genero el arte con la herramienta de imagen; la exportacion a la resolucion
que eligio el usuario usa vecino mas cercano, sin suavizado ni deformar la forma
(ambos lienzos tienen proporcion 2:3). La vista 4x no agrega detalle nuevo.

Para integrarlo al juego, usar character_128x192.png y TextureFilter.Nearest.
Evitar filtrado Linear y escalado no entero en previews de pixel art. Para una
animacion futura, conservar lienzo, escala y pivotes comunes en todos los frames.
El PNG mantiene transparencia; para trabajar por capas conviene crear despues un
archivo fuente editable en el editor de pixel art elegido.