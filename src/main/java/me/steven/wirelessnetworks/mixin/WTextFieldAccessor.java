package me.steven.wirelessnetworks.mixin;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WTextField.class, remap = false)
public interface WTextFieldAccessor {

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
