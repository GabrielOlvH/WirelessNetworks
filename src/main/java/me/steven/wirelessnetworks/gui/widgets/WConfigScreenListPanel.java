package me.steven.wirelessnetworks.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WListPanel;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class WConfigScreenListPanel extends WListPanel<String, WNetworkListEntry> {

    /**
     * Constructs a list panel.
     *
     * @param data         the list data
     * @param supplier     the widget supplier that creates unconfigured widgets
     * @param configurator the widget configurator that configures widgets to display the passed data
     */
    public WConfigScreenListPanel(List<String> data, Supplier<WNetworkListEntry> supplier, BiConsumer<String, WNetworkListEntry> configurator) {
        super(data, supplier, configurator);
        scrollBar = new WConfigScreenScrollbar();
        scrollBar.setMaxValue(data.size());
        scrollBar.setParent(this);
    }
}
