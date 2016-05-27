package org.jboss.rhiot.ble.bluez;

/**
 * The general scanner callback for every BLE advertising event
 */
public interface IAdvertEventCallback {

    /**
     * Notification of a BLE advertising event
     * @param info - BLE advertising event information
     * @return true if scanning should stop, false to continue
     */
    public boolean advertEvent(AdEventInfo info);
}
