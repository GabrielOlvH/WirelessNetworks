package me.steven.wirelessnetworks;

import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import me.steven.wirelessnetworks.block.NetworkBlock;
import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.NetworkConfigureScreen;
import me.steven.wirelessnetworks.gui.NetworkNodeScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WirelessNetworks implements ModInitializer {

	public static final String MOD_ID = "wirelessnetworks";

	public static final Block NODE_BLOCK = new NetworkBlock(FabricBlockSettings.of(Material.METAL).strength(2f).breakByTool(FabricToolTags.PICKAXES, 2).nonOpaque());
	public static final BlockItem NODE_BLOCK_ITEM = new BlockItem(NODE_BLOCK, new Item.Settings().group(ItemGroup.SEARCH));
	public static final BlockEntityType<NetworkNodeBlockEntity> NODE_BLOCK_ENTITY_TYPE
			= FabricBlockEntityTypeBuilder.create(NetworkNodeBlockEntity::new, NODE_BLOCK).build(null);
	public static final Item ENTANGLED_CAPACITOR_ITEM = new Item(new Item.Settings().group(ItemGroup.SEARCH));

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
				String networkId = isNewNetwork ? null : buf.readString(32767);
				double energyCapacity = buf.readDouble();
				double maxInput = buf.readDouble();
				double maxOutput = buf.readDouble();
				boolean isProtected = buf.readBoolean();
				UUID owner = buf.readUuid();
				return new NetworkConfigureScreen(pos, networkId, owner, isProtected, energyCapacity, maxInput, maxOutput, syncId, inventory);
			});

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "node_block"), NODE_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "node_block"), NODE_BLOCK_ITEM);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "node_block"), NODE_BLOCK_ENTITY_TYPE);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "entangled_capacitor"), ENTANGLED_CAPACITOR_ITEM);

		EnergyApi.SIDED.registerForBlockEntities((blockEntity, direction) -> {
			if (
					blockEntity instanceof NetworkNodeBlockEntity
							&& blockEntity.getWorld() != null
							&& !blockEntity.getWorld().isClient
							&& direction != Direction.UP
			) {
				return ((NetworkNodeBlockEntity) blockEntity).getNetwork().orElse(null);
			}
			return null;
		}, NODE_BLOCK_ENTITY_TYPE);

		PacketHelper.registerServer();
	}
}