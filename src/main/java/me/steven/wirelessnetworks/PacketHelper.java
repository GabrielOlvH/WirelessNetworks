package me.steven.wirelessnetworks;

import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreen;
import me.steven.wirelessnetworks.network.Network;
import me.steven.wirelessnetworks.network.NetworkState;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class PacketHelper {

    public static final Identifier OPEN_CONFIGURE_SCREEN = new Identifier(WirelessNetworks.MOD_ID, "open_configure_screen");

    public static final Identifier UPDATE_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "update_network");

    public static final Identifier SELECT_NETWORK = new Identifier(WirelessNetworks.MOD_ID, "select_network");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_CONFIGURE_SCREEN, (server, player, networkHandler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            boolean isNewNetwork = buf.readBoolean();
            String networkId = isNewNetwork ? null : buf.readString();
            server.execute(() -> {
                Network network = isNewNetwork ? null : NetworkState.getOrCreate(((ServerWorld) player.world)).getNetworkHandler(networkId).get();
                player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                        buf.writeBlockPos(blockPos);
                        buf.writeBoolean(isNewNetwork);
                        if (network != null)
                            network.writeScreenData(buf);
                        else {
                            buf.writeDouble(1000000);
                        }
                    }

                    @Override
                    public Text getDisplayName() {
                        return new LiteralText("get fucked idiot");
                    }

                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeBlockPos(blockPos);
                        buf.writeBoolean(isNewNetwork);
                        if (network != null)
                            network.writeScreenData(buf);
                        else {
                            buf.writeDouble(1000000);
                        }
                        return WirelessNetworks.CONFIGURE_SCREEN_TYPE.create(syncId, inv, buf);
                    }
                });
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_NETWORK, (server, player, networkHandler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            String networkId = buf.readString();
            double capacity = buf.readDouble();
            server.execute(() -> {
                NetworkState state = NetworkState.getOrCreate((ServerWorld) player.world);
                Network network = state.getOrCreateNetworkHandler(networkId);
                network.setEnergyCapacity(capacity);
                state.markDirty();
                BlockEntity blockEntity = player.world.getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    player.openHandledScreen((NetworkNodeBlockEntity) blockEntity);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SELECT_NETWORK, (server, player, networkHandler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            String networkId = buf.readString();
            server.execute(() -> {
                BlockEntity blockEntity = player.world.getBlockEntity(pos);
                if (blockEntity instanceof NetworkNodeBlockEntity) {
                    ((NetworkNodeBlockEntity) blockEntity).setNetworkId(networkId);
                    blockEntity.markDirty();
                    ((NetworkNodeBlockEntity) blockEntity).sync();
                }
            });
        });
    }
}
