package rhiot;

import org.jboss.rhiot.ble.bluez.RHIoTTag;

/**
 * Created by sstark on 6/10/16.
 */
public class TestKeyState {
    static void printKeyState(int keys) {
        RHIoTTag.KeyState keyState = RHIoTTag.KeyState.NONE;
        if((keys & 0b01) != 0 && (keys & 0b010) != 0)
            keyState = RHIoTTag.KeyState.LEFT_AND_RIGHT;
        else if((keys & 0b01) != 0)
            keyState = RHIoTTag.KeyState.LEFT;
        else if((keys & 0b010) != 0)
            keyState = RHIoTTag.KeyState.RIGHT;
        else if((keys & 0b0100) != 0)
            keyState = RHIoTTag.KeyState.REED;
        System.out.printf("keyState(%d) = %s\n", keys, keyState);
    }

    public static void main(String[] args) {
        printKeyState(1);
        printKeyState(2);
        printKeyState(4);
        printKeyState(3);
    }
}
