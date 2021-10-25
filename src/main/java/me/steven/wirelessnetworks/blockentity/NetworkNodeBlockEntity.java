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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkNodeBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Nameable, ExtendedScreenHandlerFactory, BlockEntityClientSerializable {

    private String networkId = null;
    private boolean input = false;

    public NetworkNodeBlockEntity(BlockPos pos, BlockState state) {
        super(WirelessNetworks.NODE_BLOCK_ENTITY_TYPE, pos, state);
    }

    public EnumSet<Direction> validDirections = EnumSet.allOf(Direction.class);

    public static void tick(World world, BlockPos pos, NetworkNodeBlockEntity blockEntity) {
        if (blockEntity.isOutput()) {
            for (Direction dir : Direction.values()) {
                if (blockEntity.validDirections.contains(dir)) {
                    EnergyStorage sourceIo = EnergyStorage.SIDED.find(world, pos, dir);
                    EnergyStorage targetIo = EnergyStorage.SIDED.find(world, pos.offset(dir), dir.getOpposite());

                    if (targetIo == null || sourceIo == null) {
                        blockEntity.validDirections.remove(dir);
                    } else if (sourceIo.supportsExtraction() && targetIo.supportsInsertion()) {
                        EnergyStorageUtil.move(sourceIo, targetIo, Long.MAX_VALUE, null);
                    }
                }
            }
        }
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

    public void setMode(boolean input) {
        this.input = input;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isOutput() {
        return !input;
    }

    @Override
    public Text getName() { return new TranslatableText("block.wirelessnetworks.node_block"); }

    @Override
    public Text getDisplayName() { return new TranslatableText("block.wirelessnetworks.node_block"); }

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
        return new NetworkNodeScreen(pos, input, keys, syncId, inv);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(input);
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
        tag.putBoolean("Input", input);
        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("NetworkID"))
            networkId = tag.getString("NetworkID");
        input = tag.getBoolean("Input");
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        if (tag.contains("NetworkID"))
            networkId = tag.getString("NetworkID");
        input = tag.getBoolean("Input");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        if (networkId != null)
            tag.putString("NetworkID", networkId);
        tag.putBoolean("Input", input);
        return tag;
    }
}