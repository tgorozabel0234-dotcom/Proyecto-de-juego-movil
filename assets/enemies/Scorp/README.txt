SCORPIUS-C1 / LibGDX sprite pack

- scorpius_c1_sheet.png: 1536 x 768 pixels, transparent, each frame 256 x 192.
- frames/: individual 256 x 192 transparent PNGs.
- scorpius_c1.atlas: TextureAtlas with named regions walk, fire, pincer, damage_death.
- scorpius_c1.json: frame data.

LibGDX loading:
TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("scorpius_c1.atlas"));
Animation<TextureRegion> walk = new Animation<>(0.10f, atlas.findRegions("walk"), Animation.PlayMode.LOOP);
Animation<TextureRegion> fire = new Animation<>(0.08f, atlas.findRegions("fire"), Animation.PlayMode.NORMAL);
Animation<TextureRegion> pincer = new Animation<>(0.08f, atlas.findRegions("pincer"), Animation.PlayMode.NORMAL);
Animation<TextureRegion> death = new Animation<>(0.12f, atlas.findRegions("damage_death"), Animation.PlayMode.NORMAL);

NOTE: Original JPG contains checkerboard pattern baked into the image, not true transparency. It is automatically removed approximately; inspect frames for artifacts.
