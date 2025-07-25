package com.server.effects.config.definitions.display;

import com.server.effects.config.definitions.ItemDefinition;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import ninja.leaping.configurate.objectmapping.Setting;

import java.util.Optional;

public class DisplayDefinition {
    //location = "86.5,88,241.10"
    //type = "BLOCK_DISPLAY"
    //block = "DIAMOND_BLOCK"
//    item = "DIAMOND_BLOCK"
//    displayType = "THIRD_PERSON_LEFT_HAND"
//    text = ""
    //size = 20
    //rotationLeft = 180
    //rotationRight = 20
    //billboard = "FIXED"

    @Setting("location")
    private Pos location;

    @Setting("type")
    private String displayType;

    @Setting("size")
    private Integer size;

    @Setting("rotationLeft")
    private float[] rotationLeft;

    @Setting("rotationRight")
    private float[] rotationRight;

    @Setting("billboard")
    private Byte billboardConstraints;


    // Optionals

    @Setting("block")
    private Block block;

    @Setting("item")
    private ItemDefinition item;

    @Setting("displayType")
    private Byte itemDisplayType;

    @Setting("text")
    private Component text;

    @Setting("image")
    private String image;

    public Pos getLocation() {
        return location;
    }

    public void setLocation(Pos location) {
        this.location = location;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public float[] getRotationLeft() {
        return rotationLeft;
    }

    public void setRotationLeft(float[] rotationLeft) {
        this.rotationLeft = rotationLeft;
    }

    public float[] getRotationRight() {
        return rotationRight;
    }

    public void setRotationRight(float[] rotationRight) {
        this.rotationRight = rotationRight;
    }

    public Byte getBillboardConstraints() {
        return billboardConstraints;
    }

    public void setBillboardConstraints(Byte billboardConstraints) {
        this.billboardConstraints = billboardConstraints;
    }

    public Optional<Block> getBlock() {
        return Optional.ofNullable(block);
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Optional<ItemDefinition> getItem() {
        return Optional.ofNullable(item);
    }

    public void setItem(ItemDefinition item) {
        this.item = item;
    }

    public Optional<Byte> getItemDisplayType() {
        return Optional.ofNullable(itemDisplayType);
    }

    public void setItemDisplayType(Byte itemDisplayType) {
        this.itemDisplayType = itemDisplayType;
    }

    public Optional<Component> getText() {
        return Optional.ofNullable(text);
    }

    public void setText(Component text) {
        this.text = text;
    }

    public Optional<String> getImage() {
        return Optional.ofNullable(image);
    }

    public void setText(String image) {
        this.image = image;
    }
}
