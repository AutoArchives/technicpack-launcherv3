package net.technicpack.solder.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import net.technicpack.launcher.io.LauncherFileSystem;
import net.technicpack.rest.RestfulAPIException;
import net.technicpack.rest.io.Modpack;
import net.technicpack.solder.ISolderPackApi;
import net.technicpack.solder.io.SolderPackInfo;
import net.technicpack.utilslib.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CachedSolderPackApiTest {

  private static final int CACHE_SECONDS = 3600;

  @TempDir Path tempDir;

  private static class CountingSolderPackApi implements ISolderPackApi {
    int packInfoPulls = 0;
    int buildPulls = 0;

    @Override
    public String getMirrorUrl() {
      return "https://mirror.example.com/";
    }

    @Override
    public SolderPackInfo getPackInfoForBulk() {
      packInfoPulls++;
      return Utils.getGson().fromJson("{\"name\":\"test-pack\"}", SolderPackInfo.class);
    }

    @Override
    public SolderPackInfo getPackInfo() {
      return getPackInfoForBulk();
    }

    @Override
    public Modpack getPackBuild(String build) {
      buildPulls++;
      return Utils.getGson().fromJson("{\"name\":\"test-pack\"}", Modpack.class);
    }
  }

  @Test
  void invalidateCacheForcesNextPackInfoPull() throws RestfulAPIException {
    CountingSolderPackApi inner = new CountingSolderPackApi();
    CachedSolderPackApi api =
        new CachedSolderPackApi(
            new LauncherFileSystem(tempDir.resolve("launcher-root")),
            inner,
            CACHE_SECONDS,
            "test-pack");

    api.getPackInfo();
    api.getPackInfo();
    assertEquals(1, inner.packInfoPulls, "second call within TTL must be served from cache");

    api.invalidateCache();

    api.getPackInfo();
    assertEquals(2, inner.packInfoPulls, "call after invalidateCache must hit the inner API");
  }

  @Test
  void invalidateCacheForcesNextBuildPull() throws Exception {
    CountingSolderPackApi inner = new CountingSolderPackApi();
    CachedSolderPackApi api =
        new CachedSolderPackApi(
            new LauncherFileSystem(tempDir.resolve("launcher-root")),
            inner,
            CACHE_SECONDS,
            "test-pack");

    api.getPackBuild("1.0.1");
    api.getPackBuild("1.0.1");
    assertEquals(1, inner.buildPulls, "second build fetch within TTL must be served from cache");

    api.invalidateCache();

    api.getPackBuild("1.0.1");
    assertEquals(2, inner.buildPulls, "build fetch after invalidateCache must hit the inner API");
  }
}
