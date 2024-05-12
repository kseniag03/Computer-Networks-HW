package org.example.Utils;

import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;

import java.io.EOFException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class ArpCapture {
    private final int timeout;
    private final PcapHandle handle;
    private final List<ArpPacket> arpPackets;

    public ArpCapture(PcapNetworkInterface nif, int snapLen,
                      PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException {
        this.timeout = timeout;
        this.handle = nif.openLive(snapLen, mode, timeout);
        arpPackets = new ArrayList<>();

        var filter = "arp";

        try {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void startCapturing() {
        var startTime = System.currentTimeMillis();
        var endTime = startTime + timeout;

        while (System.currentTimeMillis() < endTime) {
            try {
                var packet = handle.getNextPacketEx();
                var arpPacket = packet.get(ArpPacket.class);

                if (arpPacket != null) {
                    arpPackets.add(arpPacket);
                }
            } catch (PcapNativeException | EOFException | TimeoutException | NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopCapturing() {
        handle.close();
    }

    public void printCapturedPackets() {
        arpPackets.forEach(arpPacket -> {
            System.out.println("ARP Packet captured:");
            System.out.println("ARP Header: " + arpPacket.getHeader());
            System.out.println("Operation: " + arpPacket.getHeader().getOperation());
            System.out.println("Sender Hardware Address: " + arpPacket.getHeader().getSrcHardwareAddr());
            System.out.println("Sender Protocol Address: " + arpPacket.getHeader().getSrcProtocolAddr());
            System.out.println("Target Hardware Address: " + arpPacket.getHeader().getDstHardwareAddr());
            System.out.println("Target Protocol Address: " + arpPacket.getHeader().getDstProtocolAddr());
            System.out.println("------------------------------------");
        });
    }
}
