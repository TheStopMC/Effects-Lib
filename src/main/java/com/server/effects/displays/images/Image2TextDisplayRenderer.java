package com.server.effects.displays.images;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.nio.file.Files;
import java.nio.file.Path;

public class Image2TextDisplayRenderer {

    private static final String PIXEL_STR = "⏹ ";
    private static final int CHAR_PIXELS = 5;
    private static final int LINE_WIDTH = CHAR_PIXELS * 2;
    private static final float CHAR_LENGTH = CHAR_PIXELS / 40f;

    private final ColorModel rgb;

    private Image2TextDisplayRenderer() {
        this.rgb = null;
    }

    private Image2TextDisplayRenderer(ColorModel rgb) {
        this.rgb = rgb;
    }

    private Image2TextDisplayRenderer(int[] bitMasks) {
        this(new DirectColorModel(32, bitMasks[0], bitMasks[1], bitMasks[2]));
    }

    public Surface surface(Supplier supplier) {
        return new Surface(supplier);
    }

    public int getColor(int pixel) {
        return rgb.getRGB(pixel);
    }

    public boolean renderNextField(Surface surface) {
        while (!surface.next()) {}
        return surface.unloaded;
    }

    public static void renderImage(Path imagePath, Instance instance, Pos position) {
        int width = 0;
        int height = 0;
        int[] pixels = new int[0];
        try {
            BufferedImage image = resize(ImageIO.read(Files.newInputStream(imagePath)), 64);
            if (image == null) throw new RuntimeException("no");
            width = image.getWidth();
            height = image.getHeight();
            pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        Image2TextDisplayRenderer renderer = new Image2TextDisplayRenderer(new int[]{0x00FF0000, 0x0000FF00, 0x000000FF});
        Surface surface = renderer.surface((x, y) -> {
            net.minestom.server.entity.Entity entity = new net.minestom.server.entity.Entity(EntityType.TEXT_DISPLAY);
            TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
            meta.setHasNoGravity(true);
            meta.setTranslation(new Vec(x, y, 0));
            meta.setScale(new Vec(0.5, 0.5, 0.5));
            meta.setBackgroundColor(0x00000000);
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.FIXED);
            meta.setViewRange(1);
            entity.setInstance(instance, position);
            return new Entity() {
                @Override
                public void update(int lineWidth) {
                    meta.setLineWidth(lineWidth);
                }

                @Override
                public void render(Component text) {
                    meta.setText(text);
                }
            };
        });

        surface.update(pixels, width, height);
        for (int i = 0; i < surface.fieldsCount(); i++) {
            if (renderer.renderNextField(surface)) break;
        }
    }


    private static BufferedImage resize(BufferedImage original, int newWidth) {
        int newHeight = (int) (original.getHeight() * (newWidth / (double) original.getWidth()));
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();

        // Use high-quality scaling hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    public class Surface {

        private volatile int[] pixels;
        private volatile boolean unloaded;
        private int width, height;

        private int fixedWidth, fixedHeight, x, y;
        private final SurfaceField[] fields = new SurfaceField[4];

        public Surface(Supplier supplier) {
            fields[0] = new SurfaceField(supplier, 0, 1);
            fields[1] = new SurfaceField(supplier, 1, 1);
            fields[2] = new SurfaceField(supplier, 0, 0);
            fields[3] = new SurfaceField(supplier, 1, 0);
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int fieldsCount() {
            return fields.length;
        }

        public void update(int[] pixelsPtr, int width, int height) {
            if (pixelsPtr.length < width * height) {
                throw new IllegalArgumentException("Pixel array too small for dimensions");
            }
            this.pixels = pixelsPtr;
            this.width = width;
            this.height = height;

            fixedWidth = width % 2 == 0 ? width : width + 1;
            fixedHeight = height % 2 == 0 ? height : height + 1;
            for (SurfaceField field : fields) field.update(fixedWidth / 2, fixedHeight / 2);

            x = y = 0;
            unloaded = false;
        }

        public void stop() {
            unloaded = true;
            pixels = null;
        }

        private boolean next() {
            if (unloaded || pixels == null) return true;
            if (y >= fixedHeight) x = y = 0;
            int color = (x < width && y < height) ? getColor(pixels[y * width + x]) : 0;
            boolean fieldFinished = fields[(y % 2) * 2 + (x % 2)].next(color);
            if (++x >= fixedWidth) {
                x = 0;
                y++;
            }
            return fieldFinished;
        }
    }

    @FunctionalInterface
    public interface Supplier {
        Entity get(float x, float y);
    }

    public interface Entity {
        void update(int lineWidth);
        void render(Component text);
    }

    static class SurfaceField {
        int index, offX, offY;
        final Entity entity;
        final IntArrayList pixmap = new IntArrayList();
        SurfaceField(Supplier supplier, int offX, int offY) {
            this.offX = offX;
            this.offY = offY;
            entity = supplier.get(offX * CHAR_LENGTH / 2, offY * CHAR_LENGTH / 2);
        }

        boolean next(int color) {
            boolean reset = index >= pixmap.size();
            if (reset) {
                entity.render(pixmap.intStream()
                        .mapToObj(c -> Component.text(PIXEL_STR, TextColor.color(c)))
                        .collect(Component::text, TextComponent.Builder::append, TextComponent.Builder::append)
                        .build());
                index = 0;
            }
            if (index < pixmap.size()) pixmap.set(index++, color);
            return reset;
        }

        void update(int width, int height) {
            int newSize = width * height;
            pixmap.size(newSize);
            pixmap.trim();
            entity.update(width * LINE_WIDTH);
        }
    }
}