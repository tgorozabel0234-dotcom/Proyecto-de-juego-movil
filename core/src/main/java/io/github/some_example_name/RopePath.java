package io.github.some_example_name;

import java.util.ArrayList;
import java.util.List;

/** Cuerda recta sin colisiones: las superficies solo sirven para fijar el anclaje inicial. */
final class RopePath {
    private final List<float[]> path = new ArrayList<>(1);
    private final float[] anchor = new float[2];

    // Se conserva la firma para los llamadores existentes, pero no se consulta la geometria.
    RopePath(List<float[]> ignoredSolids) {}
    List<float[]> getPoints() { return path; }
    void clear() { path.clear(); }

    void update(float startX, float startY, float anchorX, float anchorY) {
        anchor[0] = anchorX;
        anchor[1] = anchorY;
        if (path.isEmpty()) path.add(anchor);
    }

    float length(float startX, float startY) {
        return path.isEmpty() ? 0 : (float) Math.hypot(anchor[0] - startX, anchor[1] - startY);
    }
}