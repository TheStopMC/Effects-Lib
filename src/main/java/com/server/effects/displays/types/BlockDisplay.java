package com.server.effects.displays.types;

import com.server.effects.displays.Display;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

import java.util.HashMap;
import java.util.UUID;

public class BlockDisplay extends Display {

    private Block block;

    public BlockDisplay(Pos location, Integer size, float[] rotationLeft, float[] rotationRight, byte billboard, Block block) {
        super(EntityType.BLOCK_DISPLAY, UUID.randomUUID(), location, size, rotationLeft, rotationRight, billboard);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public void spawn(Integer id, Player player) {
        SpawnEntityPacket nameTag = new SpawnEntityPacket(
                id,
                UUID.randomUUID(),
                EntityType.BLOCK_DISPLAY.id(),
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
        nameTagMetaData.put(23, Metadata.BlockState(getBlock()));
        player.sendPacket(nameTag);
        player.sendPacket(new EntityMetaDataPacket(id, nameTagMetaData));
    }
}
