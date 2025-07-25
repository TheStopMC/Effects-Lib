package com.server.effects.npc;

import com.server.effects.config.definitions.npcs.NPCDefinition;
import com.server.effects.config.serializers.*;
import com.server.effects.npc.inventories.VillagerInventoryImpl;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.InstanceRegisterEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.server.effects.Paths.NPCS;

public class NPCManager {


    private static final TypeSerializerCollection serializers = TypeSerializerCollection.create()
            .register(new PosTypeSerializer())
            .register(new EntityTypeSerializer())
            .register(new MaterialTypeSerializer())
            .register(new ComponentTypeSerializer())
            .register(new BlockTypeSerializer());


    private HashMap<String, HashMap<Integer, NPC>> npcs = new HashMap<>();

    public NPCManager() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, playerSpawnEvent -> {
            spawnNPCS(playerSpawnEvent.getInstance().getDimensionName(), playerSpawnEvent.getPlayer());
        });
        MinecraftServer.getGlobalEventHandler().addListener(InstanceRegisterEvent.class, instanceRegisterEvent -> {
            loadNPCS(instanceRegisterEvent.getInstance());
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, packet -> {
            if (packet.getPacket() instanceof ClientInteractEntityPacket(int targetId, ClientInteractEntityPacket.Type type, boolean sneaking)) {
                npcs(packet.getPlayer().getInstance().getDimensionName()).ifPresent(worldNPCS -> {
                    if (worldNPCS.containsKey(targetId)) {
                        NPC npc = worldNPCS.get(targetId);
                        npc.getTrades().ifPresent(trades -> {
                            VillagerInventoryImpl villagerInventory = new VillagerInventoryImpl(npc.getName());
                            trades.forEach(villagerInventory::addTrade);
                            packet.getPlayer().openInventory(villagerInventory);
                        });
//                    switch (npc.getMessageSendOrder()) {
//                        case "RANDOM" -> {
//                            ThreadLocalRandom random = ThreadLocalRandom.current();
//                            npc.getMessages().ifPresent(messages -> {
//                                packet.getPlayer().sendMessage(messages.get(random.nextInt(messages.size()) - 1));
//                            });
//                        }
//                        case "ORDER" -> {
//
//                        }
//                        default -> {}
//                    }
                    }
                });
            }
        });

        MinecraftServer.getCommandManager().register(new Command("reloadNPCS") {
            public Command load() {
//                setCondition((sender, commandString) -> sender instanceof ConsoleSender || ((AbstractServerPlayer) sender).hasPermission("admin.reloadnpcs"));


                setDefaultExecutor((sender, context) -> {
                    MinecraftServer.getInstanceManager().getInstances().forEach(instance -> reloadNPCS(instance));
                });
                return this;
            }
        }.load());
    }

    public Optional<HashMap<Integer, NPC>> npcs(String dimensionName) {return Optional.ofNullable(npcs.get(dimensionName));}

    public Optional<NPC> removeNPC(String dimensionName, int id) {
        return Optional.ofNullable(npcs.get(dimensionName).remove(id));
    }

    public Optional<NPC> getNPC(String dimensionName, int id) {
        return Optional.ofNullable(npcs.get(dimensionName).get(id));
    }

    public NPC addNPC(String dimensionName, NPC npc) {
        final int id = ThreadLocalRandom.current().nextInt(10000, Integer.MAX_VALUE);
        if (npcs.containsKey(dimensionName) && npcs.get(dimensionName) != null) {
            npcs.get(dimensionName).put(id, npc);
            return npc;
        } else {
            HashMap<Integer, NPC> map = new HashMap<>();
            map.put(id, npc);
            npcs.put(dimensionName, map);
            return npc;
        }
    }

    public void spawnNPCS(String dimensionName, Player player) {
        npcs.get(dimensionName).forEach((id, npc) -> {
            npc.spawn(player, id);
        });
    }

    public void loadNPCS(Instance instance) {
        Path npcPath = NPCS.resolve(instance.getDimensionName().replaceFirst("map:", ""));
        if (Files.notExists(npcPath)) {
            try {
                Files.createDirectories(npcPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (Stream<Path> stream = Files.walk(npcPath, 1)) {
            stream.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".conf"))
                    .forEach(path -> {
                        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                                .setPath(path)
                                .setDefaultOptions(opts -> opts.withSerializers(serializers))
                                .build();

                        try {
                            ConfigurationNode root = loader.load();
                            ObjectMapperFactory factory = new DefaultObjectMapperFactory();
                            ObjectMapper<NPCDefinition> mapper = factory.getMapper(NPCDefinition.class);
                            NPCDefinition definition = mapper.bindToNew().populate(root);
                            addNPC(instance.getDimensionName(), NPC.fromDefinition(definition));
                        } catch (IOException | ObjectMappingException e) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        }
                    });

        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    public void reloadNPCS(Instance instance) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
            DestroyEntitiesPacket removeAll = new DestroyEntitiesPacket(npcs.get(player.getInstance().getDimensionName()).keySet().stream().toList());
            player.sendPacket(removeAll);
            PlayerInfoRemovePacket removePlayerInfo = new PlayerInfoRemovePacket((npcs.get(player.getInstance().getDimensionName()).values().stream().map(NPC::getUuid).toList()));
            player.sendPacket(removePlayerInfo);
            DestroyEntitiesPacket removeAllNameTags = new DestroyEntitiesPacket(npcs.get(player.getInstance().getDimensionName()).keySet().stream().map(i -> -i).toList());
            player.sendPacket(removeAllNameTags);
        });
        if (npcs.containsKey(instance.getDimensionName())) {npcs.replace(instance.getDimensionName(), new HashMap<>());}
        loadNPCS(instance);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
            spawnNPCS(player.getInstance().getDimensionName(), player);
        });
    }

}
