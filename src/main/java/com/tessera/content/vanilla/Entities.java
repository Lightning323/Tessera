package com.tessera.content.vanilla;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.content.vanilla.entities.Banner;
import com.tessera.content.vanilla.entities.animal.Cat;
import com.tessera.content.vanilla.entities.animal.Fox;
import com.tessera.content.vanilla.entities.animal.Rabbit;
import com.tessera.content.vanilla.entities.animal.fish.FishA;
import com.tessera.content.vanilla.entities.animal.fish.FishB;
import com.tessera.content.vanilla.entities.animal.landAndWater.Beaver;
import com.tessera.content.vanilla.entities.animal.landAndWater.Turtle;
import com.tessera.content.vanilla.entities.animal.quadPedal.Dog;
import com.tessera.content.vanilla.entities.animal.quadPedal.Horse;
import com.tessera.content.vanilla.entities.animal.quadPedal.Mule;
import com.tessera.content.vanilla.entities.vehicle.Boat;
import com.tessera.content.vanilla.entities.vehicle.Minecart;

import java.util.ArrayList;
import java.util.Arrays;

public class Entities {

    public static ArrayList<EntitySupplier> startup_getEntities(ClientWindow window) {

        EntitySupplier[] entityArray = new EntitySupplier[]{
                //Banners
                new EntitySupplier("tessera:red_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "red")),
                new EntitySupplier("tessera:orange_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "orange")),
                new EntitySupplier("tessera:yellow_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "yellow")),
                new EntitySupplier("tessera:lime_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "lime")),
                new EntitySupplier("tessera:green_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "green")),
                new EntitySupplier("tessera:blue_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "blue")),
                new EntitySupplier("tessera:gray_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "gray")),
                new EntitySupplier("tessera:pink_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "pink")),
                new EntitySupplier("tessera:purple_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "purple")),
                new EntitySupplier("tessera:white_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "white")),
                new EntitySupplier("tessera:tessera_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "blue_logo")),
                new EntitySupplier("tessera:regal_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "regal")),
                new EntitySupplier("tessera:royal_banner", (uniqueIdentifier) -> new Banner(uniqueIdentifier, "royal")),


                //Boats
                new EntitySupplier("tessera:oak_boat", (uniqueIdentifier) -> new Boat(window, uniqueIdentifier, "boat_oak")),
                new EntitySupplier("tessera:dark_oak_boat", (uniqueIdentifier) -> new Boat(window, uniqueIdentifier, "boat_darkoak")),
                new EntitySupplier("tessera:spruce_boat", (uniqueIdentifier) -> new Boat(window, uniqueIdentifier, "boat_spruce")),
                new EntitySupplier("tessera:acacia_boat", (uniqueIdentifier) -> new Boat(window, uniqueIdentifier, "boat_acacia")),
                new EntitySupplier("tessera:jungle_boat", (uniqueIdentifier) -> new Boat(window, uniqueIdentifier, "boat_jungle")),
                new EntitySupplier("tessera:birch_boat", (uniqueIdentifier) -> new Boat(window, uniqueIdentifier, "boat_birch")),

                //Minecarts
                new EntitySupplier("tessera:blue_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "blue")),
                new EntitySupplier("tessera:charcoal_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "charcoal")),
                new EntitySupplier("tessera:cyan_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "cyan")),
                new EntitySupplier("tessera:green_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "green")),
                new EntitySupplier("tessera:iron_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "iron")),
                new EntitySupplier("tessera:red_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "red")),
                new EntitySupplier("tessera:yellow_minecart", (uniqueIdentifier) -> new Minecart(window, uniqueIdentifier, "yellow")),

                //Foxes
                new EntitySupplier("tessera:fox", (uniqueIdentifier) -> new Fox(uniqueIdentifier, window)),
                //Cats
                new EntitySupplier("tessera:cat", (uniqueIdentifier) -> new Cat(uniqueIdentifier, window)),
                //Rabbits
                new EntitySupplier("tessera:rabbit", (uniqueIdentifier) -> new Rabbit(window, uniqueIdentifier)),

                //Horses
                new EntitySupplier("tessera:horse", (uniqueIdentifier) -> new Horse(uniqueIdentifier, window)),
                //Mules
                new EntitySupplier("tessera:mule", (uniqueIdentifier) -> new Mule(uniqueIdentifier, window)),

                //Dogs
                new EntitySupplier("tessera:dog", (uniqueIdentifier) -> new Dog(uniqueIdentifier, window)),

                //Turtles
                new EntitySupplier("tessera:sea_turtle", (uniqueIdentifier) -> new Turtle(uniqueIdentifier, window)),

                //Beavers
                new EntitySupplier("tessera:beaver", (uniqueIdentifier) -> new Beaver(uniqueIdentifier, window)),

                //Fish
                new EntitySupplier("tessera:angler_fish", (uniqueIdentifier) -> new FishA(uniqueIdentifier, window)),
                new EntitySupplier("tessera:butterfly_fish", (uniqueIdentifier) -> new FishB(uniqueIdentifier, window))
        };


        ArrayList<EntitySupplier> entityList = new ArrayList<>();
        entityList.addAll(Arrays.asList(entityArray));
        return entityList;
    }
}
