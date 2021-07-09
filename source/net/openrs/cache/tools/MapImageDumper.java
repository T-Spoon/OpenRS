/*
 * Copyright (C) Kyle Fricilone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.openrs.cache.tools;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.openrs.cache.Archive;
import net.openrs.cache.Cache;
import net.openrs.cache.Constants;
import net.openrs.cache.Container;
import net.openrs.cache.FileStore;
import net.openrs.cache.ReferenceTable;
import net.openrs.cache.region.Location;
import net.openrs.cache.region.Region;
import net.openrs.cache.sprite.Sprite;
import net.openrs.cache.sprite.Sprites;
import net.openrs.cache.sprite.Textures;
import net.openrs.cache.type.CacheIndex;
import net.openrs.cache.type.TypeListManager;
import net.openrs.cache.type.areas.AreaType;
import net.openrs.cache.type.objects.ObjectType;
import net.openrs.cache.type.overlays.OverlayType;
import net.openrs.cache.type.underlays.UnderlayType;
import net.openrs.cache.type.world.WorldMapType;
import net.openrs.cache.util.WorldMapLoader;
import net.openrs.cache.util.XTEAManager;
import net.openrs.util.BigBufferedImage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

/**
 * Created by Kyle Fricilone on Sep 16, 2016.
 * Optimizations done by Adam
 * Updated by Explv on October 02, 2018
 */
public class MapImageDumper {

    private static int[][] TILE_SHAPES = new int[][]{
            {
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1
            },
            {
                    1, 0, 0, 0,
                    1, 1, 0, 0,
                    1, 1, 1, 0,
                    1, 1, 1, 1
            },

            {
                    1, 1, 0, 0,
                    1, 1, 0, 0,
                    1, 0, 0, 0,
                    1, 0, 0, 0
            },

            {
                    0, 0, 1, 1,
                    0, 0, 1, 1,
                    0, 0, 0, 1,
                    0, 0, 0, 1,
            },
            {
                    0, 1, 1, 1,
                    0, 1, 1, 1,
                    1, 1, 1, 1,
                    1, 1, 1, 1
            },
            {
                    1, 1, 1, 0,
                    1, 1, 1, 0,
                    1, 1, 1, 1,
                    1, 1, 1, 1
            },
            {
                    1, 1, 0, 0,
                    1, 1, 0, 0,
                    1, 1, 0, 0,
                    1, 1, 0, 0
            },
            {
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    1, 0, 0, 0,
                    1, 1, 0, 0
            },
            {
                    1, 1, 1, 1,
                    1, 1, 1, 1,
                    0, 1, 1, 1,
                    0, 0, 1, 1
            },
            {
                    1, 1, 1, 1,
                    1, 1, 0, 0,
                    1, 0, 0, 0,
                    1, 0, 0, 0
            },

            {
                    0, 0, 0, 0,
                    0, 0, 1, 1,
                    0, 1, 1, 1,
                    0, 1, 1, 1
            },

            {
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 1, 1, 0,
                    1, 1, 1, 1
            }
    };

    private static int[][] TILE_ROTATIONS = new int[][]{
            {
                    0, 1, 2, 3,
                    4, 5, 6, 7,
                    8, 9, 10, 11,
                    12, 13, 14, 15
            },

            {
                    12, 8, 4, 0,
                    13, 9, 5, 1,
                    14, 10, 6, 2,
                    15, 11, 7, 3
            },

            {
                    15, 14, 13, 12,
                    11, 10, 9, 8,
                    7, 6, 5, 4,
                    3, 2, 1, 0
            },

            {
                    3, 7, 11, 15,
                    2, 6, 10, 14,
                    1, 5, 9, 13,
                    0, 4, 8, 12
            }
    };

    private final List<Region> allRegions = new ArrayList<>();
    private final List<Region> regions = new ArrayList<>();
    private final List<Integer> flags = new ArrayList<>();
    private final List<WorldMapType> worlds = new ArrayList<>();
    private final Map<Integer, Set<Integer>> worldMapRegionIds = new HashMap<>();

    private final Map<Integer, Image> mapIcons = new HashMap<>();

    private Region lowestX;
    private Region lowestY;
    private Region highestX;
    private Region highestY;

    private static final int MAX_REGION = 32768;
    private static final int PIXELS_PER_TILE = 4;

