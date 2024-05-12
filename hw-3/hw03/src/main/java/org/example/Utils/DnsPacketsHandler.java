package org.example.Utils;

import org.example.Common.Constants;
import org.pcap4j.packet.DnsPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;

public class DnsPacketsHandler {
    public static DnsPacket processDnsPacket(Packet packet) {
        if (packet.contains(UdpPacket.class)) {
            var udpPacket = packet.get(UdpPacket.class);

            if (udpPacket.getHeader().getDstPort().valueAsInt() == Constants.DNS_PORT) {
                var dnsPacket = udpPacket.get(DnsPacket.class);

                // System.out.println(dnsPacket);
                return dnsPacket;
            }
        }

        return null;
    }
}
