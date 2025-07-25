package com.server.effects.displays;

import com.server.effects.config.definitions.display.DisplayDefinition;
import com.server.effects.config.serializers.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.InstanceRegisterEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
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

import static com.server.effects.Paths.DISPLAYS;

public class DisplaysManager {
    private static final TypeSerializerCollection serializers = TypeSerializerCollection.create()
            .register(new PosTypeSerializer())
            .register(new EntityTypeSerializer())
            .register(new MaterialTypeSerializer())
            .register(new ComponentTypeSerializer())
            .register(new BlockTypeSerializer());

    private HashMap<String, HashMap<Integer, Display>> displays = new HashMap<>();

    public DisplaysManager() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, playerSpawnEvent -> {
            spawnDisplays(playerSpawnEvent.getInstance().getDimensionName(), playerSpawnEvent.getPlayer());
        });
        MinecraftServer.getGlobalEventHandler().addListener(InstanceRegisterEvent.class, instanceRegisterEvent -> {
            loadDisplays(instanceRegisterEvent.getInstance());
        });

        MinecraftServer.getCommandManager().register(new Command("reloadDisplays") {
            public Command load() {
                setDefaultExecutor((sender, context) -> {
                    MinecraftServer.getInstanceManager().getInstances().forEach(instance -> reloadDisplays(instance));
                });
                return this;
            }
        }.load());
    }

    public Optional<HashMap<Integer, Display>> displays(String dimensionName) {
        return Optional.ofNullable(displays.get(dimensionName));
    }

    public Optional<Display> removeDisplay(String dimensionName, int id) {
        return Optional.ofNullable(displays.get(dimensionName).remove(id));
    }

    public Optional<Display> getDisplay(String dimensionName, int id) {
        return Optional.ofNullable(displays.get(dimensionName).get(id));
    }

    public Display addDisplay(String dimensionName, Display display) {
        final int id = ThreadLocalRandom.current().nextInt(10000, Integer.MAX_VALUE);
        if (displays.containsKey(dimensionName) && displays.get(dimensionName) != null) {
            displays.get(dimensionName).put(id, display);
            return display;
        } else {
            HashMap<Integer, Display> map = new HashMap<>();
            map.put(id, display);
            displays.put(dimensionName, map);
            return display;
        }
    }

    public void spawnDisplays(String dimensionName, Player player) {
        displays(dimensionName).ifPresent(npcs ->
                npcs.forEach((id, display) -> display.spawn(id, player)));
    }

    public void loadDisplays(Instance instance) {
        Path displayPath = DISPLAYS.resolve(instance.getDimensionName().replaceFirst("map:", ""));
        if (Files.notExists(displayPath)) {
            try {
                Files.createDirectories(displayPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (Stream<Path> stream = Files.walk(displayPath, 1)) {
            stream.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".conf"))
                    .forEach(path -> {
                        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                                .setPath(path)
                                .setDefaultOptions(opts -> opts.withSerializers(serializers))
                                .build();

                        try {
                            ConfigurationNode root = loader.load();
                            ObjectMapperFactory factory = new DefaultObjectMapperFactory();
                            ObjectMapper<DisplayDefinition> mapper = factory.getMapper(DisplayDefinition.class);
                            DisplayDefinition definition = mapper.bindToNew().populate(root);
                            addDisplay(instance.getDimensionName(), Display.fromDefinition(definition));
                        } catch (IOException | ObjectMappingException e) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        }
                    });

        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    public void reloadDisplays(Instance instance) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
            DestroyEntitiesPacket removeAll = new DestroyEntitiesPacket(displays.get(player.getInstance().getDimensionName()).keySet().stream().toList());
            player.sendPacket(removeAll);
        });
        if (displays.containsKey(instance.getDimensionName())) {displays.replace(instance.getDimensionName(), new HashMap<>());}
        loadDisplays(instance);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
            spawnDisplays(player.getInstance().getDimensionName(), player);
        });
    }

}
