package com.tessera.engine.server.loot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tessera.engine.server.loot.animalFeed.AnimalFeedLootRegistry;
import com.tessera.engine.server.loot.block.BlockLootRegistry;
import com.tessera.engine.server.loot.output.Loot;
import com.tessera.engine.common.json.fasterXML.loot.LootDeserializer;
import com.tessera.engine.common.json.fasterXML.loot.LootSerializer;

public class AllLootTables {
    public static BlockLootRegistry blockLootTables = new BlockLootRegistry();
    public static AnimalFeedLootRegistry animalFeedLootTables = new AnimalFeedLootRegistry();

    //JSON serializer and deserializer
    public static final ObjectMapper lootMapper;

    static {
        lootMapper = new ObjectMapper();

        //Custom serializers and deserializers
        SimpleModule module = new SimpleModule();
        module.addSerializer(Loot.class, new LootSerializer());
        module.addDeserializer(Loot.class, new LootDeserializer());
        lootMapper.registerModule(module);

        //https://stackoverflow.com/questions/6371092/can-not-find-a-map-key-deserializer-for-type-simple-type-class
        //https://stackoverflow.com/questions/11246748/deserializing-non-string-map-keys-with-jackson
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addKeyDeserializer(AnimalFeedLoot.class, new KeyDeserializer());
//        lootMapper.registerModule(simpleModule);

//        SimpleModule module1 = new SimpleModule();
//        module1.addSerializer(AnimalFeedInput.class, new Serializer_AnimalFeedInput());
//        module1.addDeserializer(AnimalFeedInput.class, new Deserializer_AnimalFeedInput());
//        lootMapper.registerModule(module1);
    }
}
