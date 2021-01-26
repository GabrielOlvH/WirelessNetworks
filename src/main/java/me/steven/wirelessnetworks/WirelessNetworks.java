package me.steven.wirelessnetworks;

import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import me.steven.wirelessnetworks.block.NetworkBlock;
import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreen;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class WirelessNetworks implements ModInitializer {

	public static final String MOD_ID = "wirelessnetworks";

	public static final Block NODE_BLOCK = new NetworkBlock(FabricBlockSettings.of(Material.METAL));
	public static final BlockItem NODE_BLOCK_ITEM = new BlockItem(NODE_BLOCK, new Item.Settings());
	public static final BlockEntityType<NetworkNodeBlockEntity> NODE_BLOCK_ENTITY_TYPE
			= BlockEntityType.Builder.create(NetworkNodeBlockEntity::new, NODE_BLOCK).build(null);

	public static final ExtendedScreenHandlerType<NetworkNodeScreen> NODE_SCREEN_TYPE = (ExtendedScreenHandlerType<NetworkNodeScreen>)
			ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "node_screen"), (syncId, inventory, buf) -> {
				BlockPos blockPos = buf.readBlockPos();
				int size = buf.readInt();
				List<String> keys = new ArrayList<>();
				for (int index = 0; index < size; index++) {
					keys.add(buf.readString(32767));
				}
				return new NetworkNodeScreen(blockPos, keys, syncId, inventory);
			});

	public static final ExtendedScreenHandlerType<NetworkConfigureScreen> CONFIGURE_SCREEN_TYPE = (ExtendedScreenHandlerType<NetworkConfigureScreen>)
			ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "configure_node_screen"), (syncId, inventory, buf) -> {
				BlockPos pos = buf.readBlockPos();
				boolean isNewNetwork = buf.readBoolean();
				String networkId = isNewNetwork ? null : buf.readString();
				double energyCapacity = buf.readDouble();
				double maxInput = buf.readDouble();
				double maxOutput = buf.readDouble();
				return new NetworkConfigureScreen(pos, networkId, energyCapacity, maxInput, maxOutput, syncId, inventory);
			});

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "node_block"), NODE_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "node_block"), NODE_BLOCK_ITEM);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "node_block"), NODE_BLOCK_ENTITY_TYPE);

		EnergyApi.SIDED.registerForBlockEntities((blockEntity, direction) -> {
			if (blockEntity instanceof NetworkNodeBlockEntity && blockEntity.getWorld() != null && !blockEntity.getWorld().isClient) {
				return ((NetworkNodeBlockEntity) blockEntity).getNetwork().orElse(null);
			}
			return null;
		}, NODE_BLOCK_ENTITY_TYPE);

		PacketHelper.registerServer();
	}
}