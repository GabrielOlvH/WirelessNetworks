package me.steven.wirelessnetworks.network;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import team.reborn.energy.api.EnergyStorage;

import java.util.UUID;

public class Network implements EnergyStorage {

    public static final long DEFAULT_MAX_ENERGY = 1_000_000;

    private String id;
    private boolean isProtected;
    private final UUID owner;
    private long energy;
    private long energyCapacity = DEFAULT_MAX_ENERGY;
    private long maxInput = DEFAULT_MAX_ENERGY;
    private long maxOutput = DEFAULT_MAX_ENERGY;

    private Network(String id, boolean isProtected, UUID owner, long energy, long energyCapacity, long maxInput, long maxOutput) {
        this.id = id;
        this.isProtected = isProtected;
        this.owner = owner;
        this.energy = energy;
        this.energyCapacity = energyCapacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }

    public Network(String id, UUID owner) {
        this.id = id;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    @Override
    public long getAmount() { return Math.min(energy, energyCapacity); }

    @Override
    public long getCapacity() { return energyCapacity; }

    public void setEnergyCapacity(long energyCapacity) {
        this.energyCapacity = energyCapacity;
        this.energy = Math.min(energy, energyCapacity);
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long inserted = Math.min(Math.min(maxAmount, maxInput), energyCapacity - getAmount());
        this.energy += maxAmount;
        return maxAmount - inserted;
    }

    public long getMaxInput() {
        return maxInput;
    }

    public void setMaxInput(long maxInput) {
        this.maxInput = Math.min(maxInput, energyCapacity);
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long extracted = Math.min(Math.min(maxAmount, maxOutput), getAmount());
        this.energy -= extracted;
        return extracted;
    }

    public long getMaxOutput() {
        return maxOutput;
    }

    public void setMaxOutput(long maxOutput) {
        this.maxOutput = Math.min(maxOutput, energyCapacity);
    }

    // id is modified so multiple players can have the same name as their private network eg "Base" or whatever
    // this will disconnect any previously connected nodes too
    public void markProtected(NetworkState state) {
        if (!isProtected) {
            this.isProtected = true;
            state.delete(id);
            this.id = owner.toString() + ":" + id;
            state.put(id, this);
        }
    }

    public void markPublic(NetworkState state) {
        if (isProtected) {
            this.isProtected = false;
            state.delete(id);
            this.id = id.substring(owner.toString().length() + 1);
            state.put(id, this);
        }
    }

    public boolean isProtected() {
        return isProtected;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean canInteract(PlayerEntity player) {
        return player.getAbilities().creativeMode || !isProtected || owner.equals(player.getUuid());
    }

    public boolean canModify(PlayerEntity player) {
        return player.getAbilities().creativeMode || owner.equals(player.getUuid());
    }

    public void writeScreenData(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeLong(energyCapacity);
        buf.writeLong(maxInput);
        buf.writeLong(maxOutput);
        buf.writeBoolean(isProtected);
        buf.writeUuid(owner);
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("energy", energy);
        tag.putLong("energyCapacity", energyCapacity);
        tag.putString("id", id);
        tag.putUuid("owner", owner);
        tag.putBoolean("protected", isProtected);
        tag.putLong("maxInput", maxInput);
        tag.putLong("maxOutput", maxOutput);
        return tag;
    }

    public static Network fromTag(NbtCompound tag) {
        long energy = tag.getLong("energy");
        long energyCapacity = tag.getLong("energyCapacity");
        String id = tag.getString("id");
        long maxInput = tag.getLong("maxInput");
        long maxOutput = tag.getLong("maxOutput");
        boolean isProtected = tag.getBoolean("protected");
        UUID playerUuid = tag.getUuid("owner");
        return new Network(id, isProtected, playerUuid, energy, energyCapacity, maxInput, maxOutput);
    }
}
