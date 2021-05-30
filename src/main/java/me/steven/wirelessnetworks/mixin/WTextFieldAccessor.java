package me.steven.wirelessnetworks.mixin;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = WTextField.class, remap = false)
public interface WTextFieldAccessor {
    @Invoker("invertedRect")
    void invertedRect(MatrixStack matrices, int x, int y, int width, int height);

    @Accessor
    boolean isEditable();

    @Accessor
    int getEnabledColor();

    @Accessor
    int getUneditableColor();

    @Accessor
    String getText();

    @Accessor
    int getCursor();

    @Accessor
    Text getSuggestion();

    @Accessor
    int getSelect();
}
