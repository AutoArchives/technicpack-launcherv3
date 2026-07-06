package net.technicpack.launchercore.modpacks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.technicpack.launcher.io.InstalledPackStore;
import net.technicpack.launcher.io.LauncherFileSystem;
import net.technicpack.launchercore.exception.BuildInaccessibleException;
import net.technicpack.platform.io.FeedItem;
import net.technicpack.rest.io.Modpack;
import net.technicpack.rest.io.PackInfo;
import net.technicpack.rest.io.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModpackModelSendClientIdTest {

  private static final String PACK_NAME = "testpack";

  @TempDir Path tempDir;

  private LauncherFileSystem fileSystem;
  private InstalledPackStore packStore;

  @BeforeEach
  void setUp() {
    fileSystem = new LauncherFileSystem(tempDir.resolve("launcher-root"));
    packStore = new InstalledPackStore(tempDir.resolve("installedpacks.json"));
  }

  @Test
  void sendClientIdIsFalseWithoutInstalledPack() {
    ModpackModel model = new ModpackModel(null, null, packStore, fileSystem);
    assertFalse(model.isSendClientId());
  }

  @Test
  void setSendClientIdPersistsToPackStore() {
    InstalledPack pack = new InstalledPack(PACK_NAME, InstalledPack.RECOMMENDED);
    packStore.put(pack);
    ModpackModel model = new ModpackModel(pack, null, packStore, fileSystem);

    model.setSendClientId(true);

    assertTrue(model.isSendClientId());
    assertTrue(packStore.getInstalledPacks().get(PACK_NAME).isSendClientId());
  }

  @Test
  void disablingSendClientIdWithoutInstalledPackIsANoOp() {
    ModpackModel model = new ModpackModel(null, null, packStore, fileSystem);

    model.setSendClientId(false);

    assertFalse(model.isSendClientId());
    assertTrue(packStore.getInstalledPacks().isEmpty(), "no InstalledPack entry may be created");
  }

  @Test
  void enablingSendClientIdWithoutInstalledPackBookmarksThePack() {
    ModpackModel model =
        new ModpackModel(null, new NamedPackInfo(PACK_NAME), packStore, fileSystem);

    model.setSendClientId(true);

    assertTrue(model.isSendClientId());
    InstalledPack created = packStore.getInstalledPacks().get(PACK_NAME);
    assertNotNull(created, "enabling must lazily create the InstalledPack entry");
    assertTrue(created.isSendClientId());
  }

  /** Minimal PackInfo whose only meaningful data is its name, used to drive the bookmark path. */
  private static final class NamedPackInfo implements PackInfo {
    private final String name;

    private NamedPackInfo(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getDisplayName() {
      return name;
    }

    @Override
    public String getWebSite() {
      return null;
    }

    @Override
    public Resource getIcon() {
      return null;
    }

    @Override
    public Resource getBackground() {
      return null;
    }

    @Override
    public Resource getLogo() {
      return null;
    }

    @Override
    public String getRecommended() {
      return null;
    }

    @Override
    public String getLatest() {
      return null;
    }

    @Override
    public List<String> getBuilds() {
      return Collections.emptyList();
    }

    @Override
    public ArrayList<FeedItem> getFeed() {
      return new ArrayList<>();
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public Integer getRuns() {
      return null;
    }

    @Override
    public Integer getInstalls() {
      return null;
    }

    @Override
    public Integer getLikes() {
      return null;
    }

    @Override
    public Modpack getModpack(String build) throws BuildInaccessibleException {
      return null;
    }

    @Override
    public boolean isComplete() {
      return true;
    }

    @Override
    public boolean isLocal() {
      return false;
    }

    @Override
    public boolean isServerPack() {
      return false;
    }

    @Override
    public boolean isOfficial() {
      return false;
    }

    @Override
    public boolean hasSolder() {
      return false;
    }

    @Override
    public String getDiscordId() {
      return null;
    }
  }
}
