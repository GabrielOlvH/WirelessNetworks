package me.steven.wirelessnetworks;

import me.steven.wirelessnetworks.block.NetworkBlock;
import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreen;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import me.steven.wirelessnetworks.network.ExposedNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WirelessNetworks implements ModInitializer {

	public static final String MOD_ID = "wirelessnetworks";

	public static final Block NODE_BLOCK = new NetworkBlock(FabricBlockSettings.create().strength(2f).nonOpaque().requiresTool());
	public static final BlockItem NODE_BLOCK_ITEM = new BlockItem(NODE_BLOCK, new Item.Settings());
	public static final BlockEntityType<NetworkNodeBlockEntity> NODE_BLOCK_ENTITY_TYPE
			= FabricBlockEntityTypeBuilder.create(NetworkNodeBlockEntity::new, NODE_BLOCK).build(null);
	public static final Item ENTANGLED_CAPACITOR_ITEM = new Item(new Item.Settings());

	public static final ScreenHandlerType<NetworkNodeScreen> NODE_SCREEN_TYPE = Registry.register(
		Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "node_screen"), new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> {
			BlockPos blockPos = buf.readBlockPos();
			boolean input = buf.readBoolean();
			int size = buf.readInt();
			List<String> keys = new ArrayList<>();
			for (int index = 0; index < size; index++) {
				keys.add(buf.readString(32767));
			}
			return new NetworkNodeScreen(blockPos, input, keys, syncId, inventory);
		})
	);

	public static final ScreenHandlerType<NetworkConfigureScreen> CONFIGURE_SCREEN_TYPE = Registry.register(
		Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "configure_node_screen"), new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> {
			BlockPos pos = buf.readBlockPos();
			boolean isNewNetwork = buf.readBoolean();
			String networkId = isNewNetwork ? null : buf.readString(32767);
			long energyCapacity = buf.readLong();
			long maxInput = buf.readLong();
			long maxOutput = buf.readLong();
			boolean isProtected = buf.readBoolean();
			UUID owner = buf.readUuid();
			return new NetworkConfigureScreen(pos, networkId, owner, isProtected, energyCapacity, maxInput, maxOutput, syncId, inventory);
		})
	);

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "node_block"), NODE_BLOCK);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "node_block"), NODE_BLOCK_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "node_block"), NODE_BLOCK_ENTITY_TYPE);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "entangled_capacitor"), ENTANGLED_CAPACITOR_ITEM);

		EnergyStorage.SIDED.registerForBlockEntities((blockEntity, direction) -> {
			if (
					blockEntity instanceof NetworkNodeBlockEntity node
							&& blockEntity.getWorld() != null
							&& !blockEntity.getWorld().isClient
							&& direction != Direction.UP
			) {
				return ((NetworkNodeBlockEntity) blockEntity).getNetwork().map((n) -> new ExposedNetwork(n, node.isInput())).orElse(null);
			}
			return null;
		}, NODE_BLOCK_ENTITY_TYPE);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(g -> {
			g.add(ENTANGLED_CAPACITOR_ITEM);
			g.add(NODE_BLOCK_ITEM);
		});

		PacketHelper.registerServer();
	}
}
