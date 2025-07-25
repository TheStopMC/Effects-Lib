package com.server.effects.config.definitions;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Optional;

@ConfigSerializable
public class ItemDefinition {

    @Setting("material")
    private Material material;

    @Setting("amount")
    private Integer amount = 1;

    @Setting("name")
    private Component name;

    @Setting("lore")
    private List<Component> lore;

    @Setting("enchantments")
    private List<EnchantmentDefinition> enchantments;

    @Setting("custom-model-data")
    private Integer customModelData;

    public ItemDefinition() {}

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Optional<Integer> getAmount() {
        return Optional.ofNullable(amount);
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Optional<Component> getName() {
        return Optional.ofNullable(name);
    }

    public void setName(Component name) {
        this.name = name;
    }

    public Optional<List<Component>> getLore() {
        return Optional.ofNullable(lore);
    }

    public void setLore(List<Component> lore) {
        this.lore = lore;
    }

    public Optional<List<EnchantmentDefinition>> getEnchantments() {
        return Optional.ofNullable(enchantments);
    }

    public void setEnchantments(List<EnchantmentDefinition> enchantments) {
        this.enchantments = enchantments;
    }

    public Optional<Integer> getCustomModelData() {
        return Optional.ofNullable(customModelData);
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
    }


    @ConfigSerializable
    public static class EnchantmentDefinition {

        @Setting("id")
        private String id;

        @Setting("level")
        private int level;

        public EnchantmentDefinition() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

}

