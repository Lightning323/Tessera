package com.tessera.engine.server.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.tessera.engine.common.json.fasterXML.itemStack.ItemStackDeserializer;
import com.tessera.engine.common.json.fasterXML.itemStack.ItemStackSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StorageSpace {
    //Items needs to be private so that we can properly serverExecute changes
    private final ItemStack[] list;

    private final static Object changeEventLock = new Object();
    private static boolean isChangeEventRunning = false; //To prevent infinite recursion if we use set method within changeEvent
    public final static ObjectMapper binaryJsonMapper;
    public final static TypeReference<ItemStack[]> itemStackRef = new TypeReference<ItemStack[]>() {
    };

    static {
        SmileFactory smileFactory = new SmileFactory();
        //set flags
        smileFactory.enable(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT);
        smileFactory.enable(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES);

        //create new object mapper based on factory
        binaryJsonMapper = new ObjectMapper(smileFactory);
        // Create a module to register custom serializer and deserializer
        SimpleModule module = new SimpleModule();
        module.addSerializer(ItemStack.class, new ItemStackSerializer()); // Register the custom serializer
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer()); // Register the custom deserializer
        binaryJsonMapper.registerModule(module);
    }

    public void loadFromJson(byte[] json) throws IOException {
        ItemStack[] deserializedObject = StorageSpace.binaryJsonMapper.readValue(json, StorageSpace.itemStackRef);
        clear();
        for (int i = 0; i < deserializedObject.length; i++) {
            set(i, deserializedObject[i]);
        }
    }

    //TODO: Add write/read json methods but with JsonObject to parse it with other things in a json file

    public byte[] writeToJson() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StorageSpace.binaryJsonMapper.writeValue(baos, getList());
        return baos.toByteArray();
    }

    public ItemStack[] getList() {
        return list;
    }

    public Runnable changeEvent;

    public void changeEvent() {
        synchronized (changeEventLock) {
            isChangeEventRunning = true;
            deleteEmptyItems();
            if (changeEvent != null) changeEvent.run();
            isChangeEventRunning = false;
        }
    }

    public StorageSpace(int size) {
        list = new ItemStack[size];
    }

    public boolean hasRoomForItem(ItemStack stack) {
        if (stack == null) return false;
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null && list[i].item.equals(stack.item)
                    && list[i].stackSize + stack.stackSize <= list[i].item.maxStackSize) {
                return true;
            }
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i] == null) {
                return true;
            }
        }
        return false;
    }

    public int acquireItem(ItemStack stack) {
        return acquireItem(stack, 0);
    }

    public int acquireItem(ItemStack stack, int start) {
        if (start >= list.length) start = 0;

        int index = attemptToAdd(stack, start);
        if (index == -1 && start > 0) attemptToAdd(stack, 0);

        changeEvent();
        return -1;
    }

    private int attemptToAdd(ItemStack stack, int start) {
        for (int i = start; i < list.length; i++) {
            if (list[i] != null && list[i].item.equals(stack.item)
                    && list[i].stackSize + stack.stackSize <= list[i].item.maxStackSize) {
                list[i].stackSize += stack.stackSize;
                return i;
            }
        }
        for (int i = start; i < list.length; i++) {
            if (list[i] == null) {
                list[i] = stack;
                return i;
            }
        }
        return -1;
    }


    public ItemStack get(int index) {
        return list[index];
    }

    public void set(int index, ItemStack item) {
        list[index] = item;
        if (!isChangeEventRunning) changeEvent();
    }

    public int size() {
        return list.length;
    }

    public void organize() {
        sortItems();
        boolean hasMerged = true;
        while (hasMerged) {
            hasMerged = false;
            for (int i = 0; i < list.length - 1; i++) {
                if (list[i] != null && list[i + 1] != null && list[i].item.equals(list[i + 1].item)) {
                    list[i].stackSize += list[i + 1].stackSize;


                    if (list[i].stackSize > list[i].item.maxStackSize) {
                        // Handle overflow by keeping the excess in the next slot
                        list[i + 1].stackSize = list[i].stackSize - list[i].item.maxStackSize;
                        list[i].stackSize = list[i].item.maxStackSize;
                    } else {
                        list[i + 1] = null; // Fully combined, clear the next slot
                        hasMerged = true; // Mark as combined so we continue looping
                    }
                }
            }
            sortItems(); // Bring non-null items to the beginning
        }
        changeEvent();
    }

    // Move non-null items to the start of the array
    private void sortItems() {
        // Sort items based on the custom comparator
        Arrays.sort(list, (item1, item2) -> {
            if (item1 == null && item2 == null) return 0;
            if (item1 == null) return 1;
            if (item2 == null) return -1;
            return item1.item.compareTo(item2.item);
        });
    }


    public void clear() {
        for (int i = 0; i < size(); i++) {
            list[i] = null;
        }
    }

    public void deleteEmptyItems() {
        Set<ItemStack> seenItems = new HashSet<>();

        for (int i = 0; i < size(); i++) {
            ItemStack currentItem = list[i];

            if (currentItem != null) {
                // Remove empty or marked-for-destruction items
                if (currentItem.stackSize <= 0
                        || currentItem.destroy
                        || (currentItem.durability == 0 && currentItem.item.maxDurability > 0)) {
                    System.out.println("Removing empty item at index " + i + " Durability: " + currentItem.durability + " Max Durability: " + currentItem.item.maxDurability);
                    list[i] = null;
                    continue;
                }

                // Check for duplicate references (same object in memory)
                boolean isDuplicate = false;
                for (ItemStack item : seenItems) {
                    if (item == currentItem) { // Reference equality
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    list[i] = null; // Remove duplicate reference
                } else {
                    seenItems.add(currentItem); // Track unique reference
                }
            }
        }
    }

    public boolean isEmpty() {
        return false;
    }


}
