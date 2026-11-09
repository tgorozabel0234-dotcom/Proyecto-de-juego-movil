package io.github.some_example_name;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import com.badlogic.gdx.utils.JsonValue;

/** One manual checkpoint, independent of the renderer and frame rate. */
final class SaveStore {
    static final int CURRENT_VERSION = 3;

    static final class Snapshot {
        public int facing = 1;
        public int version;
        public String level;
        public String[] flags = new String[0];
        public float x = Float.NaN, y = Float.NaN, health = Float.NaN;
        public HashMap<String, Float> enemies;
    }

    private final Path file;
    SaveStore(Path file) { this.file = file; }

    static SaveStore local() {
        Path directory = Gdx.app.getType() == ApplicationType.Desktop
            ? Paths.get(System.getProperty("user.home"), ".cyberpunk-metroidvania")
            : Gdx.files.local("saves").file().toPath();
        return new SaveStore(directory.resolve("partida.json"));
    }

    private static Json codec() {
        Json json = new Json(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        json.setElementType(Snapshot.class, "enemies", Float.class);
        return json;
    }

    private static void validate(Snapshot data) throws IOException {
        if (data == null || data.version != CURRENT_VERSION || Math.abs(data.facing) != 1 || data.level == null
            || !data.level.matches("levels/[A-Za-z0-9_-]+\\.json")
            || !Float.isFinite(data.x) || !Float.isFinite(data.y)
            || !Float.isFinite(data.health) || data.health <= 0 || data.enemies == null)
            throw new InvalidSave("Partida incompatible o incompleta");
        for (Float hp : data.enemies.values())
            if (hp == null || !Float.isFinite(hp) || hp < 0)
                throw new InvalidSave("Vida de enemigo invalida");
    }

    private Path backup() {
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    private void replace(Path target, byte[] bytes) throws IOException {
        Path pending = Files.createTempFile(file.getParent(), "partida-", ".tmp");
        try {
            Files.write(pending, bytes);
            try {
                Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(pending);
        }
    }

    private static final class InvalidSave extends IOException {
        InvalidSave(String reason) { super(reason); }
    }

    void write(Snapshot data) throws IOException {
        validate(data);
        Files.createDirectories(file.getParent());
        byte[] encoded = codec().toJson(data).getBytes(StandardCharsets.UTF_8);
        if (Files.exists(file)) {
            byte[] previous = Files.readAllBytes(file);
            try {
                decode(previous);
                replace(backup(), previous);
            } catch (InvalidSave corrupt) {
                // Conservar el respaldo válido si el principal está dañado.
            }
        }
        replace(file, encoded);
    }

    Snapshot read() throws IOException {
        try {
            return decode(Files.readAllBytes(file));
        } catch (InvalidSave | NoSuchFileException damaged) {
            if (!Files.exists(backup())) throw damaged;
            return decode(Files.readAllBytes(backup()));
        }
    }

    private Snapshot decode(byte[] bytes) throws IOException {
        String content = new String(bytes, StandardCharsets.UTF_8);
        try {
            JsonValue root = new com.badlogic.gdx.utils.JsonReader().parse(content);
            Snapshot data = new Snapshot();
            int version = root.getInt("version");
            if (version > CURRENT_VERSION) throw new IOException("La partida requiere una versión posterior del juego");
            if (version < 1) throw new InvalidSave("Versión inválida");
            data.version = CURRENT_VERSION;
            data.facing = version == 1 ? 1 : root.getInt("facing");
            data.level = root.getString("level");
            data.x = root.getFloat("x");
            data.y = root.getFloat("y");
            data.health = root.getFloat("health");
            if (version >= 3) data.flags = root.get("flags").asStringArray();
            data.enemies = new HashMap<>();
            JsonValue enemies = root.get("enemies");
            if (!enemies.isObject()) throw new InvalidSave("Estado de enemigos invalido");
            for (JsonValue e = enemies.child; e != null; e = e.next)
                data.enemies.put(e.name, e.asFloat());
            validate(data);
            return data;
        } catch (RuntimeException e) {
            throw new InvalidSave("Partida ilegible: " + e.getMessage());
        }
    }
}

