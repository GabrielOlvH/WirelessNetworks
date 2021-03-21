package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import net.minecraft.client.util.math.MatrixStack;

public class WFixedToggleButton extends WToggleButton {
    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        ScreenDrawing.texturedRect(x, y, width, height, isOn ? onImage : offImage, 0xFFFFFFFF);
        if (isFocused()) {
            ScreenDrawing.texturedRect(x, y, width, height, focusImage, 0xFFFFFFFF);
        }
    }
}
