package com.server.effects.npc;

import com.server.effects.config.definitions.ItemDefinition;
import com.server.effects.config.definitions.npcs.NPCDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class NPC extends LivingEntity {

    private String mapId;

    private Component name;

    private Pos location;

    private String messageSendOrder;

    private List<Component> messages;

    private List<TradeListPacket.Trade> trades;

    private String skinTexture;

    private String skinSignature;

    public NPC(@NotNull EntityType entityType, Component name, String mapId, Pos location, String messageSendOrder, List<Component> messages, List<TradeListPacket.Trade> trades, String skinTexture, String skinSignature) {
        super(entityType);
        this.mapId = mapId;
        this.location = location;
        this.name = name;
        this.messageSendOrder = messageSendOrder;
        this.messages = messages;
        this.trades = trades;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
    }

    public static class Builder {
        private EntityType entityType = EntityType.PLAYER;
        private Component name;
        private Pos location;
        private String mapId;
        private String messageSendOrder;
        private List<Component> messages;
        private List<TradeListPacket.Trade> trades;
        private String skinTexture;
        private String skinSignature;

        public Builder entityType(@NotNull EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder location(Pos location) {
            this.location = location;
            return this;
        }

        public Builder messageSendOrder(String messageSendOrder) {
            this.messageSendOrder = messageSendOrder;
            return this;
        }

        public Builder messages(List<Component> messages) {
            this.messages = messages;
            return this;
        }

        public Builder trades(List<TradeListPacket.Trade> trades) {
            this.trades = trades;
            return this;
        }

        public Builder skinTexture(String skinTexture) {
            this.skinTexture = skinTexture;
            return this;
        }

        public Builder skinSignature(String skinSignature) {
            this.skinSignature = skinSignature;
            return this;
        }

        public Builder mapId(String mapId) {
            this.mapId = mapId;
            return this;
        }

        public NPC build() {
            return new NPC(entityType, name, mapId, location, messageSendOrder, messages, trades, skinTexture, skinSignature);
        }
    }

    public Component getName() {
        return name;
    }

    public Pos getLocation() {
        return location;
    }

    public String getMapId() {
        return mapId;
    }

    public String getMessageSendOrder() {
        return messageSendOrder;
    }

    public Optional<List<Component>> getMessages() {
        return Optional.ofNullable(messages.isEmpty() ? null : messages);
    }

    public Optional<List<TradeListPacket.Trade>> getTrades() {
        return Optional.ofNullable(trades.isEmpty() ? null : trades);
    }

    public Optional<String> getSkinTexture() {
        return Optional.ofNullable(skinTexture);
    }

    public Optional<String> getSkinSignature() {
        return Optional.ofNullable(skinSignature);
    }


    public static NPC fromDefinition(NPCDefinition definition) {
        Builder builder = new Builder();
        builder.entityType(definition.getType());
        builder.location(definition.getLocation());
        builder.name(definition.getName());
        builder.mapId(definition.getInstance());
        builder.messageSendOrder(definition.getMessageSendOrder());
        definition.getMessages().ifPresent(builder::messages);
        definition.getSkinTexture().ifPresent(builder::skinTexture);
        definition.getSkinSignature().ifPresent(builder::skinSignature);

        List<TradeListPacket.Trade> trades = new ArrayList<>();
        definition.getTrades().ifPresent(tradeDefinitionList -> tradeDefinitionList.forEach(trade -> {

            ItemDefinition input1Definition = trade.getInput1();
            ItemStack.Builder input1 = ItemStack.builder(input1Definition.getMaterial());
            input1Definition.getAmount().ifPresent(input1::amount);
            input1Definition.getName().ifPresent(input1::customName);
            input1Definition.getLore().ifPresent(input1::lore);
//            input1Definition.getCustomModelData().ifPresent(input1::customModelData);
            trade.getInput1().getEnchantments().ifPresent(enchantments -> {
                Map<RegistryKey<Enchantment>, Integer> enchantmentsList = new HashMap<>();
                enchantments.forEach(enchantment -> {
                    enchantmentsList.put(RegistryKey.unsafeOf(enchantment.getId()),  enchantment.getLevel());
                });
                input1.set(DataComponents.ENCHANTMENTS, new EnchantmentList(enchantmentsList));
            });

            ItemStack.Builder input2 = ItemStack.builder(Material.AIR);
            trade.getInput2().ifPresent(input2Definition -> {
                input2.material(input2Definition.getMaterial());
                input2Definition.getAmount().ifPresent(input2::amount);
                input2Definition.getName().ifPresent(input2::customName);
                input2Definition.getLore().ifPresent(input2::lore);
//            input2Definition.getCustomModelData().ifPresent(input1::customModelData);
                input2Definition.getEnchantments().ifPresent(enchantments -> {
                    Map<RegistryKey<Enchantment>, Integer> enchantmentsList = new HashMap<>();
                    enchantments.forEach(enchantment -> {
                        enchantmentsList.put(RegistryKey.unsafeOf(enchantment.getId()),  enchantment.getLevel());
                    });
                    input2.set(DataComponents.ENCHANTMENTS, new EnchantmentList(enchantmentsList));
                });
            });

            ItemDefinition outputDefinition = trade.getOutput();
            ItemStack.Builder output = ItemStack.builder(outputDefinition.getMaterial());
            outputDefinition.getAmount().ifPresent(output::amount);
            outputDefinition.getName().ifPresent(output::customName);
            outputDefinition.getLore().ifPresent(output::lore);
//            outputDefinition.getCustomModelData().ifPresent(input1::customModelData);
            outputDefinition.getEnchantments().ifPresent(enchantments -> {
                Map<RegistryKey<Enchantment>, Integer> enchantmentsList = new HashMap<>();
                enchantments.forEach(enchantment -> {
                    enchantmentsList.put(RegistryKey.unsafeOf(enchantment.getId()),  enchantment.getLevel());
                });
                output.set(DataComponents.ENCHANTMENTS, new EnchantmentList(enchantmentsList));
            });

            trades.add(new TradeListPacket.Trade(
                    input1.build(),
                    output.build(),
                    input2.build().isAir() ? null : input2.build(),
                    false,
                    0,
                    100000,
                    0,
                    0,
                    0,
                    0
            ));
        }));
        builder.trades(trades);
        return builder.build();
    }

    public void spawn(Player player, Integer id) {

        SpawnEntityPacket spawnPacket = new SpawnEntityPacket(
                id,
                getUuid(),
                getEntityType().id(),
                getLocation(),
                getLocation().yaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0);

        EntityRotationPacket entityRotationPacket = new EntityRotationPacket(
                id,
                getLocation().yaw(),
                getLocation().pitch(),
                true
        );
        Map<Integer, Metadata.Entry<?>> metaData = new HashMap<>();
        if (getEntityType() == EntityType.PLAYER) {
            var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
            if (getSkinSignature().isPresent() && getSkinTexture().isPresent()) {
                if (getSkinSignature().get().equalsIgnoreCase("viewer") ||
                        getSkinTexture().get().equalsIgnoreCase("viewer")) {
                    properties.add(new PlayerInfoUpdatePacket.Property("textures", player.getSkin().textures(), player.getSkin().signature()));
                } else properties.add(new PlayerInfoUpdatePacket.Property("textures", getSkinTexture().get(), getSkinSignature().get()));
            }
            String fakeUsername = "NPC-" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            PlayerInfoUpdatePacket addPlayerInfo = new PlayerInfoUpdatePacket(
                    PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                    new PlayerInfoUpdatePacket.Entry(
                            getUuid(),
                            fakeUsername,
                            properties,
                            false,
                            0,
                            GameMode.SURVIVAL,
                            getName(),
                            null,
                            0));
            player.sendPacket(addPlayerInfo);
            metaData.put(17, Metadata.Byte((byte) 127));
            player.sendPacket(new TeamsPacket(fakeUsername, new TeamsPacket.CreateTeamAction(getName(), (byte) 0, TeamsPacket.NameTagVisibility.NEVER, TeamsPacket.CollisionRule.ALWAYS, NamedTextColor.WHITE, Component.empty(), Component.empty(), List.of(fakeUsername))));
        }

        metaData.putAll(getMetadataPacket().entries());
        player.sendPacket(spawnPacket);
        player.sendPacket(new EntityMetaDataPacket(id, metaData));
        player.sendPacket(entityRotationPacket);

        SpawnEntityPacket nameTag = new SpawnEntityPacket(
                -id,
                UUID.randomUUID(),
                EntityType.TEXT_DISPLAY.id(),
                getLocation().add(0,getEyeHeight() + .5,0),
                getLocation().yaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0);

        Map<Integer, Metadata.Entry<?>> nameTagMetaData = new HashMap<>();
        nameTagMetaData.put(15, Metadata.Byte((byte) 3));
        nameTagMetaData.put(23, Metadata.Chat(getName()));
        nameTagMetaData.put(24, Metadata.VarInt(200));
        nameTagMetaData.put(25, Metadata.VarInt(1073741824));
        nameTagMetaData.put(27, Metadata.Byte((byte) 0b00000001));
        player.sendPacket(nameTag);
        player.sendPacket(new EntityMetaDataPacket(-id, nameTagMetaData));
    }
}