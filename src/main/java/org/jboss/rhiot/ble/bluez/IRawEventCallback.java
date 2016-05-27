package org.jboss.rhiot.ble.bluez;

import java.nio.ByteBuffer;

/**
 * A raw ble event callback that passes in the native byte[] array on the native thread.
 */
@FunctionalInterface
public interface IRawEventCallback {
    /**
     * Callback on the native parser thread with the shared byte[] array associated with the direct
     * ByteBuffer associated with the GetDirectBufferAddress call.
     * @param beaconInfo the ble info read only bytebuffer, must be copied as it would be modified on the next ble
     *                   event from the bluetooth stack.
     */
    public boolean beaconEvent(ByteBuffer beaconInfo);
}
