package me.steven.wirelessnetworks.network;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

public class Network implements EnergyIo {

    private final String id;
    private double energy;
    private double energyCapacity = 10000000;

    private Network(String id, double energy, double energyCapacity) {
        this.id = id;
        this.energy = energy;
        this.energyCapacity = energyCapacity;
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
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        return 0;
    }

    public void writeScreenData(PacketByteBuf buf) {
        buf.writeDouble(energyCapacity);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("energy", energy);
        tag.putDouble("energyCapacity", energyCapacity);
        tag.putString("id", id);
        return tag;
    }

    public static Network fromTag(CompoundTag tag) {
        double energy = tag.getDouble("energy");
        double energyCapacity = tag.getDouble("energyCapacity");
        String id = tag.getString("id");
        return new Network(id, energy, energyCapacity);
    }
}
