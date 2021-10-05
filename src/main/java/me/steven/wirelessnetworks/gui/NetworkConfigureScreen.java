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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalLong;
import java.util.UUID;
import java.util.regex.Pattern;

public class NetworkConfigureScreen extends SyncedGuiDescription {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    private static final Identifier TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/configure_network_screen.png");
    private static final Identifier SAVE_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_save.png");
    private static final Identifier PRIVATE_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_private.png");
    private static final Identifier PUBLIC_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_public.png");

    public final WWarning warning = new WWarning();

    private WTextField networkIdField = null;

    public NetworkConfigureScreen(BlockPos pos, @Nullable String networkId, UUID owner, boolean isProtected, long energyCapacity, long maxInput, long maxOutput, int syncId, PlayerInventory playerInventory) {
        super(WirelessNetworks.CONFIGURE_SCREEN_TYPE, syncId, playerInventory);
        WGridPanel panel = new WGridPanel();
        this.rootPanel = panel;

        panel.add(warning, 0, 0);
        warning.setLocation(0, -18);

        if (networkId == null) {
            networkIdField = new WNoBGTextField();
            networkIdField.setSuggestion(new TranslatableText("gui.wirelessnetworks.network.suggestion"));
            panel.add(networkIdField, 0, 0);
            networkIdField.setMaxLength(10);
            networkIdField.setSize(80, 18);
        } else {
            WLabel label = new WLabel(Utils.getDisplayId(networkId), -1);
            panel.add(label,0, 0);
            label.setLocation(4, 7);
        }

        WWidget energyCapacityTooltip = new WWidget() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.store"));
            }
        };
        panel.add(energyCapacityTooltip, 1, 3);
        energyCapacityTooltip.setSize(20, 20);
        energyCapacityTooltip.setLocation(0, 18 + 18 + 18 + 2);
        WTextField energyCapacityField = new WNoBGTextField();
        energyCapacityField.setText(String.valueOf((int) energyCapacity));
        energyCapacityField.setTextPredicate(Utils::isInt);
        panel.add(energyCapacityField, 1, 3);
        energyCapacityField.setSize(65, 20);
        energyCapacityField.setLocation(22, 18 + 18 + 18 + 2);

        WWidget maxInputTooltip = new WWidget() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.receive"));
            }
        };
        panel.add(maxInputTooltip, 1, 1);
        maxInputTooltip.setSize(20, 20);
        maxInputTooltip.setLocation(0, 14 + 5);
        WTextField maxInputField = new WNoBGTextField();
        maxInputField.setText(String.valueOf((int) maxInput));
        maxInputField.setTextPredicate(Utils::isInt);
        panel.add(maxInputField, 1, 1);
        maxInputField.setSize(65, 20);
        maxInputField.setLocation(22, 14 + 5);


        WWidget maxOutputTooltip = new WWidget() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.send"));
            }
        };
        panel.add(maxOutputTooltip, 1, 2);
        maxOutputTooltip.setSize(20, 20);
        maxOutputTooltip.setLocation(0, 18 + 16 + 4);
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
                    tooltip.add(new TranslatableText("gui.wirelessnetworks.network.public"));
                else
                    tooltip.add(new TranslatableText("gui.wirelessnetworks.network.private"));
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
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.save"));
            }
        };
        save.setLabel(new TranslatableText("gui.wirelessnetworks.network.save"));
        save.setOnClick(() -> {
            OptionalLong input = validate(maxInputField, new TranslatableText("wirelessnetworks.input"));
            OptionalLong output = validate(maxOutputField, new TranslatableText("wirelessnetworks.output"));
            OptionalLong capacity = validate(energyCapacityField, new TranslatableText("wirelessnetworks.capacity"));
            if (!capacity.isPresent() || !input.isPresent() || !output.isPresent()) return;
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(networkId == null);
            buf.writeString(networkIdField != null ? Utils.sanitizeId(networkIdField.getText().trim()) : networkId);
            buf.writeLong(capacity.getAsLong());
            buf.writeLong(input.getAsLong());
            buf.writeLong(output.getAsLong());
            buf.writeBoolean(isProtectedToggle.getToggle());
            ClientPlayNetworking.send(PacketHelper.UPDATE_NETWORK, buf);
        });
        panel.add(save, 3, 6);
        save.setLocation(4 * 18 + 9, 4 * 18 + 9 + 4);
        save.setSize(8, 8);
        save.setIcon(SAVE_TEXTURE_ID);

        panel.validate(this);

        if (networkIdField != null)
            networkIdField.requestFocus();
    }

    private OptionalLong validate(WTextField field, TranslatableText title) {
        String text = field.getText();
        if (!NUMBER_PATTERN.matcher(text).matches()) {
            warning.text = new TranslatableText("warning.wirelessnetworks.network.invalid.numbers.only", title);
            warning.ticksRemaining = 400;
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(text));
        } catch (NumberFormatException e) {
            warning.text = new TranslatableText("warning.wirelessnetworks.network.invalid", title);
            warning.ticksRemaining = 400;
            return OptionalLong.empty();
        }
    }

    @Override
    public void addPainters() {
        super.addPainters();
        this.rootPanel.setBackgroundPainter((matrices, x, y, panel) ->
                ScreenDrawing.texturedRect(matrices, x-8, y, 90 + 16, 92 + 8, TEXTURE_ID, -1));
    }
}
