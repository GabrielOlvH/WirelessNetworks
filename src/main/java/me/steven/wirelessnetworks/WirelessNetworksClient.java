package me.steven.wirelessnetworks;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreen;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class WirelessNetworksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(WirelessNetworks.NODE_SCREEN_TYPE, (ScreenRegistry.Factory<NetworkNodeScreen, CottonInventoryScreen<NetworkNodeScreen>>) (handler, inventory, title) -> new CottonInventoryScreen<>(handler, inventory.player));
        ScreenRegistry.register(WirelessNetworks.CONFIGURE_SCREEN_TYPE, (ScreenRegistry.Factory<NetworkConfigureScreen, CottonInventoryScreen<NetworkConfigureScreen>>) (handler, inventory, title) -> new CottonInventoryScreen<>(handler, inventory.player));

        PacketHelper.registerClient();
    }
}
