package com.server.effects.config.definitions.npcs;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Optional;

@ConfigSerializable
public class NPCDefinition {

    @Setting("instance")
    private String instance;

    @Setting("location")
    private Pos location;

    @Setting("type")
    private EntityType type;

    @Setting("name")
    private Component name;

    @Setting("message-send-order")
    private String messageSendOrder;

    @Setting("messages")
    private List<Component> messages;

    @Setting("skinTexture")
    private String skinTexture;

    @Setting("skinSignature")
    private String skinSignature;

    @Setting("trades")
    private List<TradeDefinition> trades;

    public NPCDefinition() {}

    public Pos getLocation() {
        return location;
    }
    public void setLocation(Pos location) {
        this.location = location;
    }

    public EntityType getType() {
        return type;
    }
    public void setType(EntityType type) {
        this.type = type;
    }

    public Component getName() {
        return name;
    }
    public void setName(Component name) {
        this.name = name;
    }

    public String getMessageSendOrder() {
        return messageSendOrder;
    }

    public void setMessageSendOrder(String messageSendOrder) {
        this.messageSendOrder = messageSendOrder;
    }

    public Optional<List<Component>> getMessages() {
        return Optional.ofNullable(messages);
    }

    public void setMessages(List<Component> messages) {
        this.messages = messages;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Optional<String> getSkinTexture() {
        return Optional.ofNullable(skinTexture);
    }

    public void setSkinTexture(String skinTexture) {
        this.skinTexture = skinTexture;
    }

    public Optional<String> getSkinSignature() {
        return Optional.ofNullable(skinSignature);
    }

    public void setSkinSignature(String skinSignature) {
        this.skinSignature = skinSignature;
    }

    public Optional<List<TradeDefinition>> getTrades() {
        return Optional.ofNullable(trades);
    }
    public void setTrades(List<TradeDefinition> trades) {
        this.trades = trades;
    }
}
