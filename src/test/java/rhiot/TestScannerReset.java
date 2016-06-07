package rhiot;

import org.jboss.rhiot.ble.bluez.AdEventInfo;
import org.jboss.rhiot.ble.bluez.HCIDump;
import org.jboss.rhiot.ble.bluez.IAdvertEventCallback;
import org.jboss.rhiot.ble.bluez.RHIoTTag;

import java.nio.ByteOrder;
import java.util.Date;

/**
 * Test continually calling HCIDump.freeScanner/initScanner in a separate thread to validate the scanner
 * can be reinitialized and still receive events on origial callback.
 */
public class TestScannerReset implements IAdvertEventCallback {
    static volatile boolean running = true;
    static volatile String hciDev = "hci0";

    @Override
    public boolean advertEvent(AdEventInfo info) {
        System.out.printf("+++ advertEvent, rssi=%d, time=%s\n", info.getRssi(), new Date(info.getTime()));
        RHIoTTag tag = RHIoTTag.create(info);
        if(tag != null) {
            System.out.printf("%s\n", tag.toFullString());
        }
        return false;
    }

    static void resetMonkey() {
        while (running) {
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
            }
            System.out.printf("\n\n++++++++++ Resetting scanner...");
            HCIDump.freeScanner();
            System.out.printf("++++++++++ freed, initing...\n\n");
            HCIDump.initScanner(hciDev, 512, ByteOrder.BIG_ENDIAN);
            System.out.printf("++++++++++done\n\n");
        }
    }

    public static void main(String[] args) {
        int device = 0;
        if(args.length > 0)
            device = Integer.parseInt(args[0]);
        TestRHIoTTag test = new TestRHIoTTag();

        try {
            // Load the native library
            HCIDump.loadLibrary();

            hciDev = "hci" + device;
            //HCIDump.enableDebugMode(true);
            HCIDump.setAdvertEventCallback(test);
            HCIDump.initScanner(hciDev, 512, ByteOrder.BIG_ENDIAN);
            // Start a reset thread
            final Thread resetThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    resetMonkey();
                }
            }, "ResetThread");
            resetThread.setDaemon(true);
            resetThread.start();

            // Let the scanner event thread run
            long eventCount = 1;
            while (running) {
                Thread.sleep(10);
                if(eventCount % 1000 == 0)
                    System.out.printf("event count=%d\n", eventCount);
            }
            HCIDump.freeScanner();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
