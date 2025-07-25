package com.server.effects.config.serializers;

import com.google.common.reflect.TypeToken;
import net.minestom.server.entity.EntityType;
import ninja.leaping.configurate.objectmapping.serialize.ScalarSerializer;

import java.util.function.Predicate;

public class EntityTypeSerializer extends ScalarSerializer<EntityType> {


    public EntityTypeSerializer() {
        super(EntityType.class);
    }

    @Override
    public EntityType deserialize(TypeToken<?> type, Object obj) {
        if (obj instanceof String str) {
            return EntityType.fromKey(str.toLowerCase());
        } else {
            return null;
        }
    }

    @Override
    public Object serialize(EntityType item, Predicate<Class<?>> typeSupported) {
        if (item == null) return null;
        return item.key().asString();
    }
}

