package org.example.Utils.Dns.Stub;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.MacAddress;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsSender {
    private static final String[] dnsAddresses = {
            "example.com",
            "yandex.ru",
            "google.com",
            "hse.ru",
            "edu.hse.ru",
            "amazon.com",
            "jetbrains.com",
            "microsoft.com",
            "mail.ru",
            "draw.io",
            "cnn.com"
    };

    public static void sendDnsRequest(PcapHandle handle, String destinationMacAddress, String sourceMacAddress,
                                       String sourceIpAddress, String destinationIpAddress)
            throws PcapNativeException, NotOpenException, UnknownHostException {

        for (var address : dnsAddresses) {
            var dnsData = createDnsRequest(address);

            var etherBuilder = new EthernetPacket.Builder();

            etherBuilder.dstAddr(MacAddress.getByName(destinationMacAddress))
                    .srcAddr(MacAddress.getByName(sourceMacAddress))
                    .type(EtherType.IPV4);

            var ipv4Builder = new IpV4Packet.Builder();

            ipv4Builder.version(IpVersion.IPV4)
                    .tos(IpV4Rfc791Tos.newInstance((byte) 0))
                    .ttl((byte) 100)
                    .protocol(IpNumber.UDP)
                    .srcAddr((Inet4Address) InetAddress.getByName(sourceIpAddress))
                    .dstAddr((Inet4Address) InetAddress.getByName(destinationIpAddress));

            var udpBuilder = new UdpPacket.Builder();

            udpBuilder.srcPort(UdpPort.getInstance((short) 12345))
                    .dstPort(UdpPort.getInstance((short) 53))
                    .payloadBuilder(new UnknownPacket.Builder().rawData(dnsData));

            var packet = etherBuilder.payloadBuilder(ipv4Builder.payloadBuilder(udpBuilder)).build();

            handle.sendPacket(packet);
        }

    }

    private static byte[] createDnsRequest(String domain) {
        try {
            return DnsRequestCreator.createDnsRequest(domain);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
