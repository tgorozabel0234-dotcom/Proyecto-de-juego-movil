# Pantalla principal

key_art_v1.jpg es la imagen proporcionada por el usuario, copiada sin editar. MenuScreen la carga al abrir el juego y al volver al menú.

Se dibuja con Scaling.fill: conserva proporciones y recorta los bordes necesarios para cubrir cada pantalla. En horizontal, título y botones se ubican a la izquierda para dejar visibles los personajes y la torre. En vertical, se centran. Se conservan las acciones del menú y la transición al selector de niveles. El archivo fondo_menu.jpg anterior queda como referencia.

Verificación: :lwjgl3:classes y :lwjgl3:menuPreviewCheck --offline completados. Se revisaron las capturas del menú horizontal, selector y menú vertical en lwjgl3/build/reports/menu. No es una prueba de dispositivo móvil físico.
