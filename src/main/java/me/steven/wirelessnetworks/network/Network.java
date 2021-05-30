package me.steven.wirelessnetworks.network;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class Network implements EnergyIo {

    public static final double DEFAULT_MAX_ENERGY = 1_000_000;

    private String id;
    private boolean isProtected;
    private final UUID owner;
    private double energy;
    private double energyCapacity = DEFAULT_MAX_ENERGY;
    private double maxInput = DEFAULT_MAX_ENERGY;
    private double maxOutput = DEFAULT_MAX_ENERGY;

    private Network(String id, boolean isProtected, UUID owner, double energy, double energyCapacity, double maxInput, double maxOutput) {
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
    public double getEnergy() {
        return Math.min(energy, energyCapacity);
    }

    @Override
    public double getEnergyCapacity() {
        return energyCapacity;
    }

    public void setEnergyCapacity(double energyCapacity) {
        this.energyCapacity = energyCapacity;
        this.energy = Math.min(energy, energyCapacity);
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public double insert(double amount, Simulation simulation) {
        double inserted = Math.min(Math.min(amount, maxInput), energyCapacity - getEnergy());
        if (simulation.isActing()) this.energy += amount;
        return amount - inserted;
    }

    public double getMaxInput() {
        return maxInput;
    }

    public void setMaxInput(double maxInput) {
        this.maxInput = Math.min(maxInput, energyCapacity);
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        double extracted = Math.min(Math.min(maxAmount, maxOutput), getEnergy());
        if (simulation.isActing()) this.energy -= extracted;
        return extracted;
    }

    public double getMaxOutput() {
        return maxOutput;
    }

    public void setMaxOutput(double maxOutput) {
        this.maxOutput = Math.min(maxOutput, energyCapacity);
    }

    // id is modified so multiple players can have the same name as their private network eg "Base" or whatever
    // this will disconnected any previously connected nodes too
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
        buf.writeDouble(energyCapacity);
        buf.writeDouble(maxInput);
        buf.writeDouble(maxOutput);
        buf.writeBoolean(isProtected);
        buf.writeUuid(owner);
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putDouble("energy", energy);
        tag.putDouble("energyCapacity", energyCapacity);
        tag.putString("id", id);
        tag.putUuid("owner", owner);
        tag.putBoolean("protected", isProtected);
        tag.putDouble("maxInput", maxInput);
        tag.putDouble("maxOutput", maxOutput);
        return tag;
    }

    public static Network fromTag(NbtCompound tag) {
        double energy = tag.getDouble("energy");
        double energyCapacity = tag.getDouble("energyCapacity");
        String id = tag.getString("id");
        double maxInput = tag.getDouble("maxInput");
        double maxOutput = tag.getDouble("maxOutput");
        boolean isProtected = tag.getBoolean("protected");
        UUID playerUuid = tag.getUuid("owner");
        return new Network(id, isProtected, playerUuid, energy, energyCapacity, maxInput, maxOutput);
    }
}
