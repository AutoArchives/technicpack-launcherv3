package net.technicpack.launchercore.modpacks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.technicpack.utilslib.Utils;
import org.junit.jupiter.api.Test;

class InstalledPackTest {

  @Test
  void sendClientIdDefaultsToFalse() {
    InstalledPack pack = new InstalledPack("testpack", InstalledPack.RECOMMENDED);
    assertFalse(pack.isSendClientId());
  }

  @Test
  void sendClientIdDefaultsToFalseWhenDeserializingLegacyJson() {
    String legacyJson =
        "{\"name\":\"testpack\",\"build\":\"recommended\",\"directory\":\"%MODPACKS%\\\\testpack\"}";
    InstalledPack pack = Utils.getGson().fromJson(legacyJson, InstalledPack.class);
    assertFalse(pack.isSendClientId());
  }

  @Test
  void sendClientIdSurvivesJsonRoundTrip() {
    InstalledPack pack = new InstalledPack("testpack", InstalledPack.RECOMMENDED);
    pack.setSendClientId(true);

    String json = Utils.getGson().toJson(pack);
    InstalledPack restored = Utils.getGson().fromJson(json, InstalledPack.class);

    assertTrue(restored.isSendClientId());
  }
}
