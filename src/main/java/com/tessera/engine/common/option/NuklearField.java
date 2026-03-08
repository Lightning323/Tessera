package com.tessera.engine.common.option;

import com.tessera.window.nuklear.components.NumberBox;
import com.tessera.window.nuklear.components.TextBox;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.nuklear.Nuklear.*;

public class NuklearField {
    Field field;
    String key;
    Object value, fieldTarget;

    TextBox textBox;
    NumberBox numberBox;
    private final Consumer<Object> valueChanged;

    private boolean isNumber() {
        return isType(BoundedInt.class) ||
                isType(BoundedFloat.class) ||
                isType(int.class) || isType(float.class) || isType(double.class) ||
                isType(long.class) || isType(short.class) || isType(byte.class) ||
                isType(Integer.class) || isType(Float.class) || isType(Double.class) ||
                isType(Long.class) || isType(Short.class) || isType(Byte.class);
    }

    private boolean isBoolean() {
        return isType(boolean.class) || isType(Boolean.class);
    }

    private boolean isType(Class<?> type) {
        return getType().equals(type);
    }

    private Class getType() {
        if (field != null) return field.getType();
        else return value.getClass();
    }

    public void layout(NkContext ctx, MemoryStack stack) {
        if (isBoolean()) {
            nk_layout_row_dynamic(ctx, 35, 1);
            boolean b = (boolean) value;
            ByteBuffer active = stack.malloc(1);
            active.put(0, b ? (byte) 0 : 1); //For some reason the boolean needs to be flipped
            if (nk_checkbox_label(ctx, " " + key, active)) {
                setValue(!b);
            }
        } else {
            nk_layout_row_dynamic(ctx, 10, 1);
            nk_label(ctx, key, NK_TEXT_ALIGN_LEFT);
            nk_layout_row_dynamic(ctx, 35, 1);
            if (isNumber()) {
                numberBox.render(ctx);
            } else if (isType(String.class)) {
                textBox.render(ctx);
            }
        }
    }

    public void layout2(NkContext ctx, MemoryStack stack) {
        nk_layout_row_dynamic(ctx, 30, 2);
        nk_label(ctx, key, NK_TEXT_ALIGN_LEFT);
        if (isNumber()) {
            numberBox.render(ctx);
        } else if (isType(String.class)) {
            textBox.render(ctx);
        } else if (isBoolean()) {
            boolean b = (boolean) value;
            if (nk_button_label(ctx, b ? "ENABLED" : "DISABLED")) {
                setValue(!b);
            }
        }
    }


    public String toString() {
        return ("Field: " + key + ": " + value + " \t" + getType());
    }

    public NuklearField(Field field, Object target, Consumer<Object> valueChanged) throws IllegalAccessException {
        this.field = field;
        this.valueChanged = valueChanged;
        this.fieldTarget = target;
        this.key = prettyFormat(field.getName());
        this.value = field.get(target);
        init();
    }

    public NuklearField(String key, Object value, Consumer<Object> valueChanged) {
        this.valueChanged = valueChanged;
        this.value = value;
        this.key = key;
        init();
    }

    private void init() {
        if (isType(BoundedInt.class)) {
            numberBox = new NumberBox(50);

            BoundedInt bound = ((BoundedInt) value);
            numberBox.setValueAsNumber(bound.value);
            numberBox.setMinValue(bound.min);
            numberBox.setMaxValue(bound.max);

            numberBox.setOnChangeEvent(() -> {
                BoundedInt b = ((BoundedInt) value);
                b.value = (int) numberBox.getValueAsNumber();
                value = b;
                setValue(b);
            });
        } else if (isType(BoundedFloat.class)) {
            numberBox = new NumberBox(50);
            BoundedFloat bound = ((BoundedFloat) value);
            numberBox.setValueAsNumber(bound.value);
            numberBox.setMinValue(bound.min);
            numberBox.setMaxValue(bound.max);

            numberBox.setOnChangeEvent(() -> {
                BoundedFloat b = ((BoundedFloat) value);
                b.value = (float) numberBox.getValueAsNumber();
                value = b;
                setValue(b);
            });
        } else if (isType(long.class) || isType(Long.class)) {
            numberBox = new NumberBox(50);
            numberBox.setValueAsNumber((long) value);
            numberBox.setOnChangeEvent(() -> {
                value = (long) numberBox.getValueAsNumber();
                setValue((long) value);
            });
        } else if (isType(int.class) || isType(Integer.class)) {
            numberBox = new NumberBox(50);
            numberBox.setValueAsNumber((int) value);
            numberBox.setOnChangeEvent(() -> {
                value = (int) numberBox.getValueAsNumber();
                setValue((int) value);
            });
        } else if (isType(float.class) || isType(Float.class)) {
            numberBox = new NumberBox(50);
            numberBox.setValueAsNumber((float) value);
            numberBox.setOnChangeEvent(() -> {
                value = (float) numberBox.getValueAsNumber();
                setValue((float) value);
            });
        } else if (isType(String.class)) {
            textBox = new TextBox(50);
            textBox.setValueAsString((String) value);
            textBox.setOnChangeEvent(() -> {
                value = textBox.getValueAsString();
                setValue(value);
            });
        } else if (isType(boolean.class) || isType(Boolean.class)) {
            value = (boolean) value;
        } else throw new IllegalArgumentException("Unsupported typeReference for NuklearField: " + getType());
    }

    private String prettyFormat(String key) {
        //Format the key from camelCase to normal text (every uppercae leter should be preceded by a space)
        key = key.replaceAll("([a-z])([A-Z])", "$1 $2");
        key = key.replaceAll("_", ", ");
        //capitalize the first letter of every word
        String[] words = key.split(" ");
        key = "";
        for (String word : words) {
            key += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        return key;
    }

    public void setValue(Object value) {
        try {
            if (field != null) field.set(fieldTarget, value);
            this.value = value;
            System.out.println("Set " + key + " to " + value);
            if (valueChanged != null) valueChanged.accept(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
