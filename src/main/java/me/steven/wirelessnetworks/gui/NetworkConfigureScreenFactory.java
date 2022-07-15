package me.steven.wirelessnetworks.gui;

import me.steven.wirelessnetworks.network.Network;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class NetworkConfigureScreenFactory implements ExtendedScreenHandlerFactory {

    private final BlockPos blockPos;
    @Nullable
    private final Network network;
    private final ServerPlayerEntity player;

    public NetworkConfigureScreenFactory(BlockPos pos, @Nullable Network network, ServerPlayerEntity player) {
        this.blockPos = pos;
        this.network = network;
        this.player = player;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(network == null);
        if (network != null)
            network.writeScreenData(buf);
        else {
            buf.writeLong(Network.DEFAULT_MAX_ENERGY);
            buf.writeLong(Network.DEFAULT_MAX_ENERGY);
            buf.writeLong(Network.DEFAULT_MAX_ENERGY);
            buf.writeBoolean(true);
            buf.writeUuid(player.getUuid());
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Configure Node");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
        if (network != null) {
            return new NetworkConfigureScreen(blockPos, network.getId(), network.getOwner(), network.isProtected(), network.getAmount(), network.getMaxInput(), network.getMaxOutput(), syncId, inv);
        } else
            return openCreateScreen(syncId, inv);
    }

    private NetworkConfigureScreen openCreateScreen(int syncId, PlayerInventory inv) {
        return new NetworkConfigureScreen(blockPos, null, player.getUuid(), true, Network.DEFAULT_MAX_ENERGY, Network.DEFAULT_MAX_ENERGY, Network.DEFAULT_MAX_ENERGY, syncId, inv);
    }
}
