package me.steven.wirelessnetworks.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import me.steven.wirelessnetworks.PacketHelper;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NetworkConfigureScreen extends SyncedGuiDescription {

    private WTextField networkIdField = null;

    public NetworkConfigureScreen(BlockPos pos, @Nullable String networkId, UUID owner, boolean isProtected, double energyCapacity, double maxInput, double maxOutput, int syncId, PlayerInventory playerInventory) {
        super(WirelessNetworks.CONFIGURE_SCREEN_TYPE, syncId, playerInventory);
        WGridPanel panel = new WGridPanel();
        this.rootPanel = panel;
        if (networkId == null) {
            networkIdField = new WTextField();
            networkIdField.setSuggestion("Network ID");
            panel.add(networkIdField, 0, 0);
            networkIdField.setSize(80, 20);
        } else {
            WLabel label = new WLabel(Utils.getDisplayId(networkId));
            panel.add(label,0, 0);
        }

        WTextField energyCapacityField = new WTextField();
        energyCapacityField.setText(String.valueOf((int) energyCapacity));
        energyCapacityField.setTextPredicate(Utils::isInt);
        panel.add(energyCapacityField, 0, 2);
        energyCapacityField.setSize(60, 20);


        WTextField maxInputField = new WTextField();
        maxInputField.setText(String.valueOf((int) maxInput));
        maxInputField.setTextPredicate(Utils::isInt);
        panel.add(maxInputField, 0, 3);
        maxInputField.setSize(60, 20);

        WTextField maxOutputField = new WTextField();
        maxOutputField.setText(String.valueOf((int) maxOutput));
        maxOutputField.setTextPredicate(Utils::isInt);
        panel.add(maxOutputField, 0, 4);
        maxOutputField.setSize(60, 20);

        WToggleButton isProtectedToggle = new WToggleButton();
        isProtectedToggle.setToggle(isProtected);
        panel.add(isProtectedToggle, 2, 1);

        WButton save = new WButton();
        save.setLabel(new LiteralText("Save"));
        save.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(networkId == null);
            buf.writeString(networkIdField != null ? Utils.sanitizeId(networkIdField.getText()) : networkId);
            buf.writeDouble(Double.parseDouble(energyCapacityField.getText()));
            buf.writeDouble(Double.parseDouble(maxInputField.getText()));
            buf.writeDouble(Double.parseDouble(maxOutputField.getText()));
            buf.writeBoolean(isProtectedToggle.getToggle());
            ClientPlayNetworking.send(PacketHelper.UPDATE_NETWORK, buf);
        });
        panel.add(save, 4, 4);

        panel.validate(this);
    }
}
