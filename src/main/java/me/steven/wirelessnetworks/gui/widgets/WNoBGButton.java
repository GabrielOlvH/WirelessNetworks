package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WNoBGButton extends WButton {

    @Override
    public void setSize(int x, int y) {
        this.width = x;
        this.height = y;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        Icon icon = getIcon();
        if (icon != null) {
            icon.paint(matrices, x + 1, y + 1, 8);
        }
    }

    public void setIcon(Identifier identifier) {
        this.setIcon((f, u, c, k) -> ScreenDrawing.texturedRect(u, c, width, height, identifier, -1));
    }
}
