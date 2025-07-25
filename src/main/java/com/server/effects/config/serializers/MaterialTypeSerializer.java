package com.server.effects.config.serializers;

import com.google.common.reflect.TypeToken;
import net.minestom.server.item.Material;
import ninja.leaping.configurate.objectmapping.serialize.ScalarSerializer;

import java.util.function.Predicate;

public class MaterialTypeSerializer extends ScalarSerializer<Material> {


    public MaterialTypeSerializer() {
        super(Material.class);
    }

    @Override
    public Material deserialize(TypeToken<?> type, Object obj) {
        if (obj instanceof String str) {
            return Material.fromKey(str.toLowerCase());
        } else {
            return null;
        }
    }

    @Override
    public Object serialize(Material item, Predicate<Class<?>> typeSupported) {
        if (item == null) return null;
        return item.key().asString();
    }
}

