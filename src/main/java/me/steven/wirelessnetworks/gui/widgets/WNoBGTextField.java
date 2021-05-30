package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import me.steven.wirelessnetworks.mixin.WTextFieldAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class WNoBGTextField extends WTextField {

    @Environment(EnvType.CLIENT)
    protected void renderTextField(MatrixStack matrices, int x, int y) {

        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        WTextFieldAccessor accessor = (WTextFieldAccessor) this;

        int textColor = accessor.isEditable() ? accessor.getEnabledColor() : accessor.getUneditableColor();

        //TODO: Scroll offset
        String trimText = font.trimToWidth(accessor.getText(), this.width-OFFSET_X_TEXT);

        boolean selection = (accessor.getSelect()!=-1);
        boolean focused = this.isFocused();
        int textX = x + OFFSET_X_TEXT;

        int textY = y + (height - 8) / 2;

        //TODO: Adjust by scroll offset
        int adjustedCursor = accessor.getCursor();
        if (adjustedCursor > trimText.length()) {
            adjustedCursor = trimText.length();
        }

        int preCursorAdvance = textX;
        if (!trimText.isEmpty()) {
            String string_2 = trimText.substring(0,adjustedCursor);
            preCursorAdvance = font.drawWithShadow(matrices, string_2, textX, textY, textColor);
        }

        if (adjustedCursor<trimText.length()) {
            font.drawWithShadow(matrices, trimText.substring(adjustedCursor), preCursorAdvance-1, (float)textY, textColor);
        }


        if (accessor.getText().length()==0 && accessor.getSuggestion() != null) {
            font.drawWithShadow(matrices, accessor.getSuggestion(), textX, textY, 0xFF808080);
        }

        if (focused && !selection) {
            if (adjustedCursor<trimText.length()) {
                ScreenDrawing.coloredRect(matrices, preCursorAdvance-1, textY-2, 1, 12, 0xFFD0D0D0);

            } else {
                font.drawWithShadow(matrices, "_", preCursorAdvance, textY, textColor);
            }
        }

        if (selection) {
            int a = WTextField.getCaretOffset(accessor.getText(), accessor.getCursor());
            int b = WTextField.getCaretOffset(accessor.getText(), accessor.getSelect());
            if (b<a) {
                int tmp = b;
                b = a;
                a = tmp;
            }
            invertedRect(matrices,textX+a-1, textY-1, Math.min(b-a, width - OFFSET_X_TEXT), 12);
        }
    }

    @Environment(EnvType.CLIENT)
    private void invertedRect(MatrixStack matrices, int x, int y, int width, int height) {
        ((WTextFieldAccessor) this).invertedRect(matrices, x, y, width, height);
    }
}
