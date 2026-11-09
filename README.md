# DROP

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Reglas de desarrollo para el equipo e IAs

Consultar [AGENTS.md](AGENTS.md) y las [reglas de aturdimiento de enemigos](docs/gameplay/enemy-stun.md) antes de modificar enemigos o combate.

## Guardar y cargar partida

Abre pausa con Esc o II y pulsa GUARDAR. CARGAR PARTIDA en el menu principal
recupera el ultimo guardado manual. Iniciar juego comienza en la seccion 1 y
conserva el archivo hasta el siguiente guardado. El punto de control incluye
sala, posicion, vida y vida de los enemigos de las salas visitadas. Los enemigos vuelven
a sus posiciones iniciales y los movimientos, ataques y temporizadores se reinician.
En escritorio el archivo es ~/.cyberpunk-metroidvania/partida.json.
El formato JSON lleva version; SaveStoreCheck comprueba la lectura y reemplazo.

El formato v3 conserva el estado de la compuerta del Callejón 42 y la energía del barrio. También guarda la orientación del jugador y asocia la vida de cada enemigo con su identificador estable dentro de la sala. Al editar niveles, conserva el id cuando cambies la posición del enemigo y asigna uno nuevo a cada instancia nueva. El lector admite archivos v1 con claves de posición y v2 con identificadores estables. Si el punto guardado queda dentro de una pared o fuera de la sala, el jugador reaparece en la entrada. Al reemplazar una partida válida, se conserva la anterior en partida.json.bak. La carga recupera esa copia si el principal falta o está dañado.
