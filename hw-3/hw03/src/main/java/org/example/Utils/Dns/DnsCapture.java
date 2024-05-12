package org.example.Utils.Dns;

import org.example.Common.Constants;
import org.example.Utils.DnsPacketsHandler;
import org.pcap4j.core.*;
import org.pcap4j.packet.DnsPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;

import java.util.ArrayList;
import java.util.List;

public class DnsCapture {
    private final int timeout;
    private final PcapHandle handle;
    private final List<DnsPacket> dnsPackets;

    public DnsCapture(PcapNetworkInterface nif, int snapLen,
                      PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException {
        this.timeout = timeout;
        this.handle = nif.openLive(snapLen, mode, timeout);
        dnsPackets = new ArrayList<>();

        var filter = "udp port " + Constants.DNS_PORT;

        try {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void startCapturing() {
        try {
            handle.loop(Constants.DNS_PACKETS_COUNT, packetListener);
        } catch (PcapNativeException | NotOpenException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            stopCapturing();
        }
    }

    public void stopCapturing() {
        handle.close();
    }

    public void printCapturedPacketInfo() {
        dnsPackets.forEach(dnsPacket -> {
            System.out.println("DNS Packet captured:");
            System.out.println(dnsPacket);
            System.out.println("------------------------------------");
        });
    }

    public List<DnsPacket> getCapturedPackets() {
        return dnsPackets;
    }

    private final PacketListener packetListener = new PacketListener() {
        @Override
        public void gotPacket(Packet packet) {
            if (packet == null) return;

            var dnsPacket = DnsPacketsHandler.processDnsPacket(packet);

            if (dnsPacket == null) return;

            dnsPackets.add(dnsPacket);

            if (dnsPackets.size() >= Constants.DNS_PACKETS_COUNT) {
                try {
                    handle.breakLoop();
                } catch (NotOpenException e) {
                    throw new RuntimeException(e);
                };
            }
        }
    };
}