    private static final boolean CREATE_IMAGES = true;
    private static final boolean DRAW_WALLS = true;
    private static final boolean DRAW_ICONS = true;
    private static final boolean DRAW_LABELS = false;
    private static final boolean DRAW_REGIONS = false;
    private static final boolean LABEL = true;
    private static final boolean OUTLINE = true;
    private static final boolean FILL = true;

    private static final int X_MIN = 0, X_MAX = 80;
    private static final int Y_MIN = 0, Y_MAX = 180;
    private static final int Z_MIN = 0, Z_MAX = 0;

    private void initialize(final Cache cache) throws IOException {
        TypeListManager.initialize(cache);
        Textures.initialize(cache);
        Sprites.initialize(cache);
        XTEAManager.initializeFromJson(new File("./repository/xteas.json"));

        for (int i = 0; i < MAX_REGION; i++) {
            final Region region = new Region(i);

            int map = cache.getFileId(5, region.getTerrainIdentifier());
            int loc = cache.getFileId(5, region.getLocationsIdentifier());

            if (map == -1 && loc == -1) {
                continue;
            }

            if (map != -1) {
                region.loadTerrain(cache.read(5, map).getData());
            }

            if (loc != -1) {
                ByteBuffer buffer = cache.getStore().read(5, loc);
                try {
                    region.loadLocations(Container.decode(buffer, XTEAManager.lookupMap(i)).getData());
                } catch (Exception e) {
                    if (buffer.limit() != 32) {
                        flags.add(i);
                    }
                }
            }

            allRegions.add(region);
        }

        ReferenceTable table = cache.getReferenceTable(CacheIndex.WORLDMAP);
        ReferenceTable.Entry entry = table.getEntry(0);
        Archive archive = Archive.decode(cache.read(CacheIndex.WORLDMAP, 0).getData(), entry.size());

        final WorldMapLoader loader = new WorldMapLoader();

        final Map<WorldMapType, List<Region>> worldMapRegions = new HashMap<>();
        for (int i = 0; i < archive.size(); i++) {
            final WorldMapType worldMap = loader.load(archive.getEntry(i).array(), i);
            worlds.add(worldMap);

            final String mapName = worldMap.name;
            final String mapSafeName = worldMap.safeName;
            System.out.println("Map:" + mapName);

//            for (WorldMapTypeBase mapType : worldMap.regionList) {
//                //mapType
//            }

            final List<Region> mapRegions = new ArrayList<>();
            for (int z = Z_MIN; z <= Z_MAX; z++) {
                for (int x = X_MIN; x < X_MAX; x++) {
                    for (int y = Y_MIN; y < Y_MAX; y++) {
                        if (!worldMap.containsRegion(x, y, z)) {
                            continue;
                        }
                        final Region region = getRegion(x, y);

                        if (region == null) {
                            //System.out.println("Null region in map: + " + mapName + " [" + x + "," + y + "]");
                        } else {
                            mapRegions.add(region);
                            System.out.println("Region in map: " + mapName + " => " + region.getRegionID());
                        }
                    }
                }
            }
            worldMapRegions.put(worldMap, mapRegions);
        }

        for (Map.Entry<WorldMapType, List<Region>> e : worldMapRegions.entrySet()) {
            System.out.println("Map Regions: " + e.getKey().name + " => " + e.getValue().size());
            worldMapRegionIds.put(e.getKey().fileId, new HashSet<>(e.getValue().stream().map(Region::getRegionID).collect(Collectors.toList())));
        }

        final Sprite mapscene = Sprites.getSprite("mapscene");

        for (int i = 0; i < mapscene.size(); i++) {
            mapIcons.put(i, mapscene.getFrame(i));
        }
    }

    private void resetRegionsForWorld(Integer worldMapId) {
        regions.clear();
        lowestX = null;
        lowestY = null;
        highestX = null;
        highestY = null;
        for (Region region : allRegions) {
            if (!worldMapRegionIds.get(worldMapId).contains(region.getRegionID())) {
                continue;
            }

            regions.add(region);

            if (lowestX == null || region.getBaseX() < lowestX.getBaseX()) {
                lowestX = region;
            }

            if (highestX == null || region.getBaseX() > highestX.getBaseX()) {
                highestX = region;
            }

            if (lowestY == null || region.getBaseY() < lowestY.getBaseY()) {
                lowestY = region;
            }

            if (highestY == null || region.getBaseY() > highestY.getBaseY()) {
                highestY = region;
            }
        }

        System.out.println("Lowest X" + lowestX + ", Highest X: " + highestX);
    }

