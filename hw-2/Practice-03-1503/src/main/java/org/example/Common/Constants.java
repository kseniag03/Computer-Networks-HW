package org.example.Common;

import org.pcap4j.util.MacAddress;

public class Constants {
    public static final int DEFAULT_TIMEOUT = 100000; // in milliseconds
    public static final String BROADCAST_ADDRESS = "ff:ff:ff:ff:ff:ff";
    public static final MacAddress BROADCAST_MAC_ADDRESS = MacAddress.getByName(BROADCAST_ADDRESS);
    public static final MacAddress DEFAULT_MAC_ADDRESS = MacAddress.getByAddress(new byte[MacAddress.SIZE_IN_BYTES]);
}
