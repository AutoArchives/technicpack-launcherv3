package net.technicpack.launcher.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import net.technicpack.launcher.settings.TechnicSettings;
import net.technicpack.launchercore.modpacks.InstalledPack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InstalledPackClientIdProviderTest {

  private static final String SLUG = "test-pack";

  @TempDir Path tempDir;

  private InstalledPackStore packStore;
  private TechnicSettings settings;
  private InstalledPackClientIdProvider provider;

  @BeforeEach
  void setUp() {
    packStore = new InstalledPackStore(tempDir.resolve("installedpacks.json"));
    settings = new TechnicSettings();
    provider = new InstalledPackClientIdProvider(packStore, settings);
  }

  @Test
  void returnsNullForUnknownPack() {
    assertNull(provider.getClientIdFor(SLUG));
  }

  @Test
  void returnsNullWhenPackHasNotOptedIn() {
    packStore.put(new InstalledPack(SLUG, InstalledPack.RECOMMENDED));
    assertNull(provider.getClientIdFor(SLUG));
  }

  @Test
  void returnsClientIdWhenPackOptedIn() {
    InstalledPack pack = new InstalledPack(SLUG, InstalledPack.RECOMMENDED);
    pack.setSendClientId(true);
    packStore.put(pack);
    assertEquals(settings.getClientId(), provider.getClientIdFor(SLUG));
  }

  @Test
  void returnsNullForNullSlug() {
    assertNull(provider.getClientIdFor(null));
  }
}
