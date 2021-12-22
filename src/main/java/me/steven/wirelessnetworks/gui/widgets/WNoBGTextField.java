package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

public class WNoBGTextField extends WTextField {
    private static final int BACKGROUND_COLOR = 0xFF000000;
    private static final int BORDER_COLOR_SELECTED = 0xFFFFFFA0;
    private static final int BORDER_COLOR_UNSELECTED = 0xFFA0A0A0;
    private static final int CURSOR_COLOR = 0xFFD0D0D0;
    @Environment(EnvType.CLIENT)
    protected void renderBox(MatrixStack matrices, int x, int y) {

    }
}
