package netty.packets.responseData;

public class ResponseData {
    private int intValue;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    // standard getters and setters
    public String toString() {
        return "response: "+Integer.toString(intValue);
    }
}