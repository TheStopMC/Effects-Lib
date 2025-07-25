package com.server.effects.config.serializers;

import com.google.common.reflect.TypeToken;
import net.minestom.server.coordinate.Pos;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ScalarSerializer;

import java.util.function.Predicate;

public class PosTypeSerializer extends ScalarSerializer<Pos> {


    public PosTypeSerializer() {
        super(Pos.class);
    }

    @Override
    public Pos deserialize(TypeToken<?> type, Object obj) throws ObjectMappingException {
        if (obj instanceof String str) {
            String[] parts = str.split(",");
            if (parts.length != 5) {
                throw new ObjectMappingException("Expected Vec format 'x,y,z,yaw,pitch' but got: " + str);
            }

            try {
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                double z = Double.parseDouble(parts[2].trim());
                float yaw = Float.parseFloat(parts[3].trim());
                float pitch = Float.parseFloat(parts[4].trim());
                return new Pos(x, y, z, yaw, pitch);
            } catch (NumberFormatException e) {
                throw new ObjectMappingException("Invalid Vec component in: " + str, e);
            }
        }

        throw new ObjectMappingException("Expected string for Vec3i, got: " + obj.getClass().getName());
    }

    @Override
    public Object serialize(Pos item, Predicate<Class<?>> typeSupported) {
        if (item == null) return null;
        return item.x() + "," + item.y() + "," + item.z() + "," + item.yaw() + "," + item.pitch();
    }
}

