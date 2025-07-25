package com.server.effects.displays.types;

import com.server.effects.displays.Display;
import com.server.effects.displays.images.Image2TextDisplayRenderer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;

import java.nio.file.Path;
import java.util.UUID;

public class ImageDisplay extends Display {

    private Path image;

    public ImageDisplay(Pos location, Path image) {
        super(EntityType.TEXT_DISPLAY, UUID.randomUUID(), location, 0, new float[]{}, new float[]{}, (byte) 0);
        this.image = image;
    }

    public Path getImage() {
        return image;
    }

    public void setText(Path text) {
        this.image = text;
    }

    @Override
    public void spawn(Integer id, Player player) {
        Image2TextDisplayRenderer.renderImage(image, player.getInstance(), getLocation());
//        SpawnEntityPacket nameTag = new SpawnEntityPacket(
//                id,
//                UUID.randomUUID(),
//                EntityType.TEXT_DISPLAY.id(),
//                getLocation(),
//                getLocation().yaw(),
//                0,
//                (short) 0,
//                (short) 0,
//                (short) 0);
//
//        java.util.Map<Integer, Metadata.Entry<?>> nameTagMetaData = new HashMap<>();
//        nameTagMetaData.put(12, Metadata.Vector3(new Vec(getSize(), getSize(), getSize())));
//        nameTagMetaData.put(13, Metadata.Quaternion(getRotationLeft()));
//        nameTagMetaData.put(14, Metadata.Quaternion(getRotationRight()));
//        nameTagMetaData.put(15, Metadata.Byte(getBillboard()));
//        nameTagMetaData.put(24, Metadata.VarInt(200));
//        nameTagMetaData.put(25, Metadata.VarInt(1073741824));
//        nameTagMetaData.put(27, Metadata.Byte((byte) 0b00000001));
//        player.sendPacket(nameTag);
//        player.sendPacket(new EntityMetaDataPacket(id, nameTagMetaData));
    }
}
