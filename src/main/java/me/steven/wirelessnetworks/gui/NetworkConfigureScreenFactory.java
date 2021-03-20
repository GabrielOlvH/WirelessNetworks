package me.steven.wirelessnetworks.gui;

import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.network.Network;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
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
            buf.writeDouble(Network.DEFAULT_MAX_ENERGY);
            buf.writeDouble(Network.DEFAULT_MAX_ENERGY);
            buf.writeDouble(Network.DEFAULT_MAX_ENERGY);
            buf.writeBoolean(true);
            buf.writeUuid(player.getUuid());
        }
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText("Configure Node");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
        PacketByteBuf buf = PacketByteBufs.create();
        writeScreenOpeningData(player, buf);
        return WirelessNetworks.CONFIGURE_SCREEN_TYPE.create(syncId, inv, buf);
    }
}
