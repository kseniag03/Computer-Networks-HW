package org.example.Utils.Arp;

import org.example.Common.Constants;
import org.pcap4j.core.*;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;

import java.io.EOFException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class StatisticsCollector {
    private final int timeout;
    private final PcapHandle handle;
    private final MacAddress macAddress;
    private long ethernetFrameCount;
    private long arpPacketCount;
    private final Set<String> uniqueMacAddresses;
    private final Map<String, Integer> macAddressPairs;
    private long broadcastFrameCount;
    private long incomingEthernetFrames;
    private long incomingArpPackets;
    private long outgoingEthernetFrames;
    private long outgoingArpPackets;
    private long otherEthernetFrames;
    private long totalDataVolume;

    public StatisticsCollector(PcapNetworkInterface nif, int snapLen,
                               PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException {
        this.timeout = timeout;
        this.handle = nif.openLive(snapLen, mode, timeout);
        this.macAddress = MacAddress.getByAddress(nif.getLinkLayerAddresses().getFirst().getAddress());
        this.ethernetFrameCount = 0;
        this.arpPacketCount = 0;
        this.uniqueMacAddresses = new HashSet<>();
        this.macAddressPairs = new HashMap<>();
        this.broadcastFrameCount = 0;
        this.incomingEthernetFrames = 0;
        this.incomingArpPackets = 0;
        this.outgoingEthernetFrames = 0;
        this.outgoingArpPackets = 0;
        this.otherEthernetFrames = 0;
        this.totalDataVolume = 0;

        var filter = "arp";

        try {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void collectStatistics() {
        var startTime = System.currentTimeMillis();
        var endTime = startTime + timeout;

        while (System.currentTimeMillis() < endTime) {
            analyzePacket();
        }
    }

    public void stopCollecting() {
        handle.close();
    }

    public void printStatistics() {
        System.out.println("Ethernet Frame Count: " + ethernetFrameCount);
        System.out.println("ARP Packet Count: " + arpPacketCount);
        System.out.println("Unique MAC Address Count: " + uniqueMacAddresses.size());
        System.out.println("MAC Address Pairs: " + macAddressPairs);
        System.out.println("Broadcast Frame Count: " + broadcastFrameCount);
        System.out.println("Incoming Ethernet Frames: " + incomingEthernetFrames);
        System.out.println("Incoming ARP Packets: " + incomingArpPackets);
        System.out.println("Outgoing Ethernet Frames: " + outgoingEthernetFrames);
        System.out.println("Outgoing ARP Packets: " + outgoingArpPackets);
        System.out.println("Other Ethernet Frames: " + otherEthernetFrames);
        System.out.println("Total Data Volume: " + totalDataVolume);
    }

    private void analyzePacket() {
        try {
            var packet = handle.getNextPacketEx();
            var payload = packet.getPayload();

            ++ethernetFrameCount;

            if (payload instanceof EthernetPacket ethernetPacket) {
                var destMac = ethernetPacket.getHeader().getDstAddr().getAddress();
                var srcMac = ethernetPacket.getHeader().getSrcAddr().getAddress();
                var srcMacAddress = ByteArrays.toHexString(srcMac, ":");
                var destMacAddress = ByteArrays.toHexString(destMac, ":");

                uniqueMacAddresses.add(srcMacAddress);
                uniqueMacAddresses.add(destMacAddress);

                var etherType = ethernetPacket.getHeader().getType();

                if (etherType.equals(EtherType.ARP)) {
                    ++arpPacketCount;

                    if (srcMacAddress.equals(destMacAddress)) {
                        ++incomingArpPackets;
                    } else {
                        ++outgoingArpPackets;
                    }
                }

                if (srcMacAddress.equals(Constants.BROADCAST_ADDRESS)) {
                    ++broadcastFrameCount;
                }

                if (srcMacAddress.equals(Constants.BROADCAST_ADDRESS)) {
                    ++broadcastFrameCount;
                }

                if (srcMacAddress.equals(macAddress.toString())) {
                    ++outgoingEthernetFrames;
                } else if (!destMacAddress.equals(macAddress.toString())) {
                    ++incomingEthernetFrames;
                } else {
                    ++otherEthernetFrames;
                }

                var macAddressPair = srcMacAddress + " - " + destMacAddress;

                totalDataVolume += packet.length();
                macAddressPairs.put(macAddressPair, macAddressPairs.getOrDefault(macAddressPair, 0) + 1);

            }
        } catch (PcapNativeException | TimeoutException | EOFException | NotOpenException e) {
            e.printStackTrace();
        }
    }
}
