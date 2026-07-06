package net.technicpack.solder.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class HttpSolderApiTest {

  @Test
  void publicPacksUrlNeverContainsClientId() {
    String url = HttpSolderApi.buildPublicPacksUrl("https://solder.example.com/api/");
    assertEquals("https://solder.example.com/api/modpack?include=full", url);
    assertFalse(url.contains("cid"), "pack list URL must not contain a cid parameter, was: " + url);
  }
}
