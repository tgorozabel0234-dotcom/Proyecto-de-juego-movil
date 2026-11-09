package io.github.some_example_name;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class SaveStoreCheck {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("metroidvania-save");
        Path file = directory.resolve("partida.json");
        SaveStore store = new SaveStore(file);
        try {
            SaveStore.Snapshot initial = new SaveStore.Snapshot();
            initial.version = SaveStore.CURRENT_VERSION;
            initial.facing = -1;
            initial.level = "levels/section_1_room_3.json";
            initial.x = 15.25f;
            initial.y = 3;
            initial.health = 7;
            initial.enemies = new HashMap<>();
            initial.flags = new String[] {"power","shutter"};
            initial.enemies.put("robot", 0f);
            store.write(initial);
            initial.health = 4;
            store.write(initial);
            SaveStore.Snapshot loaded = new SaveStore(file).read();
            if (!java.util.Arrays.equals(initial.flags, loaded.flags) || loaded.facing != -1 || loaded.health != 4 || loaded.x != initial.x || loaded.y != initial.y ||
                !initial.level.equals(loaded.level) || !initial.enemies.equals(loaded.enemies))
                throw new AssertionError("Checkpoint incompleto");
            initial.version = 99;
            try {
                store.write(initial);
                throw new AssertionError("Version desconocida aceptada");
            } catch (IOException expected) {
                if (store.read().version != SaveStore.CURRENT_VERSION) throw new AssertionError("Se perdio la partida anterior");
            }
            Files.write(file, new byte[] {123});
            if (store.read().health != 7) throw new AssertionError("No recupera el respaldo");
            Files.delete(file.resolveSibling("partida.json.bak"));
            try {
                store.read();
                throw new AssertionError("Partida truncada aceptada");
            } catch (IOException expected) { }
            String legacy = "{version:1,level:levels/section_1.json,x:3,y:2,health:8,enemies:{}}";
            Files.write(file, legacy.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            if (store.read().health != 8 || store.read().facing != 1) throw new AssertionError("Error al migrar v1");
        } finally {
            Files.deleteIfExists(file.resolveSibling("partida.json.bak"));
            Files.deleteIfExists(file);
            Files.delete(directory);
        }
    }
}
