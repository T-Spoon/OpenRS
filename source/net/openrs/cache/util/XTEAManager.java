/**
 * Copyright (c) Kyle Fricilone
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.openrs.cache.util;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.openrs.cache.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kyle Friz
 * @since Jun 30, 2015
 */
public class XTEAManager {

    private static final Map<Integer, int[]> maps = new HashMap<Integer, int[]>();
    private static final Map<Integer, int[]> tables = new HashMap<Integer, int[]>();

    public static final int[] NULL_KEYS = new int[4];

    public static final int[] lookupTable(int id) {
        int[] keys = tables.get(id);
        if (keys == null)
            return NULL_KEYS;

        return keys;
    }

    public static final int[] lookupMap(int id) {
        int[] keys = maps.get(id);
        if (keys == null)
            return NULL_KEYS;

        return keys;
    }

    static {
        try {

//            File xMapDir = new File(Constants.XMAP_PATH);
//
//            if (!xMapDir.exists()) {
//                xMapDir.mkdirs();
//            }
//
//            for (File file : xMapDir.listFiles()) {
//                if (file.getName().endsWith(".txt")) {
//                    Integer regionID = Integer.valueOf(file.getName().substring(0,
//                            file.getName().indexOf(".txt")));
//
//                    int[] keys = Files.lines(Paths.get(".")
//                            .resolve(Constants.XMAP_PATH + file.getName()))
//                            .map(Integer::valueOf).mapToInt(Integer::intValue).toArray();
//
//                    maps.put(regionID, keys);
//                }
//            }
//
//            File xTableDir = new File(Constants.XTABLE_PATH);
//
//            if (!xTableDir.exists()) {
//                xTableDir.mkdirs();
//            }
//
//            for (File file : xTableDir.listFiles()) {
//                if (file.getName().endsWith(".txt")) {
//                    Integer typeID = Integer.valueOf(file.getName().substring(0,
//                            file.getName().indexOf(".txt")));
//
//                    int[] keys = Files.lines(Paths.get(".")
//                            .resolve(Constants.XTABLE_PATH + file.getName()))
//                            .map(Integer::valueOf).mapToInt(Integer::intValue).toArray();
//
//                    tables.put(typeID, keys);
//                }
//            }
//            initializeFromJson(new File(Constants.XTEA_JSON_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void touch() {
    }

    public static void initializeFromJson(File xteaJsonFile) {
        final Type type = new TypeToken<List<XTEAEntry>>() {
        }.getType();
        final Gson gson = new Gson();
        final JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(xteaJsonFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        final List<XTEAEntry> entries = gson.fromJson(reader, type);
        for (XTEAEntry entry : entries) {
            maps.put(entry.regionId, entry.key);
            tables.put(entry.regionId, entry.key);
        }
    }

    static class XTEAEntry {
        @SerializedName("mapsquare") int regionId;
        @SerializedName("key") int[] key;
    }
}
