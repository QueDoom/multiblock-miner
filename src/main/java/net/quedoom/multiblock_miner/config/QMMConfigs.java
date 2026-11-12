package net.quedoom.multiblock_miner.config;

import net.darkhax.pricklemc.common.api.annotations.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.quedoom.multiblock_miner.QSMultiblockMiner;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class QMMConfigs {
    @RangedInt(min = 1, max = 10)
    @Value(comment = "How far you can multimine.")
    public int maxDepth = 4;

    @Value(comment = "For example it will stop if you switch to an empty hand when Multiblock Mining stone.")
    public boolean requireCorrectTool = true;

    @Value(comment = "Will do a Multiblock Mine if the block broken is an ore. Even if the keybind is unpressed.")
    public OreBreakPosition ignoreKeybindIfOre = OreBreakPosition.NEVER_MULTI;

    @Value(comment = "If the enchantment addon is present; does the enchantment actually need to be on the tool?")
    public boolean requireEnchantment = true;

    @FileOnlyConfig
    @Value(comment = "Logs most things. Only turn on if debugging. Is not available in modmenu options screen.")
    public boolean crazyLogging = true;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FileOnlyConfig {}

    public static List<AbstractWidget> asWidgets(Options options) {
        List<AbstractWidget> widgets = new ArrayList<>();
        for (Field field : QMMConfigs.class.getDeclaredFields()) {
            Value meta = field.getAnnotation(Value.class);
            if (meta == null || field.isAnnotationPresent(FileOnlyConfig.class)) continue;
            field.setAccessible(true);

            String key = "options.qsmultiblockminer." + field.getName();
            Component name = Component.translatable(key);
            Component comment;
            if (meta.comment().isBlank()) {
                comment = Component.literal("");
            } else {
                comment = Component.translatable(key + ".description");
            }
            Class<?> type = field.getType();

            try {
                AbstractWidget widget = null;

                if (type == boolean.class) {
                    widget = OptionInstance.createBoolean(key,
                            OptionInstance.cachedConstantTooltip(comment),
                            field.getBoolean(QMMConfigManager.CONFIG),
                            v -> set(field, v)).createButton(options);

                } else if (type == int.class && field.isAnnotationPresent(RangedInt.class)
                        && field.getAnnotation(RangedInt.class).min() != Integer.MIN_VALUE
                        && field.getAnnotation(RangedInt.class).max() != Integer.MAX_VALUE) {
                    RangedInt r = field.getAnnotation(RangedInt.class);
                    OptionInstance.TooltipSupplier<Integer> tip = OptionInstance.cachedConstantTooltip(comment);
                    widget = new OptionInstance<Integer>(key, tip,
                            (caption, v) -> Options.genericValueLabel(caption, Component.literal(String.valueOf(v))),
                            new OptionInstance.IntRange(r.min(), r.max()),
                            field.getInt(QMMConfigManager.CONFIG),
                            v -> set(field, v)).createButton(options);

                } else if (type == long.class && field.isAnnotationPresent(RangedLong.class)
                        && field.getAnnotation(RangedLong.class).min() != Long.MIN_VALUE
                        && field.getAnnotation(RangedLong.class).max() != Long.MAX_VALUE) {
                    RangedLong r = field.getAnnotation(RangedLong.class);
                    double min = r.min(), span = (double) r.max() - r.min();
                    widget = unitSlider(options, key, comment,
                            d -> Math.max(r.min(), Math.min(r.max(), Math.round(min + d * span))),
                            (Long v) -> (v - min) / span,
                            String::valueOf,
                            field.getLong(QMMConfigManager.CONFIG), v -> set(field, v));

                } else if (type == double.class && field.isAnnotationPresent(RangedDouble.class)
                        && isFinite(field.getAnnotation(RangedDouble.class).min(), field.getAnnotation(RangedDouble.class).max())) {
                    RangedDouble r = field.getAnnotation(RangedDouble.class);
                    double span = r.max() - r.min();
                    widget = unitSlider(options, key, comment,
                            d -> r.min() + d * span,
                            (Double v) -> (v - r.min()) / span,
                            v -> String.format("%.2f", v),
                            field.getDouble(QMMConfigManager.CONFIG), v -> set(field, v));

                } else if (type == float.class && field.isAnnotationPresent(RangedFloat.class)
                        && isFinite(field.getAnnotation(RangedFloat.class).min(), field.getAnnotation(RangedFloat.class).max())) {
                    RangedFloat r = field.getAnnotation(RangedFloat.class);
                    double span = (double) r.max() - r.min();
                    widget = unitSlider(options, key, comment,
                            d -> (float) (r.min() + d * span),
                            (Float v) -> (v - r.min()) / span,
                            v -> String.format("%.2f", v),
                            field.getFloat(QMMConfigManager.CONFIG), v -> set(field, v));

                } else if (type.isEnum() && StringRepresentable.class.isAssignableFrom(type)) {
                    widget = enumButton(options, key, comment, field);

                } else if (type == int.class || type == long.class || type == float.class
                        || type == double.class || type == String.class) {
                    widget = textBox(field, name, comment);
                }

                if (widget != null) widgets.add(widget);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return widgets;
    }

    private static Component translatableFromEnum(StringRepresentable enumm) {
        return QSMultiblockMiner.translatable("options.enum", enumm.getClass().getSimpleName() + '.' + enumm.getSerializedName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static AbstractWidget enumButton(Options options, String key, Component comment, Field field) throws IllegalAccessException {
        return enumButtonTyped(options, key, comment, field, (Class) field.getType());
    }

    private static <E extends Enum<E> & StringRepresentable> AbstractWidget enumButtonTyped(
            Options options, String key, Component comment, Field field, Class<E> type) throws IllegalAccessException {
        E[] constants = type.getEnumConstants();
        @SuppressWarnings("unchecked")
        E initial = (E) field.get(QMMConfigManager.CONFIG);
        OptionInstance.TooltipSupplier<@NonNull E> tip = OptionInstance.cachedConstantTooltip(comment);

        return new OptionInstance<>(key, tip,
                (caption, v) -> translatableFromEnum(v),
                new OptionInstance.Enum<>(List.of(constants), StringRepresentable.fromEnum(() -> constants)),
                initial,
                v -> set(field, v)).createButton(options);
    }

    private static boolean isFinite(double min, double max) {
        return min > -Double.MAX_VALUE && max < Double.MAX_VALUE;
    }

    private static <T> AbstractWidget unitSlider(Options options, String key, Component comment,
                                                 DoubleFunction<T> to, ToDoubleFunction<T> from,
                                                 Function<T, String> format, T initial, Consumer<T> onChange) {
        OptionInstance.TooltipSupplier<T> tip = OptionInstance.cachedConstantTooltip(comment);
        return new OptionInstance<T>(key, tip,
                (caption, v) -> Options.genericValueLabel(caption, Component.literal(format.apply(v))),
                OptionInstance.UnitDouble.INSTANCE.xmap(to, from),
                initial, onChange).createButton(options);
    }

    private static AbstractWidget textBox(Field field, Component name, Component comment) throws IllegalAccessException {
        EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 150, 20, name);
        box.setMaxLength(256);
        box.setHint(name);
        box.setTooltip(Tooltip.create(name.copy().append("\n").append(comment)));
        box.setValue(String.valueOf(field.get(QMMConfigManager.CONFIG)));
        box.setResponder(text -> {
            try {
                set(field, parse(field, text));
                box.setTextColor(0xFFE0E0E0);
            } catch (Exception e) {
                box.setTextColor(0xFFFF5555);
            }
        });
        box.setTooltip(Tooltip.create(comment == null ? name : name.copy().append("\n").append(comment)));
        return box;
    }

    private static Object parse(Field field, String text) {
        Class<?> type = field.getType();
        if (type == String.class) {
            Regex regex = field.getAnnotation(Regex.class);
            if (regex != null && !text.matches(regex.value())) throw new IllegalArgumentException();
            return text;
        }
        text = text.trim();
        if (type == int.class) {
            int v = Integer.parseInt(text);
            RangedInt r = field.getAnnotation(RangedInt.class);
            if (r != null && (v < r.min() || v > r.max())) throw new IllegalArgumentException();
            return v;
        } else if (type == long.class) {
            long v = Long.parseLong(text);
            RangedLong r = field.getAnnotation(RangedLong.class);
            if (r != null && (v < r.min() || v > r.max())) throw new IllegalArgumentException();
            return v;
        } else if (type == float.class) {
            float v = Float.parseFloat(text);
            RangedFloat r = field.getAnnotation(RangedFloat.class);
            if (r != null && (v < r.min() || v > r.max())) throw new IllegalArgumentException();
            return v;
        } else {
            double v = Double.parseDouble(text);
            RangedDouble r = field.getAnnotation(RangedDouble.class);
            if (r != null && (v < r.min() || v > r.max())) throw new IllegalArgumentException();
            return v;
        }
    }

    private static void set(Field field, Object value) {
        try {
            field.set(QMMConfigManager.CONFIG, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
