package me.steven.wirelessnetworks.network;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public class ExposedNetwork extends SnapshotParticipant<Long> implements EnergyStorage {
    private final Network network;
    private final boolean input;
    
    public ExposedNetwork(Network network, boolean input) {
        this.network = network;
        this.input = input;
    }

    public Network getNetwork() {
        return network;
    }

    @Override
    public long getAmount() { return network.getAmount(); }

    @Override
    public long getCapacity() { return network.getCapacity(); }

    @Override
    public boolean supportsInsertion() { return network.supportsInsertion(); }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if (!input) return 0;
        return network.insert(maxAmount, transaction);
    }

    @Override
    public boolean supportsExtraction() { return network.supportsExtraction(); }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if (input) return 0;
        return network.extract(maxAmount, transaction);
    }

    @Override
    protected Long createSnapshot() {
        return network.createSnapshot();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        network.readSnapshot(snapshot);
    }

}
