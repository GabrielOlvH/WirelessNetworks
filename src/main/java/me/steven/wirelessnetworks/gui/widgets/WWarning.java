package me.steven.wirelessnetworks.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import me.steven.wirelessnetworks.mixin.HandledScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class WWarning extends WWidget {

    public int ticksRemaining;
    public String text;

    public float bgWidth = 0;

    @Override
    @Environment(EnvType.CLIENT)
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        x = (((HandledScreenAccessor)currentScreen).getX()) + parent.getWidth() / 2;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int width = textRenderer.getWidth(text);
        if (ticksRemaining < 0){
            if (bgWidth > 0)
            bgWidth -= width * 0.07;
        } else if (bgWidth < width) {
            bgWidth += width * 0.065;
            bgWidth = Math.min(width, bgWidth);
        } else if (bgWidth > width) {
            bgWidth -= width * 0.065;
            bgWidth = Math.max(width, bgWidth);
        } else {
            ticksRemaining--;
        }

        if (bgWidth > 0) {
            renderTooltipBackground(matrices, x - (int)bgWidth / 2, y, (int)bgWidth, textRenderer.fontHeight);
        }

    }

    @Environment(EnvType.CLIENT)
    public void renderTooltipBackground(MatrixStack matrices, int x, int y, int width, int height) {
        matrices.push();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrices.peek().getModel();
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 4, x + width + 3, y - 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, x - 3, y + height + 3, x + width + 3, y + height + 4, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + width + 3, y + height + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, x - 4, y - 3, x - 3, y + height + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, x + width + 3, y - 3, x + width + 4, y + height + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + width + 3, y - 3 + 1, 400, 1347420415, 1347420415);
        fillGradient(matrix4f, bufferBuilder, x - 3, y + height + 2, x + width + 3, y + height + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrices.translate(0.0D, 0.0D, 400.0D);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (bgWidth == textRenderer.getWidth(text)) {
            textRenderer.draw(text, x, y, -1, true, matrix4f, immediate, false, 0, 15728880);
        }
        immediate.draw();
        matrices.pop();
    }

    @Environment(EnvType.CLIENT)
    protected static void fillGradient(Matrix4f matrix, BufferBuilder bufferBuilder, int xStart, int yStart, int xEnd, int yEnd, int z, int colorStart, int colorEnd) {
        float f = (float)(colorStart >> 24 & 255) / 255.0F;
        float g = (float)(colorStart >> 16 & 255) / 255.0F;
        float h = (float)(colorStart >> 8 & 255) / 255.0F;
        float i = (float)(colorStart & 255) / 255.0F;
        float j = (float)(colorEnd >> 24 & 255) / 255.0F;
        float k = (float)(colorEnd >> 16 & 255) / 255.0F;
        float l = (float)(colorEnd >> 8 & 255) / 255.0F;
        float m = (float)(colorEnd & 255) / 255.0F;
        bufferBuilder.vertex(matrix, (float)xEnd, (float)yStart, (float)z).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float)xStart, (float)yStart, (float)z).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float)xStart, (float)yEnd, (float)z).color(k, l, m, j).next();
        bufferBuilder.vertex(matrix, (float)xEnd, (float)yEnd, (float)z).color(k, l, m, j).next();
    }
}
