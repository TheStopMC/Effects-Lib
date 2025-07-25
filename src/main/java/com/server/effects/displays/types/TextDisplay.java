package com.server.effects.displays.types;

import com.server.effects.displays.Display;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

import java.util.HashMap;
import java.util.UUID;

public class TextDisplay extends Display {

    private Component text;

    public TextDisplay(Pos location, Integer size, float[] rotationLeft, float[] rotationRight, byte billboard, Component text) {
        super(EntityType.TEXT_DISPLAY, UUID.randomUUID(), location, size, rotationLeft, rotationRight, billboard);
        this.text = text;
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    @Override
    public void spawn(Integer id, Player player) {
        SpawnEntityPacket nameTag = new SpawnEntityPacket(
                id,
                UUID.randomUUID(),
                EntityType.TEXT_DISPLAY.id(),
                getLocation(),
                getLocation().yaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0);

        java.util.Map<Integer, Metadata.Entry<?>> nameTagMetaData = new HashMap<>();
        nameTagMetaData.put(12, Metadata.Vector3(new Vec(getSize(), getSize(), getSize())));
        nameTagMetaData.put(13, Metadata.Quaternion(getRotationLeft()));
        nameTagMetaData.put(14, Metadata.Quaternion(getRotationRight()));
        nameTagMetaData.put(15, Metadata.Byte(getBillboard()));
        nameTagMetaData.put(23, Metadata.Chat(getText()));
        nameTagMetaData.put(24, Metadata.VarInt(200));
        nameTagMetaData.put(25, Metadata.VarInt(1073741824));
        nameTagMetaData.put(27, Metadata.Byte((byte) 0b00000001));
        player.sendPacket(nameTag);
        player.sendPacket(new EntityMetaDataPacket(id, nameTagMetaData));
    }
}
