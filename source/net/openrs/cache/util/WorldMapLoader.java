package net.openrs.cache.util;

import net.openrs.cache.region.Position;
import net.openrs.cache.type.world.WorldMapType;
import net.openrs.cache.type.world.WorldMapType0;
import net.openrs.cache.type.world.WorldMapType1;
import net.openrs.cache.type.world.WorldMapType2;
import net.openrs.cache.type.world.WorldMapType3;
import net.openrs.cache.type.world.WorldMapTypeBase;

import java.util.LinkedList;

public class WorldMapLoader {
    public WorldMapType load(byte[] b, int fileId) {
        WorldMapType def = new WorldMapType();
        InputStream in = new InputStream(b);

        def.fileId = fileId;
        def.safeName = in.readString();
        def.name = in.readString();

        int packedPos = in.readInt();
        if (packedPos == -1) {
            def.position = new Position(-1, -1, -1);
        } else {
            int y = packedPos >> 28 & 3;
            int x = packedPos >> 14 & 16383;
            int z = packedPos & 16383;
            def.position = new Position(x, y, z);
        }

        def.field450 = in.readInt();
        in.readUnsignedByte();
        def.isSurface = in.readUnsignedByte() == 1;
        def.defaultZoom = in.readUnsignedByte();
        int var3 = in.readUnsignedByte();
        def.regionList = new LinkedList();

        for (int var4 = 0; var4 < var3; ++var4) {
            def.regionList.add(this.loadType(in));
        }

        return def;
    }

    private WorldMapTypeBase loadType(InputStream var1) {
        int var2 = var1.readUnsignedByte();
        //      field397 = new class27(1, (byte)0);
        //      field390 = new class27(2, (byte)1);
        //      field399 = new class27(3, (byte)2);
        //      field393 = new class27(0, (byte)3);
        WorldMapTypeBase base;
        switch (var2) {
            case 0:
                // type 1
                base = load1(var1);
                break;
            case 1:
                // type 2
                base = load2(var1);
                break;
            case 2:
                // type 3
                base = load3(var1);
                break;
            case 3:
                // type 0
                base = load0(var1);
                break;
            default:
                throw new IllegalStateException();
        }
        return base;
    }

    private WorldMapTypeBase load0(InputStream in) {
        WorldMapType0 wm = new WorldMapType0();

        wm.plane = in.readUnsignedByte();
        wm.numberOfPlanes = in.readUnsignedByte();
        wm.xLow = in.readUnsignedShort();
        wm.chunk_xLow = in.readUnsignedByte();
        wm.yLow = in.readUnsignedShort();
        wm.chunk_yLow = in.readUnsignedByte();
        wm.xHigh = in.readUnsignedShort();
        wm.chunk_xHigh = in.readUnsignedByte();
        wm.yHigh = in.readUnsignedShort();
        wm.chunk_yHigh = in.readUnsignedByte();

        return wm;
    }

    private WorldMapTypeBase load1(InputStream in) {
        WorldMapType1 wm = new WorldMapType1();

        wm.plane = in.readUnsignedByte();
        wm.numberOfPlanes = in.readUnsignedByte();
        wm.xLowerLeft = in.readUnsignedShort();
        wm.yLowerLeft = in.readUnsignedShort();
        wm.xLowerRight = in.readUnsignedShort();
        wm.yUpperLeft = in.readUnsignedShort();
        wm.xUpperLeft = in.readUnsignedShort();
        wm.yLowerRight = in.readUnsignedShort();
        wm.xUpperRight = in.readUnsignedShort();
        wm.yUpperRight = in.readUnsignedShort();

        return wm;
    }

    private WorldMapTypeBase load2(InputStream in) {
        WorldMapType2 wm = new WorldMapType2();

        wm.plane = in.readUnsignedByte();
        wm.numberOfPlanes = in.readUnsignedByte();
        wm.xLow = in.readUnsignedShort();
        wm.yLow = in.readUnsignedShort();
        wm.xHigh = in.readUnsignedShort();
        wm.yHigh = in.readUnsignedShort();

        return wm;
    }

    private WorldMapTypeBase load3(InputStream in) {
        WorldMapType3 wm = new WorldMapType3();

        wm.oldPlane = in.readUnsignedByte();
        wm.numberOfPlanes = in.readUnsignedByte();
        wm.oldX = in.readUnsignedShort();
        wm.chunk_oldXLow = in.readUnsignedByte();
        wm.chunk_oldXHigh = in.readUnsignedByte();
        wm.oldY = in.readUnsignedShort();
        wm.chunk_oldYLow = in.readUnsignedByte();
        wm.chunk_oldYHigh = in.readUnsignedByte();
        wm.newX = in.readUnsignedShort();
        wm.chunk_newXLow = in.readUnsignedByte();
        wm.chunk_newXHigh = in.readUnsignedByte();
        wm.newY = in.readUnsignedShort();
        wm.chunk_newYLow = in.readUnsignedByte();
        wm.chunk_newYHigh = in.readUnsignedByte();

        return wm;
    }
}
