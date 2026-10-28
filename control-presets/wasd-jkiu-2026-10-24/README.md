# Respaldo de controles: WASD + J/K/I/U

Este directorio conserva la distribucion activa antes de reasignar A/S. controls.json documenta el mapa y TestLevelScreen.java.snapshot conserva el archivo exacto que lo implementa.

No es un selector de perfiles dentro del juego. Para recuperar estas asignaciones se puede consultar el JSON y aplicar el mapa al codigo vigente; reemplazar todo el snapshot solo procede si se desea restaurar tambien la version de pantalla guardada.

La nueva asignacion A=propulsion, S=pendulo, D/C=reservadas queda pendiente de definir las teclas de movimiento/apuntado, salto y dash, pues entra en conflicto con WASD.
