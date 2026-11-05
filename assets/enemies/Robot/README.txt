VOLTAIC SLINKER — LibGDX sprite pack
====================================
Contents:
- voltaic_slinker.png: RGBA transparent atlas (8 columns x 6 rows, 224x208 cells).
- voltaic_slinker.atlas: LibGDX TextureAtlas descriptor.
- voltaic_slinker.json: animation metadata.
- frames/: 32 separate PNG frames, all 224x208 with transparent background.

Animations and frames:
idle: 6 (loop)
walk: 8 (loop)
swipe: 4 (once)
static_attack: 6 (once)
get_hit: 2 (once)
death: 6 (once)

Copy voltaic_slinker.png + voltaic_slinker.atlas to android/assets/ (or assets/).

LibGDX example:
    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("voltaic_slinker.atlas"));
    Animation<TextureRegion> idle = new Animation<>(0.13f, atlas.findRegions("idle"), Animation.PlayMode.LOOP);
    Animation<TextureRegion> walk = new Animation<>(0.10f, atlas.findRegions("walk"), Animation.PlayMode.LOOP);
    Animation<TextureRegion> swipe = new Animation<>(0.09f, atlas.findRegions("swipe"), Animation.PlayMode.NORMAL);
    Animation<TextureRegion> staticAttack = new Animation<>(0.11f, atlas.findRegions("static_attack"), Animation.PlayMode.NORMAL);
    Animation<TextureRegion> hit = new Animation<>(0.15f, atlas.findRegions("get_hit"), Animation.PlayMode.NORMAL);
    Animation<TextureRegion> death = new Animation<>(0.17f, atlas.findRegions("death"), Animation.PlayMode.NORMAL);

    // In render():
    TextureRegion current = walk.getKeyFrame(stateTime);
    batch.draw(current, x, y, 112, 104); // half scale, adjust to your game

IMPORTANT: The original was a JPEG collage, not an actual spritesheet.
The transparent mattes have been estimated. Some JPG artifacts, edge roughness,
and frame inconsistencies from the original art remain. For perfect animation,
source transparent frames created by the original artist would be better.
