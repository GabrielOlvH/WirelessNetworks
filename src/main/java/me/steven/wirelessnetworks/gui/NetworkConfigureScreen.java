package me.steven.wirelessnetworks.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import me.steven.wirelessnetworks.PacketHelper;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.gui.widgets.WFixedToggleButton;
import me.steven.wirelessnetworks.gui.widgets.WNoBGButton;
import me.steven.wirelessnetworks.gui.widgets.WNoBGTextField;
import me.steven.wirelessnetworks.gui.widgets.WWarning;
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
    private static final Identifier SAVE_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_save.png");
    private static final Identifier PRIVATE_TEXTURE_ID =new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_private.png");
    private static final Identifier PUBLIC_TEXTURE_ID =new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_public.png");

    public final WWarning warning = new WWarning();

    private WTextField networkIdField = null;

    public NetworkConfigureScreen(BlockPos pos, @Nullable String networkId, UUID owner, boolean isProtected, double energyCapacity, double maxInput, double maxOutput, int syncId, PlayerInventory playerInventory) {
        super(WirelessNetworks.CONFIGURE_SCREEN_TYPE, syncId, playerInventory);
        WGridPanel panel = new WGridPanel();
        this.rootPanel = panel;

        panel.add(warning, 0, 0);
        warning.setLocation(0, -18);

        if (networkId == null) {
            networkIdField = new WNoBGTextField();
            networkIdField.setSuggestion("Network ID");
            panel.add(networkIdField, 0, 0);
            networkIdField.setSize(80, 18);
        } else {
            WLabel label = new WLabel(Utils.getDisplayId(networkId), -1);
            panel.add(label,0, 0);
            label.setLocation(4, 7);
        }

        WTextField energyCapacityField = new WNoBGTextField();
        energyCapacityField.setText(String.valueOf((int) energyCapacity));
        energyCapacityField.setTextPredicate(Utils::isInt);
        panel.add(energyCapacityField, 1, 3);
        energyCapacityField.setSize(65, 20);
        energyCapacityField.setLocation(22, 18 + 18 + 18 + 2);

        WTextField maxInputField = new WNoBGTextField();
        maxInputField.setText(String.valueOf((int) maxInput));
        maxInputField.setTextPredicate(Utils::isInt);
        panel.add(maxInputField, 1, 1);
        maxInputField.setSize(65, 20);
        maxInputField.setLocation(22, 14 + 5);

        WTextField maxOutputField = new WNoBGTextField();
        maxOutputField.setText(String.valueOf((int) maxOutput));
        maxOutputField.setTextPredicate(Utils::isInt);
        panel.add(maxOutputField, 1, 2);
        maxOutputField.setSize(65, 20);
        maxOutputField.setLocation(22, 18 + 16 + 4);

        WToggleButton isProtectedToggle = new WFixedToggleButton() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                if (getToggle())
                    tooltip.add(new LiteralText("Make network public"));
                else
                    tooltip.add(new LiteralText("Make network private"));
            }
        };
        isProtectedToggle.setToggle(isProtected);
        panel.add(isProtectedToggle, 2, 4);
        isProtectedToggle.setSize(8, 8);
        isProtectedToggle.setLocation(18 * 3 + 9, 4 * 18 + 9 + 4);
        isProtectedToggle.setOffImage(new Texture(PUBLIC_TEXTURE_ID));
        isProtectedToggle.setOnImage(new Texture(PRIVATE_TEXTURE_ID));

        WNoBGButton save = new WNoBGButton() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new LiteralText("Save"));
            }
        };
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
        panel.add(save, 3, 6);
        save.setLocation(4 * 18 + 9, 4 * 18 + 9 + 4);
        save.setSize(8, 8);
        save.setIcon(SAVE_TEXTURE_ID);

        panel.validate(this);
    }

    @Override
    public void addPainters() {
        super.addPainters();
        this.rootPanel.setBackgroundPainter((x, y, panel) ->
                ScreenDrawing.texturedRect(x-8, y, 90 + 16, 92 + 8, TEXTURE_ID, -1));
    }
}
