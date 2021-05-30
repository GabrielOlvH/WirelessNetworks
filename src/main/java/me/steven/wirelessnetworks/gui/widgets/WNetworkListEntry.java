package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import me.steven.wirelessnetworks.WirelessNetworks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class WNetworkListEntry extends WWidget {

    private static final Identifier PRIVATE_TEXTURE_ID = new Identifier(WirelessNetworks.MOD_ID, "textures/gui/icon_private.png");

    private final World world;
    private String id;
    private final UUID owner;
    private Text text;
    private Supplier<Boolean> isSelected = () -> false;
    private Runnable clickAction;

    public WNetworkListEntry(UUID owner, World world) {
        this.owner = owner;
        this.world = world;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Text getText() {
        return text;
    }

    public void setClickAction(Runnable clickAction) {
        this.clickAction = clickAction;
    }

    public Runnable getClickAction() {
        return clickAction;
    }

    public void setIsSelected(Supplier<Boolean> isSelected) {
        this.isSelected = isSelected;
    }

    public Supplier<Boolean> getIsSelected() {
        return isSelected;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        if (isSelected.get()) ScreenDrawing.coloredRect(matrices, x, y, width, height, 0x22000099);
        ScreenDrawing.drawStringWithShadow(matrices, text.asOrderedText(),
                HorizontalAlignment.LEFT, x, y + ((height - 8) / 2),
                width, WLabel.DEFAULT_DARKMODE_TEXT_COLOR);


        int index = getId().indexOf(":");
        if (index > 0) {
            ScreenDrawing.texturedRect(matrices, x + width - 8, y, 8, 8, PRIVATE_TEXTURE_ID, -1);
        }
    }

    private Optional<String> getOwnerUuid() {
        int index = getId().indexOf(":");
        if (index > 0) return Optional.of(getId().substring(0, index));
        else return Optional.empty();
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    @Override
    public void addTooltip(TooltipBuilder tooltip) {
        getOwnerUuid().ifPresent((s) -> {
            if (s.equals(owner.toString())) {
                tooltip.add(new LiteralText("You own this network"));
            } else {
                PlayerEntity player = world.getPlayerByUuid(UUID.fromString(s));
                String string = player != null ? player.getDisplayName().asString() : s;
                tooltip.add(new LiteralText("This network is owned by "));
                tooltip.add(new LiteralText((string)));
            }
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InputResult onClick(int x, int y, int button) {
        clickAction.run();
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return InputResult.PROCESSED;
    }
}
