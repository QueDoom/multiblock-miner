package net.quedoom.multiblock_miner.modmenu;

import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.quedoom.multiblock_miner.config.QMMConfigManager;
import net.quedoom.multiblock_miner.config.QMMConfigs;

import java.util.List;

public class QMMOptionScreen extends OptionsSubScreen {
    public QMMOptionScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("Q's Multiblock Miner Options"));
    }

    @Override
    protected void addOptions() {
        if (this.list == null) return;
        List<AbstractWidget> widgets = QMMConfigs.asWidgets(this.options);
        for (int i = 0; i < widgets.size(); i += 2) {
            AbstractWidget left = widgets.get(i);
            AbstractWidget right = i + 1 < widgets.size() ? widgets.get(i + 1) : null;
            this.list.addSmall(left, right);
        }
    }

    @Override
    public void added() {
        ModMenu.LOGGER.info("Opened Config Screen for Q's Multiblock Miner");
    }

    @Override
    public void removed() {
        QMMConfigManager.save();
        QMMConfigManager.load();
    }
}
