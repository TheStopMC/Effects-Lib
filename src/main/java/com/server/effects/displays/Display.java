package com.server.effects.displays;

import com.server.effects.config.definitions.display.DisplayDefinition;
import com.server.effects.displays.types.BlockDisplay;
import com.server.effects.displays.types.ImageDisplay;
import com.server.effects.displays.types.ItemDisplay;
import com.server.effects.displays.types.TextDisplay;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public abstract class Display extends Entity {

    private Pos location;

    private Integer size;
    private float[] rotationLeft;
    private float[] rotationRight;
    private byte billboardConstraints;

    public Display(@NotNull EntityType entityType, @NotNull UUID uuid, Pos location, Integer size, float[] rotationLeft, float[] rotationRight, byte billboardConstraints) {
        super(entityType, uuid);
        this.location = location;
        this.size = size;
        this.rotationLeft = rotationLeft;
        this.rotationRight = rotationRight;
        this.billboardConstraints = billboardConstraints;
    }

    public Pos getLocation() {
        return location;
    }

    public void setLocation(Pos location) {
        this.location = location;
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

    public byte getBillboard() {
        return billboardConstraints;
    }

    public void setBillboard(byte billboard) {
        this.billboardConstraints = billboard;
    }

    public abstract void spawn(Integer id, Player player);


    public static Display fromDefinition(DisplayDefinition definition) {
        return switch (definition.getDisplayType().toUpperCase()) {
            case "ITEM" -> {
                ItemStack.Builder item = ItemStack.builder(Material.AIR);
                definition.getItem().ifPresent(itemDefinition -> {
                    item.material(itemDefinition.getMaterial());
                    itemDefinition.getAmount().ifPresent(item::amount);
                    itemDefinition.getName().ifPresent(item::customName);
                    itemDefinition.getLore().ifPresent(item::lore);
                    itemDefinition.getEnchantments().ifPresent(enchantments -> {
                        java.util.Map<RegistryKey<Enchantment>, Integer> enchantmentsList = new HashMap<>();
                        enchantments.forEach(enchantment -> {
                            enchantmentsList.put(RegistryKey.unsafeOf(enchantment.getId()),  enchantment.getLevel());
                        });
                        item.set(DataComponents.ENCHANTMENTS, new EnchantmentList(enchantmentsList));
                    });
                });
                yield new ItemDisplay(definition.getLocation(),
                        definition.getSize(),
                        definition.getRotationLeft(),
                        definition.getRotationRight(),
                        definition.getBillboardConstraints(),
                        item.build(),
                        definition.getItemDisplayType().get());
            }
            case "BLOCK" -> new BlockDisplay(definition.getLocation(),
                    definition.getSize(),
                    definition.getRotationLeft(),
                    definition.getRotationRight(),
                    definition.getBillboardConstraints(),
                    definition.getBlock().get());
            case "TEXT" -> new TextDisplay(definition.getLocation(),
                    definition.getSize(),
                    definition.getRotationLeft(),
                    definition.getRotationRight(),
                    definition.getBillboardConstraints(),
                    definition.getText().get());
            case "IMAGE" -> new ImageDisplay(definition.getLocation(),
                    Path.of(definition.getImage().get()));
            default -> throw new IllegalStateException("Unexpected value: " + definition.getDisplayType().toUpperCase());
        };
    }
}
