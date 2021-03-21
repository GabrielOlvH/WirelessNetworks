package me.steven.wirelessnetworks.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

public class WNoBGTextField extends WTextField {

    @Environment(EnvType.CLIENT)
    protected void renderTextField(MatrixStack matrices, int x, int y) {

        TextRenderer font = MinecraftClient.getInstance().textRenderer;


        int textColor = this.editable ? this.enabledColor : this.uneditableColor;

        //TODO: Scroll offset
        String trimText = font.trimToWidth(this.text, this.width-OFFSET_X_TEXT);

        boolean selection = (select!=-1);
        boolean focused = this.isFocused();
        int textX = x + OFFSET_X_TEXT;

        int textY = y + (height - 8) / 2;

        //TODO: Adjust by scroll offset
        int adjustedCursor = this.cursor;
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


        if (text.length()==0 && this.suggestion != null) {
            font.drawWithShadow(matrices, this.suggestion, textX, textY, 0xFF808080);
        }

        if (focused && !selection) {
            if (adjustedCursor<trimText.length()) {
                ScreenDrawing.coloredRect(preCursorAdvance-1, textY-2, 1, 12, 0xFFD0D0D0);

            } else {
                font.drawWithShadow(matrices, "_", preCursorAdvance, textY, textColor);
            }
        }

        if (selection) {
            int a = WTextField.getCaretOffset(text, cursor);
            int b = WTextField.getCaretOffset(text, select);
            if (b<a) {
                int tmp = b;
                b = a;
                a = tmp;
            }
            invertedRect(textX+a-1, textY-1, Math.min(b-a, width - OFFSET_X_TEXT), 12);
        }
    }

    @Environment(EnvType.CLIENT)
    private void invertedRect(int x, int y, int width, int height) {
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBuffer();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder_1.begin(GL11.GL_QUADS, VertexFormats.POSITION);
        bufferBuilder_1.vertex(x,       y+height, 0.0D).next();
        bufferBuilder_1.vertex(x+width, y+height, 0.0D).next();
        bufferBuilder_1.vertex(x+width, y,        0.0D).next();
        bufferBuilder_1.vertex(x,       y,        0.0D).next();
        tessellator_1.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }
}