    private void draw(final WorldMapType map) throws IOException {
        resetRegionsForWorld(map.fileId);

        int minX = lowestX.getBaseX();
        int minY = lowestY.getBaseY();

        int maxX = highestX.getBaseX() + Region.WIDTH;
        int maxY = highestY.getBaseY() + Region.HEIGHT;

        int dimX = maxX - minX;
        int dimY = maxY - minY;

        int boundX = dimX - 1;
        int boundY = dimY - 1;

        dimX *= PIXELS_PER_TILE;
        dimY *= PIXELS_PER_TILE;

        if (CREATE_IMAGES) {
            for (int z = Z_MIN; z <= Z_MAX; z++) {
                System.out.println("Generating map images for " + map.name + ", z = " + z + "[" + dimX + "," + dimY + "]");

                BufferedImage baseImage = BigBufferedImage.create(dimX, dimY, BufferedImage.TYPE_INT_RGB);
                BufferedImage mapImage = BigBufferedImage.create(dimX, dimY, BufferedImage.TYPE_INT_RGB);
                BufferedImage overlayImage = BigBufferedImage.create(dimX, dimY, BufferedImage.TYPE_INT_ARGB);

                Graphics2D graphics = mapImage.createGraphics();
                Graphics2D overlayGraphics = overlayImage.createGraphics();

                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                overlayGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                overlayGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                overlayGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                System.out.println("Drawing underlay");
                drawUnderlay(z, baseImage);

                System.out.println("Blending underlay");
                blendUnderlay(z, baseImage, mapImage, boundX, boundY);

                System.out.println("Drawing overlay");
                drawOverlay(z, mapImage);

                System.out.println("Drawing locations");
                drawLocations(z, graphics);

                if (DRAW_WALLS) {
                    System.out.println("Drawing walls");
                    drawWalls(z, graphics);
                }

                if (DRAW_ICONS) {
                    System.out.println("Drawing icons");
                    drawIcons(z, overlayGraphics);
                }

                if (DRAW_LABELS) {
                    System.out.println("Drawing labels");
                    drawMapLabels(z, graphics);
                }

                if (DRAW_REGIONS) {
                    System.out.println("Drawing regions");
                    drawRegions(z, graphics);
                }

                graphics.dispose();
                overlayGraphics.dispose();

                System.out.println("Writing to files");


                ImageIO.write(baseImage, "png", new File("maps/" + map.safeName + "_base_image_" + z + ".png"));
                ImageIO.write(mapImage, "png", new File("maps/" + map.safeName + "_map_image_" + z + ".png"));
                ImageIO.write(overlayImage, "png", new File("maps/" + map.safeName + "_marker_image_" + z + ".png"));
            }
        }

        saveMapJson(map, regions);
    }

