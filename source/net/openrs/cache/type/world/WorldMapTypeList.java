package net.openrs.cache.type.world;

/*
public class WorldMapTypeList implements TypeList<AreaType> {

    private Logger logger = Logger.getLogger(WorldMapTypeList.class.getName());

    private WorldMapDefinition[] worldmaps;

    @Override
    public void initialize(Cache cache) {
        int count = 0;
        try {
            ReferenceTable table = cache.getReferenceTable(CacheIndex.CONFIGS);
            Entry entry = table.getEntry(ConfigArchive.WORLDMAP);
            Archive archive = Archive.decode(cache.read(CacheIndex.CONFIGS, ConfigArchive.WORLDMAP).getData(), entry.size());


            worldmaps = new WorldMapDefinition[][entry.capacity()];
            for (int id = 0; id < entry.capacity(); id++) {
                ChildEntry child = entry.getEntry(id);
                if (child == null)
                    continue;

                ByteBuffer buffer = archive.getEntry(child.index());
                WorldMapDefinition type = new AreaType(id);
                type.decode(buffer);
                worldmaps[id] = type;
                count++;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error Loading AreaType(s)!", e);
        }
        logger.info("Loaded " + count + " AreaType(s)!");
    }

    @Override
    public WorldMapDefinition list(int id) {
//        Preconditions.checkArgument(id >= 0, "ID can't be negative!");
//        Preconditions.checkArgument(id < areas.length, "ID can't be greater than the max area id!");
        return worldmaps[id];
    }

    @Override
    public void print() {

        File dir = new File(Constants.TYPE_PATH);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(Constants.TYPE_PATH, "areas.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            Arrays.stream(worldmaps).filter(Objects::nonNull).forEach((WorldMapDefinition t) -> {
                TypePrinter.print(t, writer);
            });
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int size() {
        return worldmaps.length;
    }

}
*/