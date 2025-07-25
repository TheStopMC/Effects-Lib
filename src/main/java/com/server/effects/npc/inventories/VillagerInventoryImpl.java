package com.server.effects.npc.inventories;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.type.VillagerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientSelectTradePacket;
import net.minestom.server.network.packet.server.play.TradeListPacket;
import org.jetbrains.annotations.NotNull;

public class VillagerInventoryImpl extends VillagerInventory {

    static {
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSelectTradePacket.class, (packet, p) -> {
            if (!(p.getOpenInventory() instanceof VillagerInventoryImpl villagerInventory)) return;

            villagerInventory.selectedTradeSlot = packet.selectedSlot();
            TradeListPacket.Trade trade = villagerInventory.getTrades().get(villagerInventory.selectedTradeSlot);
            if (trade == null) return;

            PlayerInventory inv = p.getInventory();
            villagerInventory.update();
            inv.update();

            // Process both input items
            processTradeInput(inv, villagerInventory, 0, trade.inputItem1());
            if (trade.inputItem2() != null && !trade.inputItem2().material().equals(Material.AIR)) {
                processTradeInput(inv, villagerInventory, 1, trade.inputItem2());
            }

            villagerInventory.update();
            inv.update();
        });
    }

    public int selectedTradeSlot = 0;

    public VillagerInventoryImpl(@NotNull Component title) {
        super(title);
    }

    public VillagerInventoryImpl(@NotNull String title) {
        this(Component.text(title));
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        if (slot == 2) {
            handleLeftClick(player);
            return false;
        }
        return super.leftClick(player, slot);
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        if (slot == 2) {
            handleLeftClick(player);
            return false;
        }
        return super.rightClick(player, slot);
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot, int button) {
        if (slot == 2) {
            handleShiftClick(player);
            return false;
        }
        return super.shiftClick(player, slot, button);
    }

    private void handleLeftClick(Player player) {
        ItemStack inputSlot1 = getItemStack(0);
        ItemStack inputSlot2 = getItemStack(1);

        TradeListPacket.Trade selectedTrade = getTrades().get(selectedTradeSlot);

        if (selectedTrade != null
                && costAndStackMatch(selectedTrade.inputItem1(), inputSlot1)) {
            if (selectedTrade.inputItem2() != null) {
                if (costAndStackMatch(selectedTrade.inputItem2(), inputSlot2)) {
                    ItemStack cursor = player.getInventory().getCursorItem();
                    if (cursor.amount() + selectedTrade.result().amount() > cursor.maxStackSize()) {return;}
                    if (cursor.isAir()) {
                        player.getInventory().setCursorItem(selectedTrade.result().withAmount(selectedTrade.result().amount()));
                    } else if (cursor.isSimilar(selectedTrade.result())) {
                        player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() + selectedTrade.result().amount()));
                    } else {
                        return;
                    }
                    setItemStack(0, inputSlot1.consume(selectedTrade.inputItem1().amount()));
                    setItemStack(1, inputSlot2.consume(selectedTrade.inputItem2().amount()));
                }
            } else {
                ItemStack cursor = player.getInventory().getCursorItem();
                if (cursor.amount() + selectedTrade.result().amount() > cursor.maxStackSize()) {return;}
                if (cursor.isAir()) {
                    player.getInventory().setCursorItem(selectedTrade.result().withAmount(selectedTrade.result().amount()));
                } else if (cursor.isSimilar(selectedTrade.result())) {
                    player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() + selectedTrade.result().amount()));
                } else {
                    return;
                }
                setItemStack(0, inputSlot1.consume(selectedTrade.inputItem1().amount()));
            }
        }
    }

    private void handleShiftClick(Player player) {
        ItemStack inputSlot1 = getItemStack(0);
        ItemStack inputSlot2 = getItemStack(1);

        TradeListPacket.Trade selectedTrade = getTrades().get(selectedTradeSlot);

        if (selectedTrade != null
                && costAndStackMatch(selectedTrade.inputItem1(), inputSlot1)) {
            if (selectedTrade.inputItem2() != null) {
                if (costAndStackMatch(selectedTrade.inputItem2(), inputSlot2)) {

                    int amountPer1 = selectedTrade.inputItem1().amount();
                    int amountInSlot1 = inputSlot1.amount();

                    int timesGive1 = amountInSlot1 / amountPer1;



                    int amountPer2 = selectedTrade.inputItem2().amount();
                    int amountInSlot2 = inputSlot2.amount();

                    int timesGive2 = amountInSlot2 / amountPer2;

                    int actualTimesGiven = Math.min(timesGive1, timesGive2);

                    setItemStack(0, inputSlot1.consume(amountPer1 * actualTimesGiven));
                    setItemStack(1, inputSlot2.consume(amountPer2 * actualTimesGiven));

                    player.getInventory().addItemStack(selectedTrade.result().withAmount(selectedTrade.result().amount() * actualTimesGiven));
                }
            } else {
                int amountPer = selectedTrade.inputItem1().amount();
                int amountInSlot = inputSlot1.amount();

                int timesGive = amountInSlot / amountPer;

                player.getInventory().addItemStack(selectedTrade.result().withAmount(selectedTrade.result().amount() * timesGive));
                setItemStack(0, inputSlot1.consume(amountPer * timesGive));
            }
        }
    }

    private static boolean costAndStackMatch(TradeListPacket.ItemCost cost, ItemStack itemStack) {
        return cost.material().equals(itemStack.material())
                && itemStack.amount() >= cost.amount()
                && itemStack.componentPatch().equals(cost.components());
    }

    private static void processTradeInput(PlayerInventory inv, VillagerInventoryImpl villagerInventory, int inputSlotIndex, TradeListPacket.ItemCost required) {
        ItemStack currentInput = villagerInventory.getItemStack(inputSlotIndex);
        int maxStackSize = required.material().maxStackSize();
        int currentAmount = 0;

        // Keep matching input item if already there
        if (costAndStackMatch(required, currentInput)) {
            currentAmount = currentInput.amount();
        } else {
            // Wrong item? Return it
            if (!currentInput.isAir()) {
                inv.addItemStack(currentInput);
            }
            currentInput = ItemStack.AIR;
            villagerInventory.setItemStack(inputSlotIndex, ItemStack.AIR);
        }

        int remainingSpace = maxStackSize - currentAmount;
        if (remainingSpace <= 0) return;

        // Pull from main inventory first, then hotbar
        currentInput = pullFromRange(inv, required, currentInput, 9, 36, remainingSpace);
        remainingSpace = maxStackSize - (currentInput.isAir() ? 0 : currentInput.amount());
        if (remainingSpace > 0) {
            currentInput = pullFromRange(inv, required, currentInput, 0, 9, remainingSpace);
        }

        // Set final stack into input slot
        if (!currentInput.isAir()) {
            villagerInventory.setItemStack(inputSlotIndex, currentInput);
        }
    }

    // ✅ Now returns the updated stack instead of modifying it indirectly
    private static ItemStack pullFromRange(PlayerInventory inv, TradeListPacket.ItemCost required, ItemStack currentInput, int start, int end, int remainingSpace) {
        for (int i = start; i < end; i++) {
            ItemStack invStack = inv.getItemStack(i);
            if (!costAndStackMatch(required, invStack)) continue;

            int takeAmount = Math.min(invStack.amount(), remainingSpace);

            if (currentInput.isAir()) {
                currentInput = invStack.withAmount(takeAmount);
            } else {
                currentInput = currentInput.withAmount(currentInput.amount() + takeAmount);
            }

            // Subtract from inventory
            if (invStack.amount() == takeAmount) {
                inv.setItemStack(i, ItemStack.AIR);
            } else {
                inv.setItemStack(i, invStack.withAmount(invStack.amount() - takeAmount));
            }

            remainingSpace -= takeAmount;
            if (remainingSpace <= 0) break;
        }

        return currentInput;
    }
}
