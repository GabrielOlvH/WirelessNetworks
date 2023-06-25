package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WScrollBar;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class WConfigScreenScrollbar extends WScrollBar {
    private static final int TOP_COLOR = 0xFF_5c5c6b;
    private static final int MIDDLE_COLOR = 0xFF_42424d;
    private static final int BOTTOM_COLOR = 0xFF_292936;

    public WConfigScreenScrollbar() {
        super(Axis.VERTICAL);
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        if (maxValue <= 0) return;

        if (axis == Axis.HORIZONTAL) {
            ScreenDrawing.drawBeveledPanel(context, x + 1 + getHandlePosition(), y + 1, getHandleSize(), height - 2, TOP_COLOR, MIDDLE_COLOR, BOTTOM_COLOR);
        } else {
            ScreenDrawing.drawBeveledPanel(context, x + 1, y + 1 + getHandlePosition(), width - 2, getHandleSize(), TOP_COLOR, MIDDLE_COLOR, BOTTOM_COLOR);
        }
    }
}
