package me.steven.wirelessnetworks.network;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class NetworkState extends PersistentState {

    public static final String KEY = "wireless_networks";

    private final Map<String, Network> networks = new HashMap<>();

    public NetworkState() {
        super(KEY);
    }

    public Optional<Network> getNetworkHandler(String id) {
        if (!networks.containsKey(id)) return Optional.empty();
        return Optional.of(networks.get(id));
    }

    public Network getOrCreateNetworkHandler(String id, UUID uuid) {
        return networks.computeIfAbsent(id, (n) -> new Network(id, uuid));
    }

    public Network delete(String id) {
        return networks.remove(id);
    }

    public void put(String id, Network network) {
        networks.put(id, network);
    }

    public Map<String, Network> getNetworks() {
        return ImmutableMap.copyOf(networks);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ListTag list = tag.getList("Networks", 10);
        list.forEach(networkTag -> {
            Network network = Network.fromTag((CompoundTag) networkTag);
            networks.put(network.getId(), network);
        });
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag list = new ListTag();
        networks.forEach((id, network) -> list.add(network.toTag()));
        tag.put("Networks", list);
        return tag;
    }

    public static NetworkState getOrCreate(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(NetworkState::new, KEY);
    }
}
