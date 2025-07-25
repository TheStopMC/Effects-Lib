package com.server.effects.displays.types;

import com.server.effects.displays.Display;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;

import java.util.HashMap;
import java.util.UUID;

public class ItemDisplay extends Display {

    private ItemStack itemStack;
    private byte displayType;

    public ItemDisplay(Pos location, Integer size, float[] rotationLeft, float[] rotationRight, byte billboard, ItemStack itemStack, byte displayType) {
        super(EntityType.ITEM_DISPLAY, UUID.randomUUID(), location, size, rotationLeft, rotationRight, billboard);
        this.itemStack = itemStack;
        this.displayType = displayType;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public byte getDisplayType() {
        return displayType;
    }

    public void setDisplayType(byte displayType) {
        this.displayType = displayType;
    }

    @Override
    public void spawn(Integer id, Player player) {
        SpawnEntityPacket nameTag = new SpawnEntityPacket(
                id,
                UUID.randomUUID(),
                EntityType.ITEM_DISPLAY.id(),
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
        nameTagMetaData.put(23, Metadata.ItemStack(getItemStack()));
        nameTagMetaData.put(24, Metadata.Byte(getDisplayType()));
        player.sendPacket(nameTag);
        player.sendPacket(new EntityMetaDataPacket(id, nameTagMetaData));
    }
}
