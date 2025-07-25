package com.server.effects.config.serializers;

import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ninja.leaping.configurate.objectmapping.serialize.ScalarSerializer;

import java.util.function.Predicate;

public class ComponentTypeSerializer extends ScalarSerializer<Component> {


    public ComponentTypeSerializer() {
        super(Component.class);
    }

    @Override
    public Component deserialize(TypeToken<?> type, Object obj) {
        if (obj instanceof String str) {
            Component component = Component.empty();
            String[] split = str.split("\\n");
            for (int i = 0; i < split.length; i++) {
                if (i == split.length - 1) {
                    component = component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(split[i].trim()));
                } else component = component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(split[i].trim())).appendNewline();
            }
            return component;
        } else {
            return null;
        }
    }

    @Override
    public Object serialize(Component item, Predicate<Class<?>> typeSupported) {
        if (item == null) return null;
        return LegacyComponentSerializer.legacyAmpersand().serialize(item);
    }
}