    private void drawUnderlay(int z, BufferedImage image) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (int x = 0; x < Region.WIDTH; ++x) {
                int drawX = drawBaseX + x;

                for (int y = 0; y < Region.HEIGHT; ++y) {
                    int drawY = drawBaseY + ((Region.HEIGHT - 1) - y);

                    int underlayId = region.getUnderlayId(z, x, y) - 1;

                    int rgb = Color.CYAN.getRGB();

                    if (underlayId > -1) {
                        UnderlayType underlay = TypeListManager.lookupUnder(underlayId);
                        rgb = underlay.getRgbColor();
                    }

                    drawMapSquare(image, drawX, drawY, rgb, -1, -1);
                }
            }
        }
    }

    private void blendUnderlay(int z, BufferedImage baseImage, BufferedImage fullImage, int boundX, int boundY) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (int x = 0; x < Region.WIDTH; ++x) {
                int drawX = drawBaseX + x;

                for (int y = 0; y < Region.HEIGHT; ++y) {
                    int drawY = drawBaseY + ((Region.HEIGHT - 1) - y);

                    Color c = getMapSquare(baseImage, drawX, drawY);

                    if (c.equals(Color.CYAN)) {
                        continue;
                    }

                    int tRed = 0, tGreen = 0, tBlue = 0;
                    int count = 0;

                    int maxDY = Math.min(boundY, drawY + 3);
                    int maxDX = Math.min(boundX, drawX + 3);
                    int minDY = Math.max(0, drawY - 3);
                    int minDX = Math.max(0, drawX - 3);


                    for (int dy = minDY; dy < maxDY; dy++) {
                        for (int dx = minDX; dx < maxDX; dx++) {
                            c = getMapSquare(baseImage, dx, dy);

                            if (c.equals(Color.CYAN)) {
                                continue;
                            }

                            tRed += c.getRed();
                            tGreen += c.getGreen();
                            tBlue += c.getBlue();
                            count++;
                        }
                    }

                    if (count > 0) {
                        c = new Color(tRed / count, tGreen / count, tBlue / count);
                        drawMapSquare(fullImage, drawX, drawY, c.getRGB(), -1, -1);
                    }
                }
            }
        }
    }

    private void drawOverlay(int z, BufferedImage image) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (int x = 0; x < Region.WIDTH; ++x) {
                int drawX = drawBaseX + x;

                for (int y = 0; y < Region.HEIGHT; ++y) {
                    int drawY = drawBaseY + ((Region.HEIGHT - 1) - y);

                    if (z == 0 || (!region.isLinkedBelow(z, x, y) && !region.isVisibleBelow(z, x, y))) {
                        int overlayId = region.getOverlayId(z, x, y) - 1;
                        if (overlayId > -1) {
                            int rgb = getOverLayColour(overlayId);
                            drawMapSquare(image, drawX, drawY, rgb, region.getOverlayPath(z, x, y), region.getOverlayRotation(z, x, y));
                        }
                    }

                    if (z < 3 && (region.isLinkedBelow(z + 1, x, y) || region.isVisibleBelow(z + 1, x, y))) {
                        int overlayAboveId = region.getOverlayId(z + 1, x, y) - 1;
                        if (overlayAboveId > -1) {
                            int rgb = getOverLayColour(overlayAboveId);
                            drawMapSquare(image, drawX, drawY, rgb, region.getOverlayPath(z + 1, x, y), region.getOverlayRotation(z + 1, x, y));
                        }
                    }
                }
            }
        }
    }

    private int getOverLayColour(int overlayID) {
        OverlayType overlay = TypeListManager.lookupOver(overlayID);

        int rgb = 0;

        if (overlay.isHideUnderlay()) {
            rgb = overlay.getRgbColor();
        }


        if (overlay.getSecondaryRgbColor() > -1) {
            rgb = overlay.getSecondaryRgbColor();
        }


        if (overlay.getTexture() > -1) {
            rgb = Textures.getColors(overlay.getTexture());
        }

        return rgb;
    }

    private void drawLocations(int z, Graphics2D graphics) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (Location location : region.getLocations()) {
                int localX = location.getPosition().getX() - region.getBaseX();
                int localY = location.getPosition().getY() - region.getBaseY();

                if (!canDrawLocation(region, location, z, localX, localY)) {
                    continue;
                }

                ObjectType objType = TypeListManager.lookupObject(location.getId());

                int drawX = drawBaseX + localX;
                int drawY = drawBaseY + ((Region.HEIGHT - 1) - localY);

                if (objType.getMapSceneID() != -1) {
                    Image spriteImage = mapIcons.get(objType.getMapSceneID());
                    graphics.drawImage(spriteImage, drawX * PIXELS_PER_TILE, drawY * PIXELS_PER_TILE, null);
                }
            }
        }
    }

    private void drawWalls(int z, Graphics2D graphics) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (Location location : region.getLocations()) {
                graphics.setColor(Color.WHITE);

                int localX = location.getPosition().getX() - region.getBaseX();
                int localY = location.getPosition().getY() - region.getBaseY();

                if (!canDrawLocation(region, location, z, localX, localY)) {
                    continue;
                }

                ObjectType objType = TypeListManager.lookupObject(location.getId());

                // Don't draw walls on water
                if (objType.getMapSceneID() == 22) {
                    continue;
                }

                String objName = objType.getName().toLowerCase();

                if (objName.contains("door") || objName.contains("gate")) {
                    graphics.setColor(Color.RED);
                }

                int drawX = drawBaseX + localX;
                int drawY = drawBaseY + ((Region.HEIGHT - 1) - localY);

                drawX *= PIXELS_PER_TILE;
                drawY *= PIXELS_PER_TILE;

                if (location.getType() == 0) { // Straight walls
                    if (location.getOrientation() == 0) { // West
                        graphics.drawLine(drawX, drawY, drawX, drawY + PIXELS_PER_TILE);
                    } else if (location.getOrientation() == 1) { // South
                        graphics.drawLine(drawX, drawY, drawX + PIXELS_PER_TILE, drawY);
                    } else if (location.getOrientation() == 2) { // East
                        graphics.drawLine(drawX + PIXELS_PER_TILE, drawY, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                    } else if (location.getOrientation() == 3) { // North
                        graphics.drawLine(drawX, drawY + PIXELS_PER_TILE, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                    }
                } else if (location.getType() == 2) { // Corner walls
                    if (location.getOrientation() == 0) { // West & South
                        graphics.drawLine(drawX, drawY, drawX, drawY + PIXELS_PER_TILE);
                        graphics.drawLine(drawX, drawY, drawX + PIXELS_PER_TILE, drawY);
                    } else if (location.getOrientation() == 1) { // South & East
                        graphics.drawLine(drawX, drawY, drawX + PIXELS_PER_TILE, drawY);
                        graphics.drawLine(drawX + PIXELS_PER_TILE, drawY, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                    } else if (location.getOrientation() == 2) { // East & North
                        graphics.drawLine(drawX + PIXELS_PER_TILE, drawY, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                        graphics.drawLine(drawX, drawY + PIXELS_PER_TILE, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                    } else if (location.getOrientation() == 3) { // North & West
                        graphics.drawLine(drawX, drawY + PIXELS_PER_TILE, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                        graphics.drawLine(drawX, drawY, drawX, drawY + PIXELS_PER_TILE);
                    }
                } else if (location.getType() == 3) { // Single points
                    if (location.getOrientation() == 0) { // West
                        graphics.drawLine(drawX, drawY + 1, drawX, drawY + 1);
                    } else if (location.getOrientation() == 1) { // South
                        graphics.drawLine(drawX + 3, drawY + 1, drawX + 3, drawY + 1);
                    } else if (location.getOrientation() == 2) { // East
                        graphics.drawLine(drawX + 3, drawY + 4, drawX + 3, drawY + 4);
                    } else if (location.getOrientation() == 3) { // North
                        graphics.drawLine(drawX, drawY + 3, drawX, drawY + 3);
                    }
                } else if (location.getType() == 9) { // Diagonal walls
                    if (location.getOrientation() == 0 || location.getOrientation() == 2) { // West or East
                        graphics.drawLine(drawX, drawY + PIXELS_PER_TILE, drawX + PIXELS_PER_TILE, drawY);
                    } else if (location.getOrientation() == 1 || location.getOrientation() == 3) { // South or South
                        graphics.drawLine(drawX, drawY, drawX + PIXELS_PER_TILE, drawY + PIXELS_PER_TILE);
                    }
                }
            }
        }
    }

    private void drawIcons(int z, Graphics2D graphics) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            System.out.println("Drawing " + region.getLocations().size() + " map icons for region: " + region.getRegionID());
            for (Location location : region.getLocations()) {
                int localX = location.getPosition().getX() - region.getBaseX();
                int localY = location.getPosition().getY() - region.getBaseY();

                if (!canDrawLocation(region, location, z, localX, localY)) {
                    //System.out.println("Can't draw location...");
                    continue;
                }

                ObjectType objType = TypeListManager.lookupObject(location.getId());

                int drawX = drawBaseX + localX;
                int drawY = drawBaseY + (63 - localY);

                if (objType.getMapAreaId() != -1) {
                    AreaType areaType = TypeListManager.lookupArea(objType.getMapAreaId());
                    Image spriteImage = Sprites.getSprite(areaType.getSpriteId()).getFrame(0);
                    //System.out.println("Drawing icon... " + objType.getName());
                    graphics.drawImage(spriteImage, (drawX - 1) * PIXELS_PER_TILE, (drawY - 1) * PIXELS_PER_TILE, null);
                }
            }
        }
    }

    private void drawMapLabels(int z, Graphics2D graphics) {
        for (Region region : regions) {
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (Location location : region.getLocations()) {
                int localX = location.getPosition().getX() - region.getBaseX();
                int localY = location.getPosition().getY() - region.getBaseY();

//                if (!canDrawLocation(region, location, z, localX, localY)) {
//                    //System.out.println("Can't draw location...");
//                    continue;
//                }

                ObjectType objType = TypeListManager.lookupObject(location.getId());

                int drawX = drawBaseX + localX;
                int drawY = drawBaseY + (63 - localY);

                if (objType.getMapAreaId() != -1) {
                    AreaType areaType = TypeListManager.lookupArea(objType.getMapAreaId());
                    if (areaType.getName() == null) {
                        System.out.println("Not drawing " + areaType.getID());
                        continue;
                    }

                    System.out.println("Drawing label... " + areaType.getName());
                    graphics.setColor(Color.WHITE);
                    graphics.drawString(areaType.getName(), drawX * PIXELS_PER_TILE, drawY * PIXELS_PER_TILE + graphics.getFontMetrics().getHeight());
                    //graphics.drawImage(spriteImage, (drawX - 1) * PIXELS_PER_TILE, (drawY - 1) * PIXELS_PER_TILE, null);
                }
            }
        }
    }

    private boolean canDrawLocation(Region region, Location location, int z, int x, int y) {
        if (region.isLinkedBelow(z, x, y) || region.isVisibleBelow(z, x, y)) {
            return false;
        }

        if (location.getPosition().getHeight() == z + 1 && (region.isLinkedBelow(z + 1, x, y) || region.isVisibleBelow(z + 1, x, y))) {
            return true;
        }

        return z == location.getPosition().getHeight();
    }

    private void drawRegions(int z, Graphics2D graphics) {
        for (Region region : regions) {
            int baseX = region.getBaseX();
            int baseY = region.getBaseY();
            int drawBaseX = baseX - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - baseY;

            if (LABEL) {
                graphics.setColor(Color.RED);
                graphics.drawString(String.valueOf(region.getRegionID()), drawBaseX * PIXELS_PER_TILE, drawBaseY * PIXELS_PER_TILE + graphics.getFontMetrics().getHeight());
            }

            if (OUTLINE) {
                graphics.setColor(Color.RED);
                graphics.drawRect(drawBaseX * PIXELS_PER_TILE, drawBaseY * PIXELS_PER_TILE, 64 * PIXELS_PER_TILE, 64 * PIXELS_PER_TILE);
            }

            if (FILL) {
                if (flags.contains(region.getRegionID())) {
                    graphics.setColor(new Color(255, 0, 0, 80));
                    graphics.fillRect(drawBaseX * PIXELS_PER_TILE, drawBaseY * PIXELS_PER_TILE, 64 * PIXELS_PER_TILE, 64 * PIXELS_PER_TILE);
                }
            }
        }
    }

    private void drawMapSquare(BufferedImage image, int x, int y, int overlayRGB, int shape, int rotation) {
        if (shape > -1) {
            int[] shapeMatrix = TILE_SHAPES[shape];
            int[] rotationMatrix = TILE_ROTATIONS[rotation & 0x3];
            int shapeIndex = 0;
            for (int tilePixelY = 0; tilePixelY < PIXELS_PER_TILE; tilePixelY++) {
                for (int tilePixelX = 0; tilePixelX < PIXELS_PER_TILE; tilePixelX++) {
                    int drawx = x * PIXELS_PER_TILE + tilePixelX;
                    int drawy = y * PIXELS_PER_TILE + tilePixelY;

                    if (shapeMatrix[rotationMatrix[shapeIndex++]] != 0) {
                        image.setRGB(drawx, drawy, overlayRGB);
                    }
                }
            }
        } else {
            for (int tilePixelY = 0; tilePixelY < PIXELS_PER_TILE; tilePixelY++) {
                for (int tilePixelX = 0; tilePixelX < PIXELS_PER_TILE; tilePixelX++) {
                    int drawx = x * PIXELS_PER_TILE + tilePixelX;
                    int drawy = y * PIXELS_PER_TILE + tilePixelY;
                    image.setRGB(drawx, drawy, overlayRGB);
                }
            }
        }
    }

    public Color getMapSquare(BufferedImage image, int x, int y) {
        x *= PIXELS_PER_TILE;
        y *= PIXELS_PER_TILE;

        return new Color(image.getRGB(x, y));
    }

    public Region getRegion(int x, int y) {
        for (Region region : allRegions) {
            int rx = region.getRegionX();
            int ry = region.getRegionY();
            if (x == rx && y == ry) {
                return region;
            }
        }
        return null;
    }

    private void saveMapJson(final WorldMapType map, final List<Region> regions) throws IOException {
        // RegionId -> Items
        final Map<Integer, List<MapLocation>> mapIcons = new HashMap<>();
        final List<MapLocation> mapLocations = new ArrayList<>();
        final Map<Integer, Integer> locationsByType = new HashMap<>();
        //final Map<Integer, Map<Integer, Integer>> locationsByRegionByType = new HashMap<>();
        for (Region region : regions) {
            //System.out.println("Region:" + region.getRegionID());

            final List<MapLocation> regionItems = new ArrayList<>();
            int drawBaseX = region.getBaseX() - lowestX.getBaseX();
            int drawBaseY = highestY.getBaseY() - region.getBaseY();

            for (Location location : region.getLocations()) {
                int localX = location.getPosition().getX() - region.getBaseX();
                int localY = location.getPosition().getY() - region.getBaseY();

                // TODO: Check Z plane value
//                if (!canDrawLocation(region, location, 0, localX, localY)) {
//                    //System.out.println("Can't draw location...");
//                    continue;
//                }

                ObjectType objType = TypeListManager.lookupObject(location.getId());

                int drawX = drawBaseX + localX;
                int drawY = drawBaseY + (63 - localY);

                locationsByType.put(location.getType(), locationsByType.getOrDefault(location.getType(), 1) + 1);

                if (objType.getMapAreaId() != -1) {
                    AreaType areaType = TypeListManager.lookupArea(objType.getMapAreaId());
                    MapLocation item = new MapLocation(location.getId(), areaType.getSpriteId(), areaType.getName(), location.getType(), drawX, drawY);
                    regionItems.add(item);
                    mapLocations.add(item);
                    //System.out.println(item.toString());
                } else {

                    //System.out.println("Type:" + location.getType());
                    try {
                        //AreaType areaType = TypeListManager.lookupArea(objType.getObjectID());
                        //System.out.println(areaType.getName() + " - " + location.getPosition());
                        //regionItems.add(new MapExportItem(location.getId(), -1, areaType.getName(), location.getType(), drawX, drawY));
                    } catch (Exception e) {
                        //System.err.println(e);
                    }
                }
            }

            mapIcons.put(region.getRegionID(), regionItems);
        }


        for (Map.Entry<Integer, Integer> entry : locationsByType.entrySet()) {
            //System.out.println("Type: " + entry.getKey() + " => " + entry.getValue().toString());
        }

        final Gson gson = new Gson();
        final FileWriter fileWriter = new FileWriter("maps/" + map.safeName + "_data.json");
        gson.toJson(new MapData(map.fileId, map.name, map.safeName, map.getBoundsAsString(), new int[]{map.position.getX(), map.position.getY()}, mapLocations), fileWriter);
        fileWriter.close();
    }

    private static class MapData {
        @SerializedName("id") int id;
        @SerializedName("name") String name;
        @SerializedName("safe_name") String safeName;
        @SerializedName("bounds") String bounds;
        @SerializedName("center") int[] center;
        @SerializedName("locations") List<MapLocation> locations;

        public MapData(int id, String name, String safeName, String bounds, int[] center, List<MapLocation> locations) {
            this.id = id;
            this.name = name;
            this.safeName = safeName;
            this.bounds = bounds;
            this.center = center;
            this.locations = locations;
        }
    }

    private static class MapLocation {
        @SerializedName("id") int id;
        @SerializedName("sprite_id") int spriteId;
        @SerializedName("area") String area;
        @SerializedName("type") int type;
        @SerializedName("x") int x;
        @SerializedName("y") int y;

        public MapLocation(int id, int spriteId, String area, int type, int x, int y) {
            this.id = id;
            this.spriteId = spriteId;
            this.area = area;
            this.type = type;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "MapExportItem{" +
                    "id=" + id +
                    ", spriteId=" + spriteId +
                    ", area='" + area + '\'' +
                    ", type=" + type +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static void main(String[] args) {
        long ms = System.currentTimeMillis();
        MapImageDumper dumper = new MapImageDumper();

        try (Cache cache = new Cache(FileStore.open(Constants.CACHE_PATH))) {
            dumper.initialize(cache);

            for (WorldMapType world : dumper.worlds) {

                if (world.fileId != 0) {
                    continue;
                }
                dumper.draw(world);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Time taken: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - ms) + "s");
    }
}
