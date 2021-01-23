package me.steven.wirelessnetworks.blockentity;

import io.netty.buffer.Unpooled;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import me.steven.wirelessnetworks.network.Network;
import me.steven.wirelessnetworks.network.NetworkState;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class NetworkNodeBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Nameable, ExtendedScreenHandlerFactory, BlockEntityClientSerializable {

    private String networkId = null;

    public NetworkNodeBlockEntity() {
        super(WirelessNetworks.NODE_BLOCK_ENTITY_TYPE);
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Optional<Network> getNetwork() {
        if (world == null) return Optional.empty();
        return NetworkState.getOrCreate((ServerWorld) world).getNetworkHandler(networkId);
    }

    @Override
    public Text getName() {
        return new LiteralText("Network Node");
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText("Network Node");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        Set<String> keys = NetworkState.getOrCreate((ServerWorld) world).getNetworks().keySet();
        buf.writeInt(keys.size());
        keys.forEach(buf::writeString);
        return WirelessNetworks.NODE_SCREEN_TYPE.create(syncId, inv, buf);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        Set<String> keys = NetworkState.getOrCreate((ServerWorld) world).getNetworks().keySet();
        buf.writeInt(keys.size());
        keys.forEach(buf::writeString);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (networkId != null)
            tag.putString("NetworkID", networkId);
        return super.toTag(tag);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        if (tag.contains("NetworkID"))
            networkId = tag.getString("NetworkID");
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        if (tag.contains("NetworkID"))
            networkId = tag.getString("NetworkID");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        if (networkId != null)
            tag.putString("NetworkID", networkId);
        return tag;
    }
}