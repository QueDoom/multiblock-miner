package net.quedoom.multiblock_miner;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.quedoom.multiblock_miner.config.QMMConfigManager;
import net.quedoom.multiblock_miner.networking.MultimineState;
import net.quedoom.multiblock_miner.things.DestroyageMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QSMultiblockMiner implements ModInitializer {
	public static final String MOD_ID = "qsmultiblockminer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModLoaded.load();
		QMMConfigManager.init();

		DestroyageMgr.init();
		MultimineState.init();
	}

	public static Identifier of(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static Component translatable(String suffix, String prefix) {
		return Component.translatable(translationString(suffix, prefix));
	}
	public static String translationString(String prefix, String suffix) {
		return prefix + '.' + MOD_ID + '.' + suffix;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
