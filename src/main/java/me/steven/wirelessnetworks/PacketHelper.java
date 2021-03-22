package me.steven.wirelessnetworks;

import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreen;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreenFactory;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import me.steven.wirelessnetworks.network.Network;
import me.steven.wirelessnetworks.network.NetworkState;
import me.steven.wirelessnetworks.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class PacketHelper {

    public static final Identifier OPEN_CONFIGURE_SCREEN = new Identifier(WirelessNetworks.MOD_ID, "open_configure_screen");

    public static final Identifier UPDATE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "update_network");

    public static final Identifier SELECT_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "select_network");

    public static final Identifier DELETE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "delete_network");

    public static final Identifier WARNING_PACKET = new Identifier(WirelessNetworks.MOD_ID, "warning_packet");

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
                        sendWarning("This network no longer exists", player);
                        return;
                    }
                    network = optional.get();
                }
                if (network == null || network.canModify(player))
                    player.openHandledScreen(new NetworkConfigureScreenFactory(blockPos, network, player));
                else
                    sendWarning("You cannot modify networks you do not own.", player);

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
                if (networkId.isEmpty()) {
                    sendWarning("Network ID cannot be empty.", player);
                    return;
                }
                Network network;
                Optional<Network> optional = state.getNetworkHandler(networkId);
                if (isCreating) {
                    if (optional.isPresent()) {
                        sendWarning("A network with this ID already exists", player);
                        return;
                    }
                    if (Utils.getDisplayId(networkId).length() > 10) {
                        sendWarning("Network ID can only have 10 characters at most.", player);
                        return;
                    }
                    if (Utils.getDisplayId(networkId).trim().isEmpty()) {
                        sendWarning("Network ID cannot be empty.", player);
                        return;
                    }
                    network = state.getOrCreateNetworkHandler(networkId, player.getUuid());
                } else {
                    if (!optional.isPresent()) {
                        sendWarning("This network no longer exists", player);
                        return;
                    }
                    network = optional.get();
                }
                if (!network.canModify(player)) {
                    sendWarning("You cannot modify networks you do not own.", player);
                    return;
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
                    sendWarning("This network no longer exists", player);
                    return;
                } else if (!optional.get().canInteract(player)) {
                    sendWarning("You cannot use this network. This should not have happened", player);
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
                Optional<Network> optional = state.getNetworkHandler(networkId);
                if (!optional.isPresent()) {
                    sendWarning("This network no longer exists", player);
                    return;
                } else if (!optional.get().canModify(player)) {
                    sendWarning("You cannot modify networks you do not own.", player);
                    return;
                }
                state.delete(networkId);
                state.markDirty();
                BlockEntity blockEntity = player.world.getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    player.openHandledScreen((NetworkNodeBlockEntity) blockEntity);
                }
            });
        });
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(WARNING_PACKET, (client, networkHandler, buf, packetSender) -> {
            String warning = buf.readString();
            client.execute(() -> {
                ScreenHandler currentScreenHandler = client.player.currentScreenHandler;
                if (currentScreenHandler instanceof NetworkNodeScreen) {
                    ((NetworkNodeScreen) currentScreenHandler).warning.text = warning;
                    ((NetworkNodeScreen) currentScreenHandler).warning.ticksRemaining = 400;
                } else if (currentScreenHandler instanceof NetworkConfigureScreen) {
                    ((NetworkConfigureScreen) currentScreenHandler).warning.text = warning;
                    ((NetworkConfigureScreen) currentScreenHandler).warning.ticksRemaining = 400;
                }
            });
        });
    }

    private static void sendWarning(String warning, ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(warning);
        ServerPlayNetworking.send(player, WARNING_PACKET, buf);

    }
}
