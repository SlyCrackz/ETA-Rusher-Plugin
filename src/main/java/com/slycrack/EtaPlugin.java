package com.slycrack;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * ETA RusherHack plugin
 */
public class EtaPlugin extends Plugin {

    @Override
    public void onLoad() {
        this.getLogger().info("ETA loaded!");

        final EtaCommand etaCommand = new EtaCommand();
        RusherHackAPI.getCommandManager().registerFeature(etaCommand);

        final EtaHud etaHud = new EtaHud(etaCommand);
        RusherHackAPI.getHudManager().registerFeature(etaHud);

    }

    @Override
    public void onUnload() {
        this.getLogger().info("ETA unloaded!");
    }
}
