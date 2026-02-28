package com.tessera.content.vanilla.items;

import com.tessera.Main;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.item.Item;

public class FlintAndSteel extends Item {
    public FlintAndSteel() {
        super("tessera:flint_and_steel", "Flint and Steel");
        setIcon("flint_and_steel.png");
        maxDurability = 200;
        createClickEvent = (ray, stack) -> {
            Main.getServer().setBlock(
                    Registrys.getBlock("tessera:fire").id,
                    ray.getHitPosPlusNormal().x,
                    ray.getHitPosPlusNormal().y,
                    ray.getHitPosPlusNormal().z);
            stack.durability -= 0.2f;
            return true;
        };

        destroyClickEvent = createClickEvent;
    }


}
