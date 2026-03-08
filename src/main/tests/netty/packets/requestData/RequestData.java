package netty.packets.requestData;

public class RequestData {
    private int intValue;
    private String stringValue;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    // standard getters and setters
    public String toString() {
        return "request: intValue: " + intValue + ", stringValue: " + stringValue;
    }
}