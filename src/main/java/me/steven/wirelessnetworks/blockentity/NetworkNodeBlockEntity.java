package me.steven.wirelessnetworks.blockentity;

import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import me.steven.wirelessnetworks.network.Network;
import me.steven.wirelessnetworks.network.NetworkState;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NetworkNodeBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Nameable, ExtendedScreenHandlerFactory, BlockEntityClientSerializable {

    private String networkId = null;

    public NetworkNodeBlockEntity(BlockPos pos, BlockState state) {
        super(WirelessNetworks.NODE_BLOCK_ENTITY_TYPE, pos, state);
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Optional<Network> getNetwork() {
        if (world == null) return Optional.empty();
        return NetworkState.getOrCreate(((ServerWorld) world).getServer()).getNetworkHandler(networkId);
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

        List<String> keys = NetworkState.getOrCreate(((ServerWorld) world).getServer())
                .getNetworks()
                .entrySet()
                .stream()
                .filter((entry) -> entry.getValue().canInteract(player))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return new NetworkNodeScreen(pos, keys, syncId, inv);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        Set<String> keys = NetworkState.getOrCreate(((ServerWorld) world).getServer())
                .getNetworks()
                .entrySet()
                .stream()
                .filter((entry) -> entry.getValue().canInteract(player))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        buf.writeInt(keys.size());
        keys.forEach(buf::writeString);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        if (networkId != null)
            tag.putString("NetworkID", networkId);
        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("NetworkID"))
            networkId = tag.getString("NetworkID");
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        if (tag.contains("NetworkID"))
            networkId = tag.getString("NetworkID");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        if (networkId != null)
            tag.putString("NetworkID", networkId);
        return tag;
    }
}