package com.tessera.utils.BFS;

public class TravelNode {

    public int x, y, z;
    public int travel = 0;

    public TravelNode(final int x, final int y, final int z, final int travel) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.travel = travel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelNode that = (TravelNode) o;
        return x == that.x && y == that.y && z == that.z && travel == that.travel;
    }

    @Override
    public int hashCode() {
        return (x ^ (x >> 32) ^ y ^ (y >> 32) ^ z ^ (z >> 32));//Generating efficient hashcode is CRUCIAL!
    }
}
