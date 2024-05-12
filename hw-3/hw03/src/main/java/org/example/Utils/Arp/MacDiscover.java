package org.example.Utils.Arp;

import org.example.Utils.Arp.ArpTargetedRequestsSender;
import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.util.MacAddress;

import java.io.EOFException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class MacDiscover {
    private final PcapHandle handle;
    private final MacAddress macAddress;

    public MacDiscover(PcapNetworkInterface nif, int snapLen,
                       PcapNetworkInterface.PromiscuousMode mode, int timeout)
            throws PcapNativeException {
        this.handle = nif.openLive(snapLen, mode, timeout);
        this.macAddress = MacAddress.getByAddress(nif.getLinkLayerAddresses().getFirst().getAddress());

        var filter = "arp";

        try {
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void printMacAddress() {
        System.out.println(macAddress.toString());
    }

    public void printMacAddressByIp(String targetIpAddress) {
        MacAddress macAddressByIp = null;

        try {
            ArpTargetedRequestsSender.sendArpTargetedRequest(handle, InetAddress.getLocalHost(),
                    InetAddress.getByName(targetIpAddress));

            var packet = handle.getNextPacketEx();
            var arpPacket = packet.get(ArpPacket.class);

            if (arpPacket != null && arpPacket.getHeader().getOperation().equals(ArpOperation.REPLY)) {
                macAddressByIp = arpPacket.getHeader().getSrcHardwareAddr();
            }
        } catch (PcapNativeException | TimeoutException | EOFException | NotOpenException | UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println(macAddressByIp.toString());
    }

    public void stopDiscovering() {
        handle.close();
    }
}
