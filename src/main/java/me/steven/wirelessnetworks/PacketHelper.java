package me.steven.wirelessnetworks;

import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreenFactory;
import me.steven.wirelessnetworks.network.Network;
import me.steven.wirelessnetworks.network.NetworkState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class PacketHelper {

    public static final Identifier OPEN_CONFIGURE_SCREEN = new Identifier(WirelessNetworks.MOD_ID, "open_configure_screen");

    public static final Identifier UPDATE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "update_network");

    public static final Identifier SELECT_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "select_network");

    public static final Identifier DELETE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "delete_network");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_CONFIGURE_SCREEN, (server, player, networkHandler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            boolean isNewNetwork = buf.readBoolean();
            String networkId = isNewNetwork ? null : buf.readString(32767);
            server.execute(() -> {
                Network network;
                if (isNewNetwork) network = null;
                else {
                    Optional<Network> optional = NetworkState.getOrCreate(server).getNetworkHandler(networkId);
                    if (!optional.isPresent()) {
                        //TODO implement warning into GUIs.
                        player.sendMessage(new LiteralText("This network no longer exists."), false);
                        return;
                    }
                    network = optional.get();
                }
                player.openHandledScreen(new NetworkConfigureScreenFactory(blockPos, network, player));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_NETWORK, (server, player, networkHandler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean isCreating = buf.readBoolean();
            String networkId = buf.readString(32767);
            double capacity = buf.readDouble();
            double maxInput = buf.readDouble();
            double maxOutput = buf.readDouble();
            boolean isProtected = buf.readBoolean();
            server.execute(() -> {
                NetworkState state = NetworkState.getOrCreate(server);
                Network network;
                if (isCreating) {
                    network = state.getOrCreateNetworkHandler(networkId, player.getUuid());
                } else {
                    Optional<Network> optional = state.getNetworkHandler(networkId);
                    if (!optional.isPresent()) {
                        //TODO implement warning into GUIs.
                        player.sendMessage(new LiteralText("This network no longer exists."), false);
                        return;
                    }
                    network = optional.get();
                }
                network.setEnergyCapacity(capacity);
                network.setMaxInput(maxInput);
                network.setMaxOutput(maxOutput);
                if (isProtected && !network.isProtected())
                    network.markProtected(state);
                else if (!isProtected && network.isProtected())
                    network.markPublic(state);
                state.markDirty();
                BlockEntity blockEntity = player.world.getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    player.openHandledScreen((NetworkNodeBlockEntity) blockEntity);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SELECT_NETWORK, (server, player, networkHandler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            String networkId = buf.readString(32767);
            server.execute(() -> {
                NetworkState state = NetworkState.getOrCreate(server);
                Optional<Network> optional = state.getNetworkHandler(networkId);
                if (!optional.isPresent()) {
                    //TODO implement warning into GUIs.
                    player.sendMessage(new LiteralText("This network no longer exists."), false);
                    return;
                }
                BlockEntity blockEntity = player.world.getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    ((NetworkNodeBlockEntity) blockEntity).setNetworkId(networkId);
                    blockEntity.markDirty();
                    ((NetworkNodeBlockEntity) blockEntity).sync();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DELETE_NETWORK, (server, player, networkHandler, buf, sender) -> {
            String networkId = buf.readString(32767);
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> {
                NetworkState state = NetworkState.getOrCreate(server);
                state.delete(networkId);
                state.markDirty();
                BlockEntity blockEntity = player.world.getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    player.openHandledScreen((NetworkNodeBlockEntity) blockEntity);
                }
            });
        });
    }
}
