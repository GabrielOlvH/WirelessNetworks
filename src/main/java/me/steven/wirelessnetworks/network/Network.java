package me.steven.wirelessnetworks.network;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

public class Network implements EnergyIo {

    private final String id;
    private double energy;
    private double energyCapacity = 10000000;
    private double maxInput = Double.MAX_VALUE;
    private double maxOutput = Double.MAX_VALUE;

    private Network(String id, double energy, double energyCapacity, double maxInput, double maxOutput) {
        this.id = id;
        this.energy = energy;
        this.energyCapacity = energyCapacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }

    public Network(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getEnergyCapacity() {
        return energyCapacity;
    }

    public void setEnergyCapacity(double energyCapacity) {
        this.energyCapacity = energyCapacity;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public double insert(double amount, Simulation simulation) {
        double inserted = Math.min(Math.min(amount, maxInput), energyCapacity - energy);
        if (simulation.isActing()) this.energy += amount;
        return amount - inserted;
    }

    public double getMaxInput() {
        return maxInput;
    }

    public void setMaxInput(double maxInput) {
        this.maxInput = maxInput;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        double extracted = Math.min(Math.min(maxAmount, maxOutput), energy);
        if (simulation.isActing()) this.energy -= extracted;
        return extracted;
    }

    public double getMaxOutput() {
        return maxOutput;
    }

    public void setMaxOutput(double maxOutput) {
        this.maxOutput = maxOutput;
    }

    public void writeScreenData(PacketByteBuf buf) {
        buf.writeDouble(energyCapacity);
        buf.writeDouble(maxInput);
        buf.writeDouble(maxOutput);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("energy", energy);
        tag.putDouble("energyCapacity", energyCapacity);
        tag.putString("id", id);
        tag.putDouble("maxInput", maxInput);
        tag.putDouble("maxOutput", maxOutput);
        return tag;
    }

    public static Network fromTag(CompoundTag tag) {
        double energy = tag.getDouble("energy");
        double energyCapacity = tag.getDouble("energyCapacity");
        String id = tag.getString("id");
        double maxInput = tag.getDouble("maxInput");
        double maxOutput = tag.getDouble("maxOutput");
        return new Network(id, energy, energyCapacity, maxInput, maxOutput);
    }
}
