package org.example.Utils.Dns;

import org.example.Common.Constants;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;

public class RootServerHandler {
    private static final Map<String, String> rootServers = new HashMap<>();
    private final Map<Integer, String> sentDnsQueries = new HashMap<>();
    private final Random random = new Random();
    private final PcapHandle handle;
    private final List<String> answers;

    static {
        rootServers.put("root-a", "199.7.83.42");
        rootServers.put("root-b", "192.5.5.241");
        rootServers.put("root-c", "193.0.14.129");
        rootServers.put("root-d", "192.203.230.10");
        rootServers.put("provider", Constants.MY_DNS_IP_ADDRESS);
    }

    public RootServerHandler(PcapNetworkInterface nif, int snapLen,
                      PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException {
        this.handle = nif.openLive(snapLen, mode, timeout);
        this.answers = new ArrayList<>();

        var filter = "udp port " + Constants.DNS_PORT;

        try {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void handleDomain(String domain) {
        for (var entry : rootServers.entrySet()) {
            var value = entry.getValue();

            sendDnsQuery(value, domain);

            if (!answers.isEmpty()) {
                System.out.println("Root server: " + value +
                        " has found IP of " + domain +
                        ":\n" + answers.getFirst());
                break;
            } else {
                System.out.println("Root server: " + value +
                        " has not found IP of " + domain + " after analyzing " +
                        Constants.DNS_PACKETS_COUNT + " packets");
            }
        }
    }

    public void stopHandle() {
        handle.close();
    }

    private void sendDnsQuery(String rootServerIp, String domain) {
        try {
            var dnsQuestion = new DnsQuestion.Builder();

            dnsQuestion
                    .qName(new DnsDomainName.Builder()
                            .labels(domain.split("\\."))
                            .build())
                    .qType(DnsResourceRecordType.A)
                    .qClass(DnsClass.IN);

            var dnsPacketBuilder = new DnsPacket.Builder();
            var dnsId = random.nextInt(Short.MAX_VALUE);

            dnsPacketBuilder
                    .id((short) dnsId)
                    .opCode(DnsOpCode.getInstance((byte) 0))
                    .rCode(DnsRCode.NO_ERROR)
                    .qdCount((short) 1)
                    .truncated(false)
                    .recursionAvailable(false)
                    .recursionDesired(true)
                    .authenticData(true)
                    .questions(Collections.singletonList(dnsQuestion.build()))
                    .anCount((short) 0)
                    .nsCount((short) 0)
                    .arCount((short) 0);

            sentDnsQueries.put(dnsId, domain);

            var udpBuilder = new UdpPacket.Builder();

            udpBuilder
                    .srcPort(UdpPort.getInstance((short) Constants.UDP_PORT))
                    .dstPort(UdpPort.getInstance((short) Constants.DNS_PORT))
                    .srcAddr(InetAddress.getByName(Constants.MY_IP_ADDRESS))
                    .dstAddr(InetAddress.getByName(Constants.MY_DNS_IP_ADDRESS))
                    .length((short) (dnsPacketBuilder.build().length() + Constants.DNS_HEADER_SIZE))
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(dnsPacketBuilder);

            var ipv4Builder = new IpV4Packet.Builder();

            ipv4Builder
                    .identification((short) dnsId)
                    .protocol(IpNumber.UDP)
                    .srcAddr((Inet4Address) InetAddress.getByName(Constants.MY_IP_ADDRESS))
                    .dstAddr((Inet4Address) InetAddress.getByName(rootServerIp))
                    .version(IpVersion.IPV4)
                    .ttl((byte) 100)
                    .protocol(IpNumber.UDP)
                    .tos(IpV4Rfc1349Tos.newInstance((byte) 0))
                    .totalLength((short) (udpBuilder.build().length() + Constants.UDP_HEADER_SIZE))
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(udpBuilder);

            ipv4Builder.build();

            var ethernetBuilder = new EthernetPacket.Builder();

            ethernetBuilder
                    .srcAddr(Constants.MY_MAC_ADDRESS)
                    .dstAddr(Constants.MY_DNS_MAC_ADDRESS)
                    .type(EtherType.IPV4)
                    .pad(new byte[0])
                    .paddingAtBuild(true)
                    .payloadBuilder(ipv4Builder);

            var packet = ethernetBuilder.build();

            handle.sendPacket(packet);
            handle.loop(Constants.DNS_PACKETS_COUNT, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int extractDnsId(Packet packet) {
        if (packet.contains(DnsPacket.class)) {
            var dnsPacket = packet.get(DnsPacket.class);

            return dnsPacket.getHeader().getId();
        }

        return -1;
    }

    private final PacketListener listener = new PacketListener() {
        @Override
        public void gotPacket(Packet packet) {
            String answer = null;

            if (packet == null) return;

            if (packet.contains(UdpPacket.class)) {
                var udpPacket = packet.get(UdpPacket.class);

                if (udpPacket.getHeader().getDstPort().valueAsInt() == Constants.DNS_PORT) {
                    var payload = udpPacket.getPayload();

                    if (payload != null) {
                        var innerDnsPacket = payload.get(DnsPacket.class);
                        var dnsId = extractDnsId(innerDnsPacket);

                        if (sentDnsQueries.containsKey(dnsId) && innerDnsPacket.getHeader().isResponse()) {
                            answer = "Inner DNS packet:\n" + innerDnsPacket;
                        }
                    }

                    var dnsPacket = udpPacket.get(DnsPacket.class);
                    var dnsId = extractDnsId(dnsPacket);

                    if (sentDnsQueries.containsKey(dnsId) && dnsPacket.getHeader().isResponse()) {
                        answer = "DNS packet:\n" + dnsPacket;
                    }
                }
            }

            if (answer != null) {
                answers.add(answer);
                try {
                    handle.breakLoop();
                } catch (NotOpenException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };
}
