package net.quedoom.multiblock_miner.config;

import com.google.gson.*;
import net.darkhax.pricklemc.common.api.annotations.Value;
import net.darkhax.pricklemc.common.api.config.ConfigManager;
import net.darkhax.pricklemc.common.impl.PrickleMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class QMMConfigManager {
    private static final String CONFIG_ID = "qmm";
    public static QMMConfigs CONFIG = firstLoad();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_ID + ".json");
    }

    private static QMMConfigs firstLoad() {
        PrickleMod.LOG.info("Loaded Q's Multiblock Miner configs!");
        return ConfigManager.load(CONFIG_ID, new QMMConfigs());
    }

    public static void load() {
        CONFIG = ConfigManager.load(CONFIG_ID, new QMMConfigs());
    }

    public static void save() {
        PrickleMod.LOG.info("Saving configs for Q's Multiblock Miner...");
        Path path = path();
        System.currentTimeMillis();
        long time;
        try {
            JsonObject root;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }

            for (Field field : QMMConfigs.class.getDeclaredFields()) {
                time = System.currentTimeMillis();
                Value meta = field.getAnnotation(Value.class);
                if (meta == null) continue;
                field.setAccessible(true);

                String key = findKey(root, field, meta);
                JsonObject entry = root.has(key) && root.get(key).isJsonObject()
                        ? root.getAsJsonObject(key)
                        : new JsonObject();

                JsonElement value = GSON.toJsonTree(field.get(CONFIG));
                entry.add("value", value);
                root.add(key, entry);
                long writeTime = System.currentTimeMillis() - time;
                if (writeTime > 10) {
                    PrickleMod.LOG.warn("Wrote {} in {}ms", field.getName(), writeTime);
                } else {
                    PrickleMod.LOG.info("Wrote {} in {}ms", field.getName(), writeTime < 1 ? "<1" : writeTime);
                }
            }

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        }
        catch (IOException | IllegalAccessException e) {
            PrickleMod.LOG.error("Couldn't save Q's Multiblock Miner config!");
            e.printStackTrace();
        }
        PrickleMod.LOG.info("Saved Q's Multiblock Miner config!");
    }

    private static String findKey(JsonObject root, Field field, Value meta) {
        String snake = field.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        for (String candidate : new String[]{meta.name(), field.getName(), snake}) {
            if (!candidate.isEmpty() && root.has(candidate)) return candidate;
        }
        return meta.name().isEmpty() ? field.getName() : meta.name();
    }

    public static void init() {
        PrickleMod.LOG.info("Loading configs for Q's Multiblock Miner...");
    }
}
