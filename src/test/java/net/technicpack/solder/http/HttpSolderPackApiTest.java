package net.technicpack.solder.http;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.technicpack.launchercore.exception.BuildInaccessibleException;
import net.technicpack.rest.RestfulAPIException;
import net.technicpack.solder.ISolderClientIdProvider;
import org.junit.jupiter.api.Test;

class HttpSolderPackApiTest {

  private static final String BASE_URL = "https://solder.example.com/api/";
  private static final String MIRROR_URL = "https://mirror.example.com/";
  private static final String SLUG = "test-pack";
  private static final String CLIENT_ID = "00000000-0000-0000-0000-000000000000";

  private static final ISolderClientIdProvider ALWAYS_SEND = slug -> CLIENT_ID;
  private static final ISolderClientIdProvider NEVER_SEND = slug -> null;

  @Test
  void constructorThrowsWhenModpackSlugIsNull() {
    RestfulAPIException ex =
        assertThrows(
            RestfulAPIException.class,
            () -> new HttpSolderPackApi(BASE_URL, null, ALWAYS_SEND, MIRROR_URL));
    assertTrue(
        ex.getMessage().contains("modpack slug"),
        "exception message should identify modpack slug, was: " + ex.getMessage());
  }

  @Test
  void constructorThrowsWhenBaseUrlIsNull() {
    RestfulAPIException ex =
        assertThrows(
            RestfulAPIException.class,
            () -> new HttpSolderPackApi(null, SLUG, ALWAYS_SEND, MIRROR_URL));
    assertTrue(
        ex.getMessage().contains("base URL"),
        "exception message should identify base URL, was: " + ex.getMessage());
  }

  @Test
  void constructorThrowsWhenMirrorUrlIsNull() {
    RestfulAPIException ex =
        assertThrows(
            RestfulAPIException.class,
            () -> new HttpSolderPackApi(BASE_URL, SLUG, ALWAYS_SEND, null));
    assertTrue(
        ex.getMessage().contains("mirror URL"),
        "exception message should identify mirror URL, was: " + ex.getMessage());
  }

  @Test
  void constructorThrowsWhenClientIdProviderIsNull() {
    RestfulAPIException ex =
        assertThrows(
            RestfulAPIException.class,
            () -> new HttpSolderPackApi(BASE_URL, SLUG, null, MIRROR_URL));
    assertTrue(
        ex.getMessage().contains("client ID provider"),
        "exception message should identify client ID provider, was: " + ex.getMessage());
  }

  @Test
  void constructorAcceptsAllNonNullArguments() {
    assertDoesNotThrow(() -> new HttpSolderPackApi(BASE_URL, SLUG, ALWAYS_SEND, MIRROR_URL));
  }

  @Test
  void packInfoUrlIncludesCidWhenProviderReturnsClientId() throws RestfulAPIException {
    HttpSolderPackApi api = new HttpSolderPackApi(BASE_URL, SLUG, ALWAYS_SEND, MIRROR_URL);
    assertEquals(
        "https://solder.example.com/api/modpack/test-pack?cid=" + CLIENT_ID,
        api.buildPackInfoUrl());
  }

  @Test
  void packInfoUrlOmitsCidWhenProviderReturnsNull() throws RestfulAPIException {
    HttpSolderPackApi api = new HttpSolderPackApi(BASE_URL, SLUG, NEVER_SEND, MIRROR_URL);
    String url = api.buildPackInfoUrl();
    assertEquals("https://solder.example.com/api/modpack/test-pack", url);
    assertFalse(url.contains("cid"), "URL must not contain a cid parameter, was: " + url);
  }

  @Test
  void packBuildUrlIncludesCidWhenProviderReturnsClientId() throws RestfulAPIException {
    HttpSolderPackApi api = new HttpSolderPackApi(BASE_URL, SLUG, ALWAYS_SEND, MIRROR_URL);
    assertEquals(
        "https://solder.example.com/api/modpack/test-pack/1.0.1?cid=" + CLIENT_ID,
        api.buildPackBuildUrl("1.0.1"));
  }

  @Test
  void packBuildUrlOmitsCidWhenProviderReturnsNull() throws RestfulAPIException {
    HttpSolderPackApi api = new HttpSolderPackApi(BASE_URL, SLUG, NEVER_SEND, MIRROR_URL);
    String url = api.buildPackBuildUrl("1.0.1");
    assertEquals("https://solder.example.com/api/modpack/test-pack/1.0.1", url);
    assertFalse(url.contains("cid"), "URL must not contain a cid parameter, was: " + url);
  }

  @Test
  void providerReceivesTheModpackSlug() throws RestfulAPIException {
    String[] seenSlug = new String[1];
    ISolderClientIdProvider capturing =
        slug -> {
          seenSlug[0] = slug;
          return null;
        };
    HttpSolderPackApi api = new HttpSolderPackApi(BASE_URL, SLUG, capturing, MIRROR_URL);
    api.buildPackInfoUrl();
    assertEquals(SLUG, seenSlug[0]);
  }

  @Test
  void getPackBuildThrowsBuildInaccessibleWhenBuildIsNull() throws RestfulAPIException {
    HttpSolderPackApi api = new HttpSolderPackApi(BASE_URL, SLUG, ALWAYS_SEND, MIRROR_URL);
    BuildInaccessibleException ex =
        assertThrows(BuildInaccessibleException.class, () -> api.getPackBuild(null));
    assertNotNull(ex.getCause());
    assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    assertEquals("build name must not be null", ex.getCause().getMessage());
    assertTrue(
        ex.getMessage().contains(SLUG),
        "exception message should name the modpack, was: " + ex.getMessage());
  }
}
