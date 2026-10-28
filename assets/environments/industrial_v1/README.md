# Industrial cyberpunk — colección v1

Assets reutilizables del nivel 00 y de futuros escenarios. Generados con la herramienta integrada ImageGen. No se han conectado aún al código del juego.

## Archivos

- `platform_metal_orange_v1.png`: plataforma horizontal aislada, fondo transparente. También puede usarse como pieza de suelo, conservando proporciones.
- `wall_metal_conduit_v1.png`: panel vertical de pared con conductos, fondo transparente.
- `background_hangar_v1.png`: fondo distante opaco de hangar industrial. Sus pasarelas son decoración, no superficies jugables.
- `manifest.json`: dimensiones, transparencia y rutas relativas para reutilizar los originales.
- `prompts.json`: instrucciones originales de generación para futuras variantes.

## Uso

Estilo 2D ilustrado, acero azul grafito, iluminación naranja y pequeños acentos cian. Los PNG transparentes incluyen margen: al integrarlos se debe usar una región ajustada al objeto, no el lienzo entero, para posicionar superficies y colisiones. No contienen colisiones, animaciones ni lógica del gancho.

Son módulos independientes, no un atlas ni un tileset con uniones sin costura verificadas. Repetirlos requiere revisar las juntas; no estirar el fondo a las dimensiones de todo el mundo. Al implementar la cámara se decidirá su encuadre o repetición.

Conservar estos originales. Guardar cambios o nuevas variantes con sufijo `_v2`, `_v3`, etc. Compartir las texturas al reutilizar piezas en lugar de cargarlas para cada instancia.

El tamaño del nivel sigue pendiente de implementación. El esquema del usuario define un recinto amplio para pruebas de movilidad futuras, con suelo, paredes, una plataforma central y dos laterales superiores.
