package org.jboss.rhiot.ble.bluez;

import org.jboss.rhiot.ble.common.Beacon;

/**
 * Higher level callback that unwraps the raw native ble event into a Beacon object
 */
@FunctionalInterface
public interface IEventCallback {
    public boolean beaconEvent(Beacon beacon);
}
