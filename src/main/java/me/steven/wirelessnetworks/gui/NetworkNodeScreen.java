package me.steven.wirelessnetworks.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import me.steven.wirelessnetworks.PacketHelper;
import me.steven.wirelessnetworks.WirelessNetworks;
import me.steven.wirelessnetworks.blockentity.NetworkNodeBlockEntity;
import me.steven.wirelessnetworks.gui.widgets.WConfigScreenListPanel;
import me.steven.wirelessnetworks.gui.widgets.WNetworkListEntry;
import me.steven.wirelessnetworks.gui.widgets.WNoBGButton;
import me.steven.wirelessnetworks.gui.widgets.WWarning;
import me.steven.wirelessnetworks.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkNodeScreen extends SyncedGuiDescription {

    private static final Identifier TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/select_network_screen.png");

    private static final Identifier ADD_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_add.png");
    private static final Identifier DELETE_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_delete.png");
    private static final Identifier EDIT_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_edit.png");
    private static final Identifier INPUT_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_input.png");
    private static final Identifier OUTPUT_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_output.png");

    @Environment(EnvType.CLIENT)
    public final WWarning warning = new WWarning();

    public NetworkNodeScreen(BlockPos pos, boolean input, List<String> networks, int syncId, PlayerInventory playerInventory) {
        super(WirelessNetworks.NODE_SCREEN_TYPE, syncId, playerInventory);

        boolean[] i = { input };

        AtomicInteger confirm = new AtomicInteger();
        WNoBGButton deleteNetwork = new WNoBGButton() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                if (confirm.get() == 1) {
                    tooltip.add(new TranslatableText("gui.wirelessnetworks.network.delete.confirm"));
                } else {
                    tooltip.add(new TranslatableText("gui.wirelessnetworks.network.delete"));
                }
            }
        };

        WGridPanel panel = new WGridPanel() {
            @Override
            public InputResult onMouseMove(int x, int y) {
                super.onMouseMove(x, y);
                if (!deleteNetwork.isWithinBounds(x, y)) {
                    confirm.set(0);
                    return InputResult.PROCESSED;
                }
                return InputResult.IGNORED;
            }
        };
        this.rootPanel = panel;

        panel.add(warning, 0, 0);
        warning.setLocation(0, -5);

        WLabel title = new WLabel(new TranslatableText("block.wirelessnetworks.node_block"), -1);
        panel.add(title, 0, 1);

        WLabel label = new WLabel(new TranslatableText("gui.wirelessnetworks.network.select"), -1);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        String[] selectedNetworkId = {null};
        if (blockEntity instanceof NetworkNodeBlockEntity) {
            selectedNetworkId[0] = ((NetworkNodeBlockEntity) blockEntity).getNetworkId();
            if (selectedNetworkId[0] != null && networks.contains(selectedNetworkId[0])) {
                label.setText(new LiteralText(Utils.getDisplayId(selectedNetworkId[0])));
            }
        }
        panel.add(label, 0, 2);
        UUID uuid = playerInventory.player.getUuid();
        WListPanel<String, WNetworkListEntry> list = new WConfigScreenListPanel(networks, () -> new WNetworkListEntry(uuid, world), (networkId, entry) -> {
            entry.setId(networkId);
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
            entry.setSize(73, 15);
        });
        list.setListItemHeight(20);

        panel.add(list, 0, 3);
        list.setLocation(0, 18 + 18 + 12);
        list.setSize(92, 119);

        WNoBGButton createButton = new WNoBGButton() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.create"));
            }
        };
        createButton.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(true);
            ClientPlayNetworking.send(PacketHelper.OPEN_CONFIGURE_SCREEN, buf);
        });
        panel.add(createButton, 5, 10);
        createButton.setLocation(5 * 18 + 9, 9 * 18 + 12);
        createButton.setSize(8, 8);
        createButton.setIcon(ADD_TEXTURE_ID);

        WNoBGButton configureButton = new WNoBGButton() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.edit"));
            }
        };
        configureButton.setOnClick(() -> {
            if (selectedNetworkId[0] != null) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeBoolean(false);
                buf.writeString(selectedNetworkId[0]);
                ClientPlayNetworking.send(PacketHelper.OPEN_CONFIGURE_SCREEN, buf);
            }
        });
        panel.add(configureButton, 4, 10);
        configureButton.setLocation(4 * 18 + 9, 9 * 18 + 12);
        configureButton.setSize(8, 8);
        configureButton.setIcon(EDIT_TEXTURE_ID);

        deleteNetwork.setOnClick(() -> {
            if (selectedNetworkId[0] != null) {
                if (confirm.get() == 0) {
                    confirm.set(1);
                } else if (confirm.get() == 1 && Screen.hasShiftDown()) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeString(selectedNetworkId[0]);
                    buf.writeBlockPos(pos);
                    ClientPlayNetworking.send(PacketHelper.DELETE_NETWORK, buf);
                    selectedNetworkId[0] = null;
                    confirm.set(0);
                }
            }
        });
        panel.add(deleteNetwork, 3, 10);
        deleteNetwork.setLocation(3 * 18 + 9, 9 * 18 + 12);
        deleteNetwork.setSize(8, 8);
        deleteNetwork.setIcon(DELETE_TEXTURE_ID);


        WNoBGButton modeBtn = new WNoBGButton() {
            @Override
            public void addTooltip(TooltipBuilder tooltip) {
                tooltip.add(new TranslatableText("gui.wirelessnetworks.network.input." + i[0]));
            }
        };
        modeBtn.setLabel(new TranslatableText("gui.wirelessnetworks.network.input." + input));
        modeBtn.setOnClick(() -> {
            i[0] = !i[0];
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeBoolean(i[0]);
            ClientPlayNetworking.send(PacketHelper.MODE_PACKET, buf);
            modeBtn.setIcon(getTexture(i[0]));
        });
        panel.add(modeBtn, 3, 6);
        modeBtn.setLocation(2 * 18 + 9, 9 * 18 + 12);
        modeBtn.setSize(8, 8);
        modeBtn.setIcon(getTexture(i[0]));

        panel.validate(this);
    }

    private Identifier getTexture(boolean input) {
        if (input) return INPUT_TEXTURE_ID;
        else return OUTPUT_TEXTURE_ID;
    }


    @Override
    public void addPainters() {
        super.addPainters();
        this.rootPanel.setBackgroundPainter((matrices, x, y, panel) ->
                ScreenDrawing.texturedRect(matrices, x-8, y-8 + 18, 108 + 16, 164 + 16, TEXTURE_ID, -1));
    }
}