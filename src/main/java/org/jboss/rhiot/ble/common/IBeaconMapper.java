package org.jboss.rhiot.ble.common;

/**
 * Interface for mapping from the ble minor id to a registered user.
 */
@FunctionalInterface
public interface IBeaconMapper {
    public String lookupUser(int minorID);
}
