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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class PacketHelper {

    public static final Identifier OPEN_CONFIGURE_SCREEN = new Identifier(WirelessNetworks.MOD_ID, "open_configure_screen");

    public static final Identifier UPDATE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "update_network");

    public static final Identifier SELECT_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "select_network");

    public static final Identifier DELETE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "delete_network");

    public static final Identifier WARNING_PACKET = new Identifier(WirelessNetworks.MOD_ID, "warning_packet");

    public static final Identifier MODE_PACKET = new Identifier(WirelessNetworks.MOD_ID, "mode_packet");

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
                        sendWarning("warning.wirelessnetworks.network.doesnt.exist", player);
                        return;
                    }
                    network = optional.get();
                }
                if (network == null || network.canModify(player))
                    player.openHandledScreen(new NetworkConfigureScreenFactory(blockPos, network, player));
                else
                    sendWarning("warning.wirelessnetworks.network.modify.dont.own", player);

            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_NETWORK, (server, player, networkHandler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean isCreating = buf.readBoolean();
            String networkId = buf.readString(32767);
            long capacity = buf.readLong();
            long maxInput = buf.readLong();
            long maxOutput = buf.readLong();
            boolean isProtected = buf.readBoolean();
            server.execute(() -> {
                NetworkState state = NetworkState.getOrCreate(server);
                if (networkId.isEmpty()) {
                    sendWarning("warning.wirelessnetworks.network.id.empty", player);
                    return;
                }
                Network network;
                Optional<Network> optional = state.getNetworkHandler(networkId);
                if (isCreating) {
                    if (optional.isPresent()) {
                        sendWarning("warning.wirelessnetworks.network.doesnt.exist", player);
                        return;
                    }
                    if (Utils.getDisplayId(networkId).length() > 10) {
                        sendWarning("warning.wirelessnetworks.network.id.characters", player);
                        return;
                    }
                    if (Utils.getDisplayId(networkId).trim().isEmpty()) {
                        sendWarning("warning.wirelessnetworks.network.id.empty", player);
                        return;
                    }
                    network = state.getOrCreateNetworkHandler(networkId, player.getUuid());
                } else {
                    if (!optional.isPresent()) {
                        sendWarning("warning.wirelessnetworks.network.doesnt.exist", player);
                        return;
                    }
                    network = optional.get();
                }
                if (!network.canModify(player)) {
                    sendWarning("warning.wirelessnetworks.network.modify.dont.own", player);
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
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
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
                    sendWarning("warning.wirelessnetworks.network.doesnt.exist", player);
                    return;
                } else if (!optional.get().canInteract(player)) {
                    sendWarning("warning.wirelessnetworks.network.misc", player);
                    return;
                }
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    ((NetworkNodeBlockEntity) blockEntity).setNetworkId(networkId);
                    blockEntity.markDirty();
                    ((NetworkNodeBlockEntity) blockEntity).sync();
                    blockEntity.getWorld().updateNeighbors(pos, blockEntity.getCachedState().getBlock());
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
                    sendWarning("warning.wirelessnetworks.network.doesnt.exist", player);
                    return;
                } else if (!optional.get().canModify(player)) {
                    sendWarning("warning.wirelessnetworks.network.modify.dont.own", player);
                    return;
                }
                state.delete(networkId);
                state.markDirty();
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    player.openHandledScreen((NetworkNodeBlockEntity) blockEntity);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MODE_PACKET, (server, player, networkHandler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean input = buf.readBoolean();

            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity node) {
                    node.setMode(input);
                    node.markDirty();
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
                    ((NetworkNodeScreen) currentScreenHandler).warning.text = Text.translatable(warning);
                    ((NetworkNodeScreen) currentScreenHandler).warning.ticksRemaining = 400;
                } else if (currentScreenHandler instanceof NetworkConfigureScreen) {
                    ((NetworkConfigureScreen) currentScreenHandler).warning.text = Text.translatable(warning);
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
