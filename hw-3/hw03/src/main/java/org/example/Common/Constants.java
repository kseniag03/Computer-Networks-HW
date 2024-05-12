package org.example.Common;

import org.pcap4j.util.MacAddress;

public class Constants {
    public static final String MY_IP_ADDRESS = "192.168.1.148";
    public static final MacAddress MY_MAC_ADDRESS = MacAddress.getByName("3c:22:fb:1c:b8:79");
    public static final int DEFAULT_SNAPLEN = 65536;
    public static final int DEFAULT_TIMEOUT = 100000; // in milliseconds
    public static final String BROADCAST_ADDRESS = "ff:ff:ff:ff:ff:ff";
    public static final MacAddress BROADCAST_MAC_ADDRESS = MacAddress.getByName(BROADCAST_ADDRESS);
    public static final MacAddress DEFAULT_MAC_ADDRESS = MacAddress.getByAddress(new byte[MacAddress.SIZE_IN_BYTES]);
    public static final int DNS_PORT = 53;
    public static final int DNS_PACKETS_COUNT = 20;
    public static final int DNS_HEADER_SIZE = 8;
    public static final int UDP_HEADER_SIZE = 20;
    public static final int UDP_PORT = 12345;
    public static final String MY_DNS_IP_ADDRESS = "192.168.1.1";
    public static final MacAddress MY_DNS_MAC_ADDRESS = MacAddress.getByName("f8:f0:82:f8:f8:52"); // got from wireshark, dns protocol
}
