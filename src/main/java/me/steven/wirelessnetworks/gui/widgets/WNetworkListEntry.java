package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class WNetworkListEntry extends WWidget {
    private Text text;
    private Supplier<Boolean> isSelected = () -> false;
    private Runnable clickAction;

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
        ScreenDrawing.drawBeveledPanel(x, y, width, height);
        if (isSelected.get()) ScreenDrawing.coloredRect(x, y, width, height, 0x22000099);
        ScreenDrawing.drawStringWithShadow(matrices, text.asOrderedText(),
                HorizontalAlignment.CENTER, x, y + ((height - 8) / 2),
                width, WLabel.DEFAULT_DARKMODE_TEXT_COLOR);
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    @Override
    public void onClick(int x, int y, int button) {
        clickAction.run();
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
