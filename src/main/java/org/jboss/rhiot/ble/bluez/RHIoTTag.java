package org.jboss.rhiot.ble.bluez;

import org.jboss.rhiot.ble.GAP_UUIDs;

import java.nio.ByteBuffer;

/**
 * The RHIoT Tag object interface that extracts the tag's sensor information from the BLE AdEventInfo.
 */
public class RHIoTTag {
    /** The ServiceData 16 bit service id used for the tag data structure */
    public static byte[] SERVICE_DATA_PREFIX = {(byte)0xAA, (byte)0xFE, 0x20};

    public enum KeyState {
        NONE, LEFT, RIGHT, LEFT_AND_RIGHT, REED
    }

    /**
     * A factory method that builds a RHIoTTag from BLE advertising events that contain the expected ServiceData.
     *
     * @param info - the BLE advertising event info
     * @return the newly created RHIoTTag if the event info contains the correct ServiceData, null if it does not.
     */
    public static RHIoTTag create(AdEventInfo info) {
        RHIoTTag tag = null;
        AdStructure tagData = info.getADSOfType(GAP_UUIDs.ServiceData);
        if(tagData != null) {
            if (tagData.startsWith(SERVICE_DATA_PREFIX)) {
                tag = new RHIoTTag(tagData.getData());
                tag.setAddress(info.bdaddr);
            }
        }
        return tag;
    }

    /**
     * Return the KeyState enum for the given mask
     * @param mask - bit mask for the button states on the sensor
     * @return KeyState enum for the given mask
     */
    public static KeyState keyStateForMask(int mask) {
        KeyState keyState = KeyState.NONE;
        if((mask & 0b01) != 0 && (mask & 0b010) != 0)
            keyState = KeyState.LEFT_AND_RIGHT;
        else if((mask & 0b01) != 0)
            keyState = KeyState.LEFT;
        else if((mask & 0b010) != 0)
            keyState = KeyState.RIGHT;
        else if((mask & 0b0100) != 0)
            keyState = KeyState.REED;
        return keyState;
    }

    /**
     * Get the byte[] representation of the string address
     * @param strAddress - string form of the BLE address as given by getAddressString
     * @return byte[] representation of the string address
     */
    public static byte[] fromStringAddress(String strAddress) {
        byte[] address = new byte[6];
        String[] parts = strAddress.split(":");
        // byte[] form is stored in reverse order
        for (int i = 0, n = parts.length-1; n >= 0; n --, i ++) {
            address[i] = (byte) Integer.parseInt(parts[n], 16);
        }
        return address;
    }

    /** Battery Voltage in mV */
    private short vBatt;
    /** Temperature in C */
    private double tempC;
    /** Advertisment packet count since power-up/reboot */
    private int advCnt;
    /** Time since power-up/reboot in 0.1 second resolution */
    private int secCnt;
    /** Bit 0: left key (user button), Bit 1: right key (power button), Bit 2: reed relay */
    private byte keys;
    /** raw optical sensor data reading */
    private int lux;
    /** The BLE address for the sending tag */
    private byte[] address;
    /** An optional name associated with the tag */
    private String name;

    /**
     * Create a tag from minimal information
     * @param adddress - BLE address for tag
     * @param keys - key mask reading
     * @param lux - light sensor reading
     */
    public RHIoTTag(String adddress, byte keys, int lux) {
        this.address = fromStringAddress(adddress);
        this.keys = keys;
        this.lux = lux;
    }

    /**
     * Create an object from the data from the AdStructure with the tags ServiceData value. The data array
     * represents the following encoded structure information:
     uint8_t   frameType;      // TLM
     uint8_t   version;        // 0x00 for now
     uint8_t   vBatt[2];       // Battery Voltage, 1mV/bit, Big Endian
     uint8_t   temp[2];        // Temperature. Signed 8.8 fixed point
     uint8_t   advCnt[4];      // Adv count since power-up/reboot
     uint8_t   secCnt[4];      // Time since power-up/reboot in 0.1 second resolution
     // Non-standard TLM data
     uint8_t   keys;           //  Bit 0: left key (user button), Bit 1: right key (power button), Bit 2: reed relay
     uint8_t   lux[2];         // raw optical sensor data, BE

     * @param data - the data from the AdStructure of type ServiceData(0x16).
     */
    public RHIoTTag(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // Skip the service 0xAAFE id 2 bytes
        buffer.get();
        buffer.get();
        // Skip frameType
        buffer.get();
        // Skip version
        buffer.get();
        // battery
        vBatt = buffer.getShort();
        byte[] temp = {buffer.get(), buffer.get()};
        tempC = temp[0] + temp[1] / 256.0;
        advCnt = buffer.getInt();
        secCnt = buffer.getInt();
        keys = buffer.get();
        // Convert unsigned short to int
        byte highByteLux = buffer.get();
        byte lowByteLux = buffer.get();
        lux = ((highByteLux & 0x0ff) << 8) + (lowByteLux & 0x0ff);
    }

