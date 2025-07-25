package com.server.effects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Paths {
    public static final Path ROOT = Path.of("");
    public static final Path NPCS = ROOT.resolve("npcs");
    public static final Path DISPLAYS = ROOT.resolve("displays");

    static {
        try {
            if (Files.notExists(NPCS)) Files.createDirectories(NPCS);
            if (Files.notExists(DISPLAYS)) Files.createDirectories(DISPLAYS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
