package net.quedoom.multiblock_miner;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.quedoom.multiblock_miner.networking.MultimineStatePayload;

public class QSMultiblockMinerClient implements ClientModInitializer {
    KeyMapping.Category CATEGORY = category("qs_multiblock_miner");

    KeyMapping multiblockMine = key("multiblock_mine", InputConstants.KEY_GRAVE);
    private boolean multiblockMineWasDown = false;

    private KeyMapping.Category category(String path) {
        return KeyMapping.Category.register(
                QSMultiblockMiner.id(path)
        );
    }

    private KeyMapping key(String name, InputConstants.Type type, int key) {
        return KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        QSMultiblockMiner.translationString("key", name),
                        type, key, this.CATEGORY
                ));
    }

    private KeyMapping key(String name, int key) {
        return key(name, InputConstants.Type.KEYSYM, key);
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean down = this.multiblockMine.isDown();
            if (down != multiblockMineWasDown) {
                multiblockMineWasDown = down;
                ClientPlayNetworking.send(new MultimineStatePayload(down));
            }
        });
    }
}
