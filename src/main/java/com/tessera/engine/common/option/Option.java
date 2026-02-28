package com.tessera.engine.common.option;

public class Option {
    BoundedFloat boundedFloat = null;
    BoundedInt boundedInt = null;
    String string = null;
    boolean booleanValue = false;

    public Option(BoundedFloat boundedFloat) {
        this.boundedFloat = boundedFloat;
    }

    public Option(BoundedInt boundedInt) {
        this.boundedInt = boundedInt;
    }

    public Option(String string) {
        this.string = string;
    }

    public Option(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public BoundedFloat getBoundedFloat() {
        return boundedFloat;
    }

    public BoundedInt getBoundedInt() {
        return boundedInt;
    }

    public String getString() {
        return string;
    }

    public boolean getBoolean() {
        return booleanValue;
    }

    public void setBoundedFloat(BoundedFloat boundedFloat) {
        this.boundedFloat = boundedFloat;
    }

    public void setBoundedInt(BoundedInt boundedInt) {
        this.boundedInt = boundedInt;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setBoolean(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
