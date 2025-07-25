package com.server.effects.config.serializers;

import com.google.common.reflect.TypeToken;
import net.minestom.server.instance.block.Block;
import ninja.leaping.configurate.objectmapping.serialize.ScalarSerializer;

import java.util.function.Predicate;

public class BlockTypeSerializer extends ScalarSerializer<Block> {


    public BlockTypeSerializer() {
        super(Block.class);
    }

    @Override
    public Block deserialize(TypeToken<?> type, Object obj) {
        if (obj instanceof String str) {
            return Block.fromKey(str.toLowerCase());
        } else {
            return null;
        }
    }

    @Override
    public Object serialize(Block item, Predicate<Class<?>> typeSupported) {
        if (item == null) return null;
        return item.key().asString();
    }
}