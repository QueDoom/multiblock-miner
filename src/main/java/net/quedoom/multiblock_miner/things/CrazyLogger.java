package net.quedoom.multiblock_miner.things;

import net.quedoom.multiblock_miner.QSMultiblockMiner;
import net.quedoom.multiblock_miner.config.QMMConfigManager;

public class CrazyLogger {
    static void info(String message, Object... args) {
        if (QMMConfigManager.CONFIG.crazyLogging) {
            QSMultiblockMiner.LOGGER.info(message, args);
        }
    }
    static void warn(String message, Object... args) {
        if (QMMConfigManager.CONFIG.crazyLogging) {
            QSMultiblockMiner.LOGGER.warn(message, args);
        }
    }
    static void err(String message, Object... args) {
        if (QMMConfigManager.CONFIG.crazyLogging) {
            QSMultiblockMiner.LOGGER.error(message, args);
        }
    }
}
