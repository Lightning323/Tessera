package com.tessera.utils;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Used as a more efficient way to store items than a hashmap.
 * With hashmaps, there is a small memory overhead when getting an item. Assuming there are very few gaps in the values,
 * you can treat the map as an array.
 *
 * @param <T>
 */
public class IntMap<T> {
    private T[] list;
    private final Class<T> type;

    public IntMap(Class<T> type) {
        list = (T[]) Array.newInstance(type, 0);
        this.type = type;
    }

    public T get(int id) {
        try {
            return list[id - lowestId];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void clear() {
        list = null;
    }

    int highestId;
    int lowestId;

//    public void put(int id, T item) {
//        if (list == null) {
//            list = (T[]) Array.newInstance(typeReference, Math.abs(lowestId - highestId) + 1);
//        }
//        int indx = id - lowestId;
//        list[indx] = item;
//    }

    public void setList(HashMap<Integer, T> map) {
        //Get the highest key
        highestId = Integer.MIN_VALUE;
        for (int id : map.keySet()) {
            if (id > highestId) {
                highestId = id;
            }
        }

        //Get the lowest key
        lowestId = highestId;
        for (int id : map.keySet()) {
            if (id < lowestId) {
                lowestId = id;
            }
        }
//System.out.println("Highest ID: " + highestId + " Lowest ID: " + lowestId);

        list = (T[]) Array.newInstance(type, Math.abs(lowestId - highestId) + 1);

        for (int id = lowestId; id <= highestId; id++) {
            if (map.containsKey(id)) {
                int indx = id - lowestId;
//                if(indx<10)System.out.println("Adding ID: " + map.get(id) + " id=" + id + " indx=" + indx);
                list[indx] = map.get(id);
            }
        }
    }
}
