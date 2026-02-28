package com.tessera.engine.server.item;

public class ItemStack {
    public static final byte MAX_STACK_SIZE = 64;

    public Item item;
    public int stackSize;
    public float durability;//The amount of damage the item has left
    public byte[] nbtData;//The NBT data of the item
    protected boolean destroy = false;

    public void destroy() {
        this.destroy = true;
    }

    public ItemStack(Item item) {
        this.item = item; //We must be able to guarantee that the item is not null
        if (this.item == null) throw new NullPointerException("Item in ItemStack cannot be null");

        durability = item.maxDurability;
        this.stackSize = 1;

    }

    public ItemStack(Item item, int stackSize) {
        this.item = item;
        if (this.item == null) throw new NullPointerException("Item in ItemStack cannot be null");

        this.durability = item.maxDurability; //We set the durability to the max durability
        this.stackSize = Math.min(item.maxStackSize, stackSize);
    }


    @Override
    public String toString() {
        return "ItemStack{" +
                "item=" + item.id +
                ", stackSize=" + stackSize +
                ", durability=" + durability +
                (nbtData != null ? ", nbtData=\"" + new String(nbtData) + "\"" : "") +
                '}';
    }
}
