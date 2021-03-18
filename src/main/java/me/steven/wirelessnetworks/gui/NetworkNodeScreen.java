package me.steven.wirelessnetworks.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import me.steven.wirelessnetworks.PacketHelper;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.widgets.WNetworkListEntry;
import me.steven.wirelessnetworks.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class NetworkNodeScreen extends SyncedGuiDescription {

    public NetworkNodeScreen(BlockPos pos, List<String> networks, int syncId, PlayerInventory playerInventory) {
        super(WirelessNetworks.NODE_SCREEN_TYPE, syncId, playerInventory);
        WGridPanel panel = new WGridPanel();
        this.rootPanel = panel;

        WLabel title = new WLabel(new LiteralText("Network Node"));
        panel.add(title, 0, 0);

        WLabel label = new WLabel(new LiteralText("No network selected"));
        BlockEntity blockEntity = world.getBlockEntity(pos);
        String[] selectedNetworkId = {null};
        if (blockEntity instanceof NetworkNodeBlockEntity) {
            selectedNetworkId[0] = ((NetworkNodeBlockEntity) blockEntity).getNetworkId();
            if (selectedNetworkId[0] != null && networks.contains(selectedNetworkId[0])) {
                label.setText(new LiteralText(Utils.getDisplayId(selectedNetworkId[0])));
            }
        }
        panel.add(label, 0, 1);
        WListPanel<String, WNetworkListEntry> list = new WListPanel<>(networks, WNetworkListEntry::new, (networkId, entry) -> {
            entry.setText(new LiteralText(Utils.getDisplayId(networkId)));
            entry.setClickAction(() -> {
                label.setText(new LiteralText(Utils.getDisplayId(networkId)));
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeString(networkId);
                selectedNetworkId[0] = networkId;
                ClientPlayNetworking.send(PacketHelper.SELECT_NETWORK, buf);
            });
            entry.setIsSelected(() -> networkId.equals(selectedNetworkId[0]));
            entry.setSize(88, 15);
        });
        list.setListItemHeight(20);

        panel.add(list, 0, 2);
        list.setSize(100, 100);

        WButton createButton = new WButton();
        createButton.setLabel(new LiteralText("Create network"));
        createButton.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(true);
            ClientPlayNetworking.send(PacketHelper.OPEN_CONFIGURE_SCREEN, buf);
        });
        panel.add(createButton, 5, 8);

        WButton configureButton = new WButton();
        configureButton.setLabel(new LiteralText("Edit network"));
        configureButton.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(false);
            buf.writeString(selectedNetworkId[0]);
            ClientPlayNetworking.send(PacketHelper.OPEN_CONFIGURE_SCREEN, buf);
        });
        panel.add(configureButton, 4, 8);

        WButton deleteNetwork = new WButton();
        deleteNetwork.setLabel(new LiteralText("Delete network"));
        deleteNetwork.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(selectedNetworkId[0]);
            buf.writeBlockPos(pos);
            ClientPlayNetworking.send(PacketHelper.DELETE_NETWORK, buf);
        });
        panel.add(deleteNetwork, 3, 8);

        panel.validate(this);
    }
}
