package me.steven.wirelessnetworks.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import me.steven.wirelessnetworks.PacketHelper;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NetworkConfigureScreen extends SyncedGuiDescription {

    private static final Identifier TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/configure_network_screen.png");

    private WTextField networkIdField = null;

    public NetworkConfigureScreen(BlockPos pos, @Nullable String networkId, UUID owner, boolean isProtected, double energyCapacity, double maxInput, double maxOutput, int syncId, PlayerInventory playerInventory) {
        super(WirelessNetworks.CONFIGURE_SCREEN_TYPE, syncId, playerInventory);
        WGridPanel panel = new WGridPanel();
        this.rootPanel = panel;
        if (networkId == null) {
            networkIdField = new WTextField();
            networkIdField.setSuggestion("Network ID");
            panel.add(networkIdField, 0, 0);
            networkIdField.setSize(80, 18);
        } else {
            WLabel label = new WLabel(Utils.getDisplayId(networkId), -1);
            panel.add(label,0, 0);
        }

        WTextField energyCapacityField = new WTextField() {
            @Override
            public void setSize(int x, int y) {
                this.width = x;
                this.height = y;
            }
        };
        energyCapacityField.setText(String.valueOf((int) energyCapacity));
        energyCapacityField.setTextPredicate(Utils::isInt);
        panel.add(energyCapacityField, 1, 3);
        energyCapacityField.setSize(65, 16);
        energyCapacityField.setLocation(22, 18 + 18 + 20);

        WTextField maxInputField = new WTextField() {
            @Override
            public void setSize(int x, int y) {
                this.width = x;
                this.height = y;
            }
        };
        maxInputField.setText(String.valueOf((int) maxInput));
        maxInputField.setTextPredicate(Utils::isInt);
        panel.add(maxInputField, 1, 2);
        maxInputField.setSize(65, 16);
        maxInputField.setLocation(22, 18 + 18);

        WTextField maxOutputField = new WTextField() {
            @Override
            public void setSize(int x, int y) {
                this.width = x;
                this.height = y;
            }
        };
        maxOutputField.setText(String.valueOf((int) maxOutput));
        maxOutputField.setTextPredicate(Utils::isInt);
        panel.add(maxOutputField, 1, 1);
        maxOutputField.setSize(65, 16);
        maxOutputField.setLocation(22, 16);

        WToggleButton isProtectedToggle = new WToggleButton();
        isProtectedToggle.setToggle(isProtected);
        panel.add(isProtectedToggle, 1, 4);
        isProtectedToggle.setLocation(18, 4 * 18 + 9);

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
        panel.add(save, 3, 4);
        save.setLocation(3 * 18, 4 * 18 + 9);

        panel.validate(this);
    }

    @Override
    public void addPainters() {
        super.addPainters();
        this.rootPanel.setBackgroundPainter((x, y, panel) ->
                ScreenDrawing.texturedRect(x-8, y-8, 90 + 16, 92 + 16, TEXTURE_ID, -1));
    }
}
