package com.tessera.engine.server.recipes;

import com.tessera.engine.server.item.Item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TagPossibilities extends HashMap<String, List<Item>> {

    public TagPossibilities(String... input) {
        //Make the tag possibilities
        for (String input_element : input) {
            if (input_element != null &&
                    input_element.startsWith(AllRecipes.TAG_PREFIX) &&
                    !containsKey(input_element))
                put(input_element, AllRecipes.getMatchingItems(input_element));
        }
    }

    public void removeElementOfTags(HashSet<String> tags, int index) {
        for (String tag : tags) {
            if (containsKey(tag) && !get(tag).isEmpty()) {
                get(tag).remove(index);
            }
        }
    }
}
