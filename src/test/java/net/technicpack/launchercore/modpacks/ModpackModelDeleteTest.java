package net.technicpack.launchercore.modpacks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.technicpack.launcher.io.InstalledPackStore;
import net.technicpack.launcher.io.LauncherFileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModpackModelDeleteTest {

  private static final String PACK_NAME = "testpack";
  private static final byte[] WORLD_DATA = {4, 2};

  @TempDir Path tempDir;

  private LauncherFileSystem fileSystem;
  private InstalledPackStore packStore;
  private Path packDir;
  private Path assetsDir;

  @BeforeEach
  void setUp() {
    fileSystem = new LauncherFileSystem(tempDir.resolve("launcher-root"));
    packStore = new InstalledPackStore(tempDir.resolve("installedpacks.json"));
    packDir = tempDir.resolve("modpacks").resolve(PACK_NAME);
    assetsDir = fileSystem.getPackAssetsDirectory().resolve(PACK_NAME);
  }

  private ModpackModel installedModel() throws IOException {
    Files.createDirectories(packDir.resolve("bin"));
    Files.createDirectories(packDir.resolve("mods"));
    Files.createDirectories(packDir.resolve("config"));
    Files.write(
        packDir.resolve("config").resolve("some.cfg"),
        "key=value".getBytes(StandardCharsets.UTF_8));
    Files.write(packDir.resolve("options.txt"), "fov:0.5".getBytes(StandardCharsets.UTF_8));

    Files.createDirectories(assetsDir);
    Files.write(assetsDir.resolve("background.png"), new byte[] {1, 2, 3});

    InstalledPack pack =
        new InstalledPack(PACK_NAME, InstalledPack.RECOMMENDED, packDir.toString());
    packStore.put(pack);

    return new ModpackModel(pack, null, packStore, fileSystem);
  }

  private void addWorldSave() throws IOException {
    Path worldDir = packDir.resolve("saves").resolve("MyWorld");
    Files.createDirectories(worldDir);
    Files.write(worldDir.resolve("level.dat"), WORLD_DATA);
  }

  // -------------------------------------------------------------------------
  // hasWorldSaves
  // -------------------------------------------------------------------------

  @Test
  void hasWorldSavesFalseWithoutInstalledDirectory() {
    InstalledPack pack = new InstalledPack(PACK_NAME, InstalledPack.RECOMMENDED);
    ModpackModel model = new ModpackModel(pack, null, packStore, fileSystem);

    assertFalse(model.hasWorldSaves());
  }

  @Test
  void hasWorldSavesFalseWhenSavesDirMissing() throws IOException {
    ModpackModel model = installedModel();

    assertFalse(model.hasWorldSaves());
  }

  @Test
  void hasWorldSavesFalseWhenSavesDirEmpty() throws IOException {
    ModpackModel model = installedModel();
    Files.createDirectories(packDir.resolve("saves"));

    assertFalse(model.hasWorldSaves());
  }

  @Test
  void hasWorldSavesTrueWhenSavesDirHasContent() throws IOException {
    ModpackModel model = installedModel();
    addWorldSave();

    assertTrue(model.hasWorldSaves());
  }

  // -------------------------------------------------------------------------
  // delete
  // -------------------------------------------------------------------------

  @Test
  void deleteRemovesEverythingIncludingSaves() throws IOException {
    ModpackModel model = installedModel();
    addWorldSave();

    model.delete();

    assertFalse(Files.exists(packDir));
    assertFalse(Files.exists(assetsDir));
    assertFalse(packStore.getInstalledPacks().containsKey(PACK_NAME));
  }

  @Test
  void deleteKeepingSavesKeepsOnlySaves() throws IOException {
    ModpackModel model = installedModel();
    addWorldSave();

    model.delete(true);

    Path levelDat = packDir.resolve("saves").resolve("MyWorld").resolve("level.dat");
    assertTrue(Files.exists(levelDat));
    assertArrayEquals(WORLD_DATA, Files.readAllBytes(levelDat));

    try (Stream<Path> children = Files.list(packDir)) {
      List<Path> remaining = children.collect(Collectors.toList());
      assertEquals(Collections.singletonList(packDir.resolve("saves")), remaining);
    }

    assertFalse(Files.exists(assetsDir));
    assertFalse(packStore.getInstalledPacks().containsKey(PACK_NAME));
  }

  @Test
  void deleteKeepingSavesWithoutSavesDirRemovesEverything() throws IOException {
    ModpackModel model = installedModel();

    model.delete(true);

    assertFalse(Files.exists(packDir));
    assertFalse(Files.exists(assetsDir));
    assertFalse(packStore.getInstalledPacks().containsKey(PACK_NAME));
  }
}
