package com.server.effects.config.definitions.npcs;

import com.server.effects.config.definitions.ItemDefinition;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public class TradeDefinition {

    @Setting("input1")
    private ItemDefinition input1;

    @Setting("input2")
    private ItemDefinition input2;

    @Setting("output")
    private ItemDefinition output;

    public TradeDefinition() {}

    public ItemDefinition getInput1() {
        return input1;
    }
    public void setInput1(ItemDefinition input1) {
        this.input1 = input1;
    }

    public Optional<ItemDefinition> getInput2() {
        return Optional.ofNullable(input2);
    }
    public void setInput2(ItemDefinition input2) {
        this.input2 = input2;
    }

    public ItemDefinition getOutput() {
        return output;
    }
    public void setOutput(ItemDefinition output) {
        this.output = output;
    }
}
