package me.steven.wirelessnetworks.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import me.steven.wirelessnetworks.PacketHelper;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
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
        if (blockEntity instanceof NetworkNodeBlockEntity) {
            String networkId = ((NetworkNodeBlockEntity) blockEntity).getNetworkId();
            if (networkId != null) {
                label.setText(new LiteralText(networkId));
            }
        }
        panel.add(label, 0, 1);

        WListPanel<String, WButton> list = new WListPanel<>(networks, WButton::new, (c, b) -> {
            b.setLabel(new LiteralText(c));
            b.setOnClick(() -> {
                label.setText(new LiteralText(c));
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeString(c);
                ClientPlayNetworking.send(PacketHelper.SELECT_NETWORK, buf);
            });
            b.setSize(20, 30);
        });

        panel.add(list, 0, 2);
        list.setSize(100, 100);

        WButton button = new WButton();
        button.setLabel(new LiteralText("Create network"));
        button.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(true);
            ClientPlayNetworking.send(PacketHelper.OPEN_CONFIGURE_SCREEN, buf);
        });
        panel.add(button, 5, 8);

        panel.validate(this);
    }
}
