/*
 * This file is part of The Technic Launcher Version 3.
 * Copyright ©2015 Syndicate, LLC
 *
 * The Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Technic Launcher  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.technicpack.launcher.io;

import net.technicpack.launcher.settings.TechnicSettings;
import net.technicpack.launchercore.modpacks.InstalledPack;
import net.technicpack.solder.ISolderClientIdProvider;

/**
 * Sends the launcher client ID only for modpacks whose {@link InstalledPack#isSendClientId()}
 * opt-in flag is set. Packs that are merely being browsed have no InstalledPack entry and therefore
 * never send it.
 */
public class InstalledPackClientIdProvider implements ISolderClientIdProvider {
  private final InstalledPackStore packStore;
  private final TechnicSettings settings;

  public InstalledPackClientIdProvider(InstalledPackStore packStore, TechnicSettings settings) {
    this.packStore = packStore;
    this.settings = settings;
  }

  @Override
  public String getClientIdFor(String modpackSlug) {
    if (modpackSlug == null) {
      return null;
    }

    InstalledPack pack = packStore.getInstalledPacks().get(modpackSlug);

    if (pack != null && pack.isSendClientId()) {
      return settings.getClientId();
    }

    return null;
  }
}