    public String keysString() {
        String keysStr = "";
        if ((keys & 0x1) != 0)
            keysStr += "Left|";
        if ((keys & 0x2) != 0)
            keysStr += "Right|";
        if ((keys & 0x4) != 0)
            keysStr += "Reed";
        return keysStr;
    }

    /**
     * @return the battery voltage in mV
     */
    public short getvBatt() {
        return vBatt;
    }

    /**
     * @return the sensor temperature in degrees C
     */
    public double getTempC() {
        return tempC;
    }

    /**
     * @return The number of advertising packets sent out since startup
     */
    public int getAdvCnt() {
        return advCnt;
    }

    /**
     * @return The time since power up in tenths of a second
     */
    public int getSecCnt() {
        return secCnt;
    }

    public String getTimeUpString() {
        int days = secCnt / (36000 * 24);
        int remainder = secCnt - days * 36000 * 24;
        int hours = remainder / 36000;
        remainder = remainder - hours * 36000;
        int minutes = remainder / 600;
        remainder = remainder - minutes * 600;
        int secs = remainder / 10;
        return String.format("%d days, %d hours, %d mins, %d secs", days, hours, minutes, secs);
    }

    /**
     * Bit 0: left key (user button), Bit 1: right key (power button), Bit 2: reed relay
     * @return the key state bit map.
     */
    public byte getKeys() {
        return keys;
    }

    /**
     * Bit 0: left key (user button), Bit 1: right key (power button), Bit 2: reed relay
     * @return button and reed press state
     */
    public KeyState getKeyState() {
        return keyStateForMask(keys);
    }

    /**
     * @return the raw light sensor reading
     */
    public int getLux() {
        return lux;
    }

    /**
     * See if the raw light sensor reading is above the given value.
     * @param rawLuxValue raw lux sensor value to test
     * @return true if the raw light sensor reading is above the given value, false otherwise
     */
    public boolean isLightSensorAbove(int rawLuxValue) {
        return lux > rawLuxValue;
    }

    /**
     * @return The BLE address of the sending tag if set
     */
    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    /**
     * @return The tag BLE address as a colon separate string of the hex values
     */
    public String getAddressString() {
        StringBuilder tmp = new StringBuilder("");
        if(address != null) {
            for(int n = address.length-1; n >= 0; n --) {
                tmp.append(String.format("%02X:", address[n]));
            }
            tmp.setLength(tmp.length()-1);
        } else {
            tmp.append(":::::");
        }
        return tmp.toString();
    }

    /**
     * The optional assigned name
     * @return possibly null name assigned to the tag
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name to assign to the tag
     * @param name - name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toFullString() {
        StringBuilder tmp = new StringBuilder("RHIoTTag(");
        tmp.append(getAddressString());
        if(name != null) {
            tmp.append('/');
            tmp.append(name);
        }
        tmp.append(')');
        tmp.append(String.format(": battery: %dmV", vBatt));
        tmp.append(String.format(", temp: %.2fC", tempC));
        tmp.append(String.format(", keys: %s", keysString()));
        tmp.append(String.format(", lux: %d raw", lux));
        tmp.append(String.format(", advertCnt: %d", advCnt));
        tmp.append(String.format(", timeUp: %s", getTimeUpString()));

        return tmp.toString();
    }

    public String toString() {
        StringBuilder tmp = new StringBuilder("RHIoTTag(");
        tmp.append(getAddressString());
        if(name != null) {
            tmp.append('/');
            tmp.append(name);
        }
        tmp.append(')');
        return tmp.toString();
    }
}
