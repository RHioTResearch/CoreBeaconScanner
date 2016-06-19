package rhiot;

import org.jboss.rhiot.ble.bluez.RHIoTTag;

public class TestFromAddress {
    public static void main(String[] args) {
        byte[] address = RHIoTTag.fromStringAddress("B0:B4:48:D6:DA:85");
        for(int n = 0; n < address.length; n ++) {
            System.out.printf("addr[%d] = %02X\n", n, address[n]);
        }
    }
}
